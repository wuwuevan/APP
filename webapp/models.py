"""Database models and helpers for the Flask web experience."""
from __future__ import annotations

from datetime import datetime, timedelta
from typing import Any, Dict, Iterable, List

from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import func
from sqlalchemy.ext.mutable import MutableDict, MutableList
from sqlalchemy.types import JSON
from flask_login import UserMixin


db = SQLAlchemy()


class TimestampMixin:
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class User(UserMixin, TimestampMixin, db.Model):
    __tablename__ = "users"

    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(64), unique=True, nullable=False)
    password_hash = db.Column(db.String(256), nullable=False)
    gender = db.Column(db.String(16))
    age = db.Column(db.Integer)
    height = db.Column(db.Float)
    weight = db.Column(db.Float)
    phone = db.Column(db.String(32))
    email = db.Column(db.String(128))
    medical_record = db.Column(db.Text)
    register_time = db.Column(db.DateTime, default=datetime.utcnow)

    tasks = db.relationship("DailyTask", back_populates="user", cascade="all, delete-orphan")
    notifications = db.relationship("Notification", back_populates="user", cascade="all, delete-orphan")
    posts = db.relationship("CommunityPost", back_populates="author")

    def __repr__(self) -> str:  # pragma: no cover - debugging helper
        return f"<User {self.username}>"


class DailyTask(TimestampMixin, db.Model):
    __tablename__ = "daily_tasks"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    task_name = db.Column(db.String(128), nullable=False)
    task_desc = db.Column(db.Text)
    task_type = db.Column(db.String(32))
    start_time = db.Column(db.Date, nullable=False)
    end_time = db.Column(db.Date, nullable=False)
    status = db.Column(db.String(32), default="待完成")
    completion_rate = db.Column(db.Float, default=0.0)

    user = db.relationship("User", back_populates="tasks")
    education_content = db.relationship("EducationContent", back_populates="task", uselist=False)
    survey = db.relationship("Survey", back_populates="task", uselist=False)
    pain_scores = db.relationship("PainScore", back_populates="task", cascade="all, delete-orphan")

    def schedule_label(self) -> str:
        return f"{self.start_time.strftime('%Y-%m-%d')} - {self.end_time.strftime('%Y-%m-%d')}"


class EducationContent(TimestampMixin, db.Model):
    __tablename__ = "education_contents"

    id = db.Column(db.Integer, primary_key=True)
    task_id = db.Column(db.Integer, db.ForeignKey("daily_tasks.id"), nullable=False)
    title = db.Column(db.String(128), nullable=False)
    content = db.Column(db.Text)
    type = db.Column(db.String(32))

    task = db.relationship("DailyTask", back_populates="education_content")


class Survey(TimestampMixin, db.Model):
    __tablename__ = "surveys"

    id = db.Column(db.Integer, primary_key=True)
    task_id = db.Column(db.Integer, db.ForeignKey("daily_tasks.id"), nullable=False)
    title = db.Column(db.String(128), nullable=False)
    description = db.Column(db.Text)
    questions = db.Column(MutableList.as_mutable(JSON), default=list)
    answers = db.Column(MutableList.as_mutable(JSON), default=list)
    submit_time = db.Column(db.DateTime)

    task = db.relationship("DailyTask", back_populates="survey")


