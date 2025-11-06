"""Flask web application mirroring the Android app workflows."""
from __future__ import annotations

from datetime import datetime, timedelta
from typing import Any

from flask import Flask, flash, redirect, render_template, request, url_for
from flask_login import (LoginManager, current_user, login_required,
                         login_user, logout_user)
from sqlalchemy.orm import joinedload
from werkzeug.security import check_password_hash, generate_password_hash

from .models import (ChatMessage, CommunityComment, CommunityPost, DailyTask,
                     EducationContent, Feedback, GaitEvent, HealthIndicator,
                     Notification, PainScore, Survey, User, create_default_daily_plan,
                     db, seed_demo_data)


def create_app(config: dict[str, Any] | None = None) -> Flask:
    app = Flask(__name__, static_folder="static", template_folder="templates")
    app.config.setdefault("SECRET_KEY", "super-secret-change-me")
    app.config.setdefault("SQLALCHEMY_DATABASE_URI", "sqlite:///rehabilitation_web.db")
    app.config.setdefault("SQLALCHEMY_TRACK_MODIFICATIONS", False)

    if config:
        app.config.update(config)

    db.init_app(app)

    login_manager = LoginManager(app)
    login_manager.login_view = "login"

    @login_manager.user_loader
    def load_user(user_id: str) -> User | None:
        return db.session.get(User, int(user_id))

    @app.context_processor
    def inject_globals() -> dict[str, Any]:
        return {"current_year": datetime.utcnow().year}

    @app.before_first_request
    def ensure_database_seeded() -> None:
        db.create_all()
        seed_demo_data()

    # Authentication routes -------------------------------------------------
    @app.route("/", methods=["GET"])
    def index() -> str:
        if current_user.is_authenticated:
            return redirect(url_for("dashboard"))
        return redirect(url_for("login"))

    @app.route("/login", methods=["GET", "POST"])
    def login() -> str:
        if current_user.is_authenticated:
            return redirect(url_for("dashboard"))
        if request.method == "POST":
            username = request.form.get("username", "").strip()
            password = request.form.get("password", "")
            user = User.query.filter_by(username=username).first()
            if user and check_password_hash(user.password_hash, password):
                login_user(user)
                flash("登录成功，欢迎回来！", "success")
                return redirect(url_for("dashboard"))
            flash("用户名或密码错误", "danger")
        return render_template("auth/login.html")

    @app.route("/register", methods=["GET", "POST"])
    def register() -> str:
        if current_user.is_authenticated:
            return redirect(url_for("dashboard"))
        if request.method == "POST":
            username = request.form.get("username", "").strip()
            password = request.form.get("password", "")
            confirm = request.form.get("confirm", "")
            if not username or not password:
                flash("请输入用户名和密码", "warning")
            elif password != confirm:
                flash("两次输入的密码不一致", "warning")
            elif User.query.filter_by(username=username).first():
                flash("用户名已存在", "warning")
            else:
                user = User(
                    username=username,
                    password_hash=generate_password_hash(password),
                    register_time=datetime.utcnow(),
                )
                db.session.add(user)
                db.session.flush()
                create_default_daily_plan(user)
                db.session.commit()
                flash("注册成功，请登录", "success")
                return redirect(url_for("login"))
        return render_template("auth/register.html")

    @app.route("/logout")
    @login_required
    def logout() -> str:
        logout_user()
        flash("您已退出登录", "info")
        return redirect(url_for("login"))

    # Dashboard -------------------------------------------------------------
    @app.route("/dashboard")
    @login_required
    def dashboard() -> str:
        today = datetime.utcnow().date()
        tasks = (
            DailyTask.query.filter(
                DailyTask.user_id == current_user.id,
                DailyTask.start_time <= today,
                DailyTask.end_time >= today,
            )
            .order_by(DailyTask.start_time.asc())
            .all()
        )
        total = len(tasks)
        completed = sum(1 for task in tasks if task.status == "已完成")
        progress = int((completed / total) * 100) if total else 0

        heart_rate = HealthIndicator.latest_value(current_user.id, "心率", fallback=72)
        step_count = HealthIndicator.latest_value(current_user.id, "步数", fallback=6243)
        sleep_hours = HealthIndicator.latest_value(current_user.id, "睡眠时长", fallback=7.5)

        notifications = (
            Notification.query.filter_by(user_id=current_user.id)
            .order_by(Notification.create_time.desc())
            .limit(4)
            .all()
        )

        return render_template(
            "dashboard.html",
            tasks=tasks,
            total_tasks=total,
            completed_tasks=completed,
            progress=progress,
            heart_rate=heart_rate,
            step_count=step_count,
            sleep_hours=sleep_hours,
            notifications=notifications,
        )

    # Task center -----------------------------------------------------------
    @app.route("/tasks")
    @login_required
    def task_list() -> str:
        tasks = (
            DailyTask.query.filter_by(user_id=current_user.id)
            .order_by(DailyTask.start_time.desc())
            .all()
        )
        return render_template("tasks/list.html", tasks=tasks)

    def _update_task_status(task: DailyTask, status: str, completion: float) -> None:
        task.status = status
        task.completion_rate = completion
        task.updated_at = datetime.utcnow()
        db.session.commit()

    @app.route("/tasks/<int:task_id>", methods=["GET", "POST"])
    @login_required
    def task_detail(task_id: int) -> str:
        task = DailyTask.query.filter_by(id=task_id, user_id=current_user.id).first_or_404()
        if request.method == "POST":
            action = request.form.get("action")
            if action == "complete":
                _update_task_status(task, "已完成", 1.0)
                flash("任务已完成", "success")
            elif action == "skip":
                _update_task_status(task, "已跳过", task.completion_rate)
                flash("任务已跳过", "info")
            elif action == "delay":
                task.start_time = task.start_time + timedelta(days=1)
                task.end_time = task.end_time + timedelta(days=1)
                db.session.commit()
                flash("任务已延后至明天", "info")
            return redirect(url_for("task_detail", task_id=task.id))

        education = EducationContent.query.filter_by(task_id=task.id).first()
        survey = Survey.query.filter_by(task_id=task.id).first()
        pain = PainScore.query.filter_by(task_id=task.id).order_by(PainScore.record_time.desc()).first()
        return render_template(
            "tasks/detail.html",
            task=task,
            education=education,
            survey=survey,
            latest_pain=pain,
        )

    @app.route("/tasks/<int:task_id>/survey", methods=["GET", "POST"])
    @login_required
    def task_survey(task_id: int) -> str:
        task = DailyTask.query.filter_by(id=task_id, user_id=current_user.id).first_or_404()
        survey = Survey.query.filter_by(task_id=task.id).first_or_404()
        questions = survey.questions or []
        if request.method == "POST":
            answers = []
            for idx, _ in enumerate(questions):
                answers.append(request.form.get(f"q{idx}", ""))
            survey.answers = answers
            survey.submit_time = datetime.utcnow()
            _update_task_status(task, "已完成", 1.0)
            flash("问卷已提交", "success")
            return redirect(url_for("task_detail", task_id=task.id))
        return render_template("tasks/survey.html", task=task, survey=survey, questions=questions)

    @app.route("/tasks/<int:task_id>/pain", methods=["GET", "POST"])
    @login_required
    def task_pain(task_id: int) -> str:
        task = DailyTask.query.filter_by(id=task_id, user_id=current_user.id).first_or_404()
        if request.method == "POST":
            score = int(request.form.get("score", 0))
            location = request.form.get("location", "")
            description = request.form.get("description", "")
            pain_score = PainScore(
                task_id=task.id,
                user_id=current_user.id,
                score=score,
                location=location,
                description=description,
                record_time=datetime.utcnow(),
            )
            db.session.add(pain_score)
            _update_task_status(task, "已完成", 1.0)
            flash("疼痛评分已记录", "success")
            return redirect(url_for("task_detail", task_id=task.id))
        history = (
            PainScore.query.filter_by(task_id=task.id, user_id=current_user.id)
            .order_by(PainScore.record_time.desc())
            .all()
        )
        return render_template("tasks/pain.html", task=task, history=history)

    # Health ----------------------------------------------------------------
    @app.route("/health")
    @login_required
    def health() -> str:
        indicators = (
            HealthIndicator.query.filter_by(user_id=current_user.id)
            .order_by(HealthIndicator.record_time.desc())
            .all()
        )
        grouped = HealthIndicator.group_by_type(indicators)
        return render_template("health/index.html", grouped=grouped)

    @app.route("/health/add", methods=["GET", "POST"])
    @login_required
    def health_add() -> str:
        if request.method == "POST":
            indicator_type = request.form.get("indicator_type", "").strip()
            value = float(request.form.get("indicator_value", 0))
            unit = request.form.get("unit", "")
            normal_min = request.form.get("normal_min")
            normal_max = request.form.get("normal_max")
            notes = request.form.get("notes", "")

            indicator = HealthIndicator(
                user_id=current_user.id,
                indicator_type=indicator_type,
                indicator_value=value,
                record_time=datetime.utcnow(),
            )
            db.session.add(indicator)
            db.session.flush()

            if unit or normal_min or normal_max or notes:
                indicator.custom_info = {
                    "unit": unit,
                    "normal_min": float(normal_min) if normal_min else None,
                    "normal_max": float(normal_max) if normal_max else None,
                    "notes": notes,
                }
            db.session.commit()
            flash("健康指标已保存", "success")
            return redirect(url_for("health"))
        return render_template("health/add.html")

    @app.route("/health/report")
    @login_required
    def health_report() -> str:
        latest = HealthIndicator.latest_by_type(current_user.id)
        return render_template("health/report.html", latest=latest)

    # Gait ------------------------------------------------------------------
    @app.route("/gait")
    @login_required
    def gait() -> str:
        events = (
            GaitEvent.query.filter_by(user_id=current_user.id)
            .order_by(GaitEvent.event_time.desc())
            .all()
        )
        return render_template("gait/index.html", events=events)

    # Chat ------------------------------------------------------------------
    @app.route("/chat", methods=["GET", "POST"])
    @login_required
    def chat() -> str:
        if request.method == "POST":
            content = request.form.get("message", "").strip()
            if content:
                user_msg = ChatMessage(
                    user_id=current_user.id,
                    sender_type="user",
                    content=content,
                    send_time=datetime.utcnow(),
                    is_read=True,
                )
                db.session.add(user_msg)
                response = ChatMessage.generate_coach_reply(current_user.id, content)
                db.session.add(response)
                db.session.commit()
                flash("消息已发送", "success")
            else:
                flash("请输入消息内容", "warning")
        messages = (
            ChatMessage.query.filter_by(user_id=current_user.id)
            .order_by(ChatMessage.send_time.asc())
            .all()
        )
        return render_template("chat/index.html", messages=messages)

    # Notifications ---------------------------------------------------------
    @app.route("/notifications")
    @login_required
    def notification_list() -> str:
        notifications = (
            Notification.query.filter_by(user_id=current_user.id)
            .order_by(Notification.create_time.desc())
            .all()
        )
        return render_template("notifications/list.html", notifications=notifications)

    @app.route("/notifications/<int:notification_id>")
    @login_required
    def notification_detail(notification_id: int) -> str:
        notification = Notification.query.filter_by(
            id=notification_id, user_id=current_user.id
        ).first_or_404()
        if not notification.is_read:
            notification.is_read = True
            db.session.commit()
        return render_template("notifications/detail.html", notification=notification)

    # Community -------------------------------------------------------------
    @app.route("/community", methods=["GET", "POST"])
    @login_required
    def community() -> str:
        if request.method == "POST":
            content = request.form.get("content", "").strip()
            title = request.form.get("title", "").strip()
            category = request.form.get("category", "经验分享")
            if content and title:
                post = CommunityPost(
                    user_id=current_user.id,
                    title=title,
                    content=content,
                    category=category,
                    create_time=datetime.utcnow(),
                )
                db.session.add(post)
                db.session.commit()
                flash("帖子已发布", "success")
            else:
                flash("请输入标题和内容", "warning")
        posts = (
            CommunityPost.query.options(joinedload(CommunityPost.comments))
            .order_by(CommunityPost.create_time.desc())
            .all()
        )
        return render_template("community/index.html", posts=posts)

    @app.route("/community/<int:post_id>/comment", methods=["POST"])
    @login_required
    def community_comment(post_id: int) -> str:
        post = CommunityPost.query.get_or_404(post_id)
        content = request.form.get("content", "").strip()
        if content:
            comment = CommunityComment(
                post_id=post.id,
                user_id=current_user.id,
                content=content,
                create_time=datetime.utcnow(),
            )
            db.session.add(comment)
            db.session.commit()
            flash("评论已发布", "success")
        else:
            flash("请输入评论内容", "warning")
        return redirect(url_for("community"))

    # Profile ---------------------------------------------------------------
    @app.route("/profile", methods=["GET", "POST"])
    @login_required
    def profile() -> str:
        user = current_user
        if request.method == "POST":
            user.gender = request.form.get("gender")
            user.age = request.form.get("age") or None
            user.height = request.form.get("height") or None
            user.weight = request.form.get("weight") or None
            user.phone = request.form.get("phone")
            user.email = request.form.get("email")
            user.medical_record = request.form.get("medical_record")
            db.session.commit()
            flash("个人资料已更新", "success")
        return render_template("profile/index.html", user=user)

    @app.route("/feedback", methods=["GET", "POST"])
    @login_required
    def feedback() -> str:
        if request.method == "POST":
            message = request.form.get("message", "").strip()
            if message:
                fb = Feedback(user_id=current_user.id, message=message, create_time=datetime.utcnow())
                db.session.add(fb)
                db.session.commit()
                flash("感谢您的反馈！", "success")
                return redirect(url_for("dashboard"))
            flash("请输入反馈内容", "warning")
        return render_template("profile/feedback.html")

    return app


__all__ = ["create_app"]