class PainScore(TimestampMixin, db.Model):
    __tablename__ = "pain_scores"

    id = db.Column(db.Integer, primary_key=True)
    task_id = db.Column(db.Integer, db.ForeignKey("daily_tasks.id"), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    score = db.Column(db.Integer, nullable=False)
    location = db.Column(db.String(64))
    description = db.Column(db.Text)
    record_time = db.Column(db.DateTime, default=datetime.utcnow)

    task = db.relationship("DailyTask", back_populates="pain_scores")


class HealthIndicator(TimestampMixin, db.Model):
    __tablename__ = "health_indicators"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    indicator_type = db.Column(db.String(64), nullable=False)
    indicator_value = db.Column(db.Float, nullable=False)
    record_time = db.Column(db.DateTime, default=datetime.utcnow)
    is_abnormal = db.Column(db.Boolean, default=False)
    custom_info = db.Column(MutableDict.as_mutable(JSON), default=dict)

    user = db.relationship("User", backref="health_indicators")

    @staticmethod
    def latest_value(user_id: int, indicator_type: str, fallback: float | int | None = None) -> float | int | None:
        indicator = (
            HealthIndicator.query.filter_by(user_id=user_id, indicator_type=indicator_type)
            .order_by(HealthIndicator.record_time.desc())
            .first()
        )
        if indicator:
            return indicator.indicator_value
        if fallback is not None:
            indicator = HealthIndicator(
                user_id=user_id,
                indicator_type=indicator_type,
                indicator_value=fallback,
                record_time=datetime.utcnow(),
            )
            db.session.add(indicator)
            db.session.commit()
            return fallback
        return None

    @staticmethod
    def latest_by_type(user_id: int) -> Dict[str, HealthIndicator]:
        subquery = (
            db.session.query(
                HealthIndicator.indicator_type,
                func.max(HealthIndicator.record_time).label("max_time"),
            )
            .filter_by(user_id=user_id)
            .group_by(HealthIndicator.indicator_type)
            .subquery()
        )
        rows = (
            HealthIndicator.query.join(
                subquery,
                (HealthIndicator.indicator_type == subquery.c.indicator_type)
                & (HealthIndicator.record_time == subquery.c.max_time),
            )
            .filter(HealthIndicator.user_id == user_id)
            .all()
        )
        return {row.indicator_type: row for row in rows}

    @staticmethod
    def group_by_type(indicators: Iterable["HealthIndicator"]) -> Dict[str, List["HealthIndicator"]]:
        grouped: Dict[str, List[HealthIndicator]] = {}
        for indicator in indicators:
            grouped.setdefault(indicator.indicator_type, []).append(indicator)
        return grouped


class ChatMessage(TimestampMixin, db.Model):
    __tablename__ = "chat_messages"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    sender_type = db.Column(db.String(16), nullable=False)  # "user" or "coach"
    content = db.Column(db.Text, nullable=False)
    send_time = db.Column(db.DateTime, default=datetime.utcnow)
    is_read = db.Column(db.Boolean, default=False)
    is_risk = db.Column(db.Boolean, default=False)
    is_encrypted = db.Column(db.Boolean, default=False)

    user = db.relationship("User", backref="chat_messages")

    @staticmethod
    def generate_coach_reply(user_id: int, content: str) -> "ChatMessage":
        tips = "保持每日复查，并注意训练时的呼吸节奏。"
        if "疼" in content or "痛" in content:
            reply = "注意观察疼痛变化，如有加重请及时联系医生。"
        elif "进步" in content:
            reply = "非常棒的进步！继续保持，有任何疑问随时告诉我。"
        else:
            reply = tips
        return ChatMessage(
            user_id=user_id,
            sender_type="coach",
            content=reply,
            send_time=datetime.utcnow(),
            is_read=True,
        )


class Notification(TimestampMixin, db.Model):
    __tablename__ = "notifications"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    notification_type = db.Column(db.String(32))
    title = db.Column(db.String(128), nullable=False)
    content = db.Column(db.Text)
    create_time = db.Column(db.DateTime, default=datetime.utcnow)
    is_read = db.Column(db.Boolean, default=False)

    user = db.relationship("User", back_populates="notifications")


class CommunityPost(TimestampMixin, db.Model):
    __tablename__ = "community_posts"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    title = db.Column(db.String(128), nullable=False)
    content = db.Column(db.Text, nullable=False)
    category = db.Column(db.String(32))
    create_time = db.Column(db.DateTime, default=datetime.utcnow)
    likes = db.Column(db.Integer, default=0)

    author = db.relationship("User", back_populates="posts")
    comments = db.relationship("CommunityComment", back_populates="post", cascade="all, delete-orphan")


class CommunityComment(TimestampMixin, db.Model):
    __tablename__ = "community_comments"

    id = db.Column(db.Integer, primary_key=True)
    post_id = db.Column(db.Integer, db.ForeignKey("community_posts.id"), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    content = db.Column(db.Text, nullable=False)
    create_time = db.Column(db.DateTime, default=datetime.utcnow)

    post = db.relationship("CommunityPost", back_populates="comments")
    user = db.relationship("User")


class Feedback(TimestampMixin, db.Model):
    __tablename__ = "feedbacks"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    message = db.Column(db.Text, nullable=False)
    create_time = db.Column(db.DateTime, default=datetime.utcnow)


class GaitEvent(TimestampMixin, db.Model):
    __tablename__ = "gait_events"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    event_time = db.Column(db.DateTime, default=datetime.utcnow)
    step_count = db.Column(db.Integer)
    cadence = db.Column(db.Float)
    speed = db.Column(db.Float)
    risk_flag = db.Column(db.Boolean, default=False)
    note = db.Column(db.Text)

    user = db.relationship("User")


def create_default_daily_plan(user: User) -> None:
    """Create default tasks and related data for a user if none exist."""
    has_tasks = DailyTask.query.filter_by(user_id=user.id).first()
    if has_tasks:
        return

    today = datetime.utcnow().date()
    tasks = [
        DailyTask(
            user_id=user.id,
            task_name="阅读康复文章",
            task_desc="了解术后恢复的注意事项",
            task_type="教育内容",
            start_time=today,
            end_time=today,
        ),
        DailyTask(
            user_id=user.id,
            task_name="填写健康问卷",
            task_desc="让我们更好地了解您的恢复情况",
            task_type="问卷",
            start_time=today,
            end_time=today,
        ),
        DailyTask(
            user_id=user.id,
            task_name="记录今日疼痛评分",
            task_desc="记录疼痛变化，帮助医生评估",
            task_type="疼痛评分",
            start_time=today,
            end_time=today,
        ),
    ]
    db.session.add_all(tasks)
    db.session.flush()

    education = EducationContent(
        task_id=tasks[0].id,
        title="术后第2周训练要点",
        content="每日进行踝泵练习、股四头肌等长收缩训练，注意逐渐增加活动量。",
        type="文章",
    )
    survey = Survey(
        task_id=tasks[1].id,
        title="术后康复自评问卷",
        description="请根据您今日的实际情况填写",
        questions=[
            "今天的行走疼痛程度如何？",
            "昨晚睡眠质量如何？",
            "今天是否完成所有康复训练？",
        ],
    )
    db.session.add_all([education, survey])

    indicators = [
        HealthIndicator(user_id=user.id, indicator_type="心率", indicator_value=72, record_time=datetime.utcnow()),
        HealthIndicator(user_id=user.id, indicator_type="步数", indicator_value=6243, record_time=datetime.utcnow()),
        HealthIndicator(user_id=user.id, indicator_type="睡眠时长", indicator_value=7.5, record_time=datetime.utcnow()),
        HealthIndicator(user_id=user.id, indicator_type="血压", indicator_value=118, record_time=datetime.utcnow()),
    ]
    db.session.add_all(indicators)

    notifications = [
        Notification(user_id=user.id, notification_type="任务提醒", title="今日任务待完成", content="别忘了完成今日的康复任务哦！"),
        Notification(user_id=user.id, notification_type="康复提示", title="新的运动建议", content="尝试在行走时放慢步伐并保持呼吸稳定。"),
    ]
    db.session.add_all(notifications)

    posts = [
        CommunityPost(
            user_id=user.id,
            title="膝关节康复心得",
            content="坚持冰敷和股四头肌训练，膝盖肿胀有明显改善。",
            category="经验分享",
        ),
        CommunityPost(
            user_id=user.id,
            title="寻求大家的建议",
            content="训练时偶尔会感到小腿酸胀，大家是如何缓解的？",
            category="问题求助",
        ),
    ]
    db.session.add_all(posts)
    db.session.flush()

    comments = [
        CommunityComment(post_id=posts[1].id, user_id=user.id, content="拉伸小腿肌肉并注意热身可以缓解。"),
    ]
    db.session.add_all(comments)

    gait_events = [
        GaitEvent(
            user_id=user.id,
            event_time=datetime.utcnow() - timedelta(hours=2),
            step_count=1200,
            cadence=92,
            speed=0.8,
            risk_flag=False,
            note="步态稳定，建议继续保持。",
        ),
        GaitEvent(
            user_id=user.id,
            event_time=datetime.utcnow() - timedelta(days=1),
            step_count=800,
            cadence=78,
            speed=0.6,
            risk_flag=True,
            note="检测到支撑相缩短，请注意训练姿势。",
        ),
    ]
    db.session.add_all(gait_events)


def seed_demo_data() -> None:
    """Populate the database with demo information if empty."""
    if User.query.first():
        return

    from werkzeug.security import generate_password_hash

    demo = User(
        username="demo",
        password_hash=generate_password_hash("demo123"),
        gender="女",
        age=32,
        height=165,
        weight=58,
        phone="13800000000",
        email="demo@example.com",
        medical_record="左膝关节镜术后第2周，进行居家康复训练",
    )
    db.session.add(demo)
    db.session.flush()

    create_default_daily_plan(demo)
    db.session.commit()


__all__ = [
    "db",
    "User",
    "DailyTask",
    "EducationContent",
    "Survey",
    "PainScore",
    "HealthIndicator",
    "ChatMessage",
    "Notification",
    "CommunityPost",
    "CommunityComment",
    "Feedback",
    "GaitEvent",
    "create_default_daily_plan",
    "seed_demo_data",
]
