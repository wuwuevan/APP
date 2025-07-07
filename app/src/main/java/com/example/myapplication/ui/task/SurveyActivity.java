package com.example.myapplication.ui.task;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.database.DailyTaskDao;
import com.example.myapplication.database.SurveyDao;
import com.example.myapplication.model.DailyTask;
import com.example.myapplication.model.Survey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 问卷页面
 */
public class SurveyActivity extends AppCompatActivity {
    private TextView tvTitle;
    private TextView tvDescription;
    private LinearLayout llQuestions;
    private Button btnSubmit;
    
    private DailyTaskDao taskDao;
    private SurveyDao surveyDao;
    private int taskId;
    private DailyTask currentTask;
    private Survey currentSurvey;
    private Map<String, String> answers = new HashMap<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        
        // 初始化DAO
        taskDao = new DailyTaskDao(this);
        surveyDao = new SurveyDao(this);
        
        // 设置返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("问卷调查");
        }
        
        // 初始化视图
        initViews();
        
        // 获取传递的任务ID
        taskId = getIntent().getIntExtra("task_id", -1);
        if (taskId == -1) {
            Toast.makeText(this, "问卷不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 加载问卷
        loadSurvey();
        
        // 设置提交按钮点击事件
        btnSubmit.setOnClickListener(v -> submitSurvey());
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);
        llQuestions = findViewById(R.id.ll_questions);
        btnSubmit = findViewById(R.id.btn_submit);
    }
    
    /**
     * 加载问卷
     */
    private void loadSurvey() {
        // 获取任务信息
        currentTask = taskDao.getTaskById(taskId);
        if (currentTask == null) {
            Toast.makeText(this, "任务不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 获取问卷信息
        currentSurvey = surveyDao.getSurveyByTaskId(taskId);
        if (currentSurvey != null) {
            // 设置标题和描述
            tvTitle.setText(currentSurvey.getTitle());
            tvDescription.setText(currentSurvey.getDescription());
            
            // 如果已经提交过，显示已提交的答案
            if (!TextUtils.isEmpty(currentSurvey.getSubmitTime())) {
                try {
                    JSONArray answersArray = new JSONArray(currentSurvey.getAnswers());
                    for (int i = 0; i < answersArray.length(); i++) {
                        JSONObject answerObj = answersArray.getJSONObject(i);
                        answers.put(answerObj.getString("question_id"), answerObj.getString("answer"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            
            // 加载问题
            try {
                JSONArray questionsArray = new JSONArray(currentSurvey.getQuestions());
                for (int i = 0; i < questionsArray.length(); i++) {
                    JSONObject questionObj = questionsArray.getJSONObject(i);
                    String questionId = questionObj.getString("id");
                    String questionText = questionObj.getString("text");
                    String questionType = questionObj.getString("type");
                    JSONArray options = questionObj.getJSONArray("options");
                    
                    addQuestionView(questionId, questionText, questionType, options);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "问卷格式错误", Toast.LENGTH_SHORT).show();
            }
            
            // 如果已提交，禁用提交按钮
            if (!TextUtils.isEmpty(currentSurvey.getSubmitTime())) {
                btnSubmit.setEnabled(false);
                btnSubmit.setText("已提交");
                Toast.makeText(this, "您已提交过此问卷", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 如果没有找到问卷，创建默认问卷
            createDefaultSurvey();
        }
        
        // 如果任务未完成，标记为进行中
        if ("未完成".equals(currentTask.getStatus())) {
            taskDao.updateTaskStatus(taskId, "进行中", 0.5f);
        }
    }
    
    /**
     * 创建默认问卷
     */
    private void createDefaultSurvey() {
        String title = "术后恢复状况问卷";
        String description = "请根据您的实际情况填写以下问题，帮助我们了解您的康复进度。";
        
        try {
            JSONArray questionsArray = new JSONArray();
            
            // 问题1
            JSONObject q1 = new JSONObject();
            q1.put("id", "q1");
            q1.put("text", "1. 您目前的疼痛程度如何？");
            q1.put("type", "radio");
            JSONArray q1Options = new JSONArray();
            q1Options.put("无疼痛");
            q1Options.put("轻微疼痛");
            q1Options.put("中度疼痛");
            q1Options.put("重度疼痛");
            q1Options.put("极度疼痛");
            q1.put("options", q1Options);
            questionsArray.put(q1);
            
            // 问题2
            JSONObject q2 = new JSONObject();
            q2.put("id", "q2");
            q2.put("text", "2. 您的伤口恢复情况如何？");
            q2.put("type", "radio");
            JSONArray q2Options = new JSONArray();
            q2Options.put("愈合良好");
            q2Options.put("有轻微红肿");
            q2Options.put("有渗液");
            q2Options.put("有感染迹象");
            q2Options.put("其他问题");
            q2.put("options", q2Options);
            questionsArray.put(q2);
            
            // 问题3
            JSONObject q3 = new JSONObject();
            q3.put("id", "q3");
            q3.put("text", "3. 您的活动能力如何？");
            q3.put("type", "radio");
            JSONArray q3Options = new JSONArray();
            q3Options.put("可以正常活动");
            q3Options.put("轻微受限");
            q3Options.put("中度受限");
            q3Options.put("严重受限");
            q3Options.put("完全无法活动");
            q3.put("options", q3Options);
            questionsArray.put(q3);
            
            // 问题4
            JSONObject q4 = new JSONObject();
            q4.put("id", "q4");
            q4.put("text", "4. 您的睡眠质量如何？");
            q4.put("type", "radio");
            JSONArray q4Options = new JSONArray();
            q4Options.put("很好");
            q4Options.put("良好");
            q4Options.put("一般");
            q4Options.put("较差");
            q4Options.put("很差");
            q4.put("options", q4Options);
            questionsArray.put(q4);
            
            // 问题5
            JSONObject q5 = new JSONObject();
            q5.put("id", "q5");
            q5.put("text", "5. 您是否按时服用了医生开的药物？");
            q5.put("type", "radio");
            JSONArray q5Options = new JSONArray();
            q5Options.put("是，完全按时");
            q5Options.put("基本按时");
            q5Options.put("偶尔忘记");
            q5Options.put("经常忘记");
            q5Options.put("没有服用");
            q5.put("options", q5Options);
            questionsArray.put(q5);
            
            // 创建问卷对象
            Survey survey = new Survey(taskId, title, description, questionsArray.toString());
            long id = surveyDao.addSurvey(survey);
            
            if (id > 0) {
                survey.setId((int) id);
                currentSurvey = survey;
                
                // 设置标题和描述
                tvTitle.setText(title);
                tvDescription.setText(description);
                
                // 添加问题视图
                for (int i = 0; i < questionsArray.length(); i++) {
                    JSONObject questionObj = questionsArray.getJSONObject(i);
                    String questionId = questionObj.getString("id");
                    String questionText = questionObj.getString("text");
                    String questionType = questionObj.getString("type");
                    JSONArray options = questionObj.getJSONArray("options");
                    
                    addQuestionView(questionId, questionText, questionType, options);
                }
            } else {
                Toast.makeText(this, "创建问卷失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "创建问卷失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * 添加问题视图
     */
    private void addQuestionView(String questionId, String questionText, String questionType, JSONArray options) throws JSONException {
        // 创建问题标题
        TextView tvQuestion = new TextView(this);
        tvQuestion.setText(questionText);
        tvQuestion.setTextSize(16);
        tvQuestion.setPadding(0, 16, 0, 8);
        llQuestions.addView(tvQuestion);
        
        if ("radio".equals(questionType)) {
            // 创建单选按钮组
            RadioGroup radioGroup = new RadioGroup(this);
            radioGroup.setOrientation(RadioGroup.VERTICAL);
            radioGroup.setTag(questionId);
            
            // 添加选项
            for (int i = 0; i < options.length(); i++) {
                String optionText = options.getString(i);
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(optionText);
                radioButton.setId(View.generateViewId());
                radioGroup.addView(radioButton);
                
                // 如果有已保存的答案，选中对应选项
                if (answers.containsKey(questionId) && answers.get(questionId).equals(optionText)) {
                    radioButton.setChecked(true);
                }
            }
            
            // 设置选择监听器
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton radioButton = findViewById(checkedId);
                if (radioButton != null) {
                    answers.put(questionId, radioButton.getText().toString());
                }
            });
            
            llQuestions.addView(radioGroup);
        }
        
        // 添加分隔线
        View divider = new View(this);
        divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.setMargins(0, 16, 0, 16);
        divider.setLayoutParams(params);
        llQuestions.addView(divider);
    }
    
    /**
     * 提交问卷
     */
    private void submitSurvey() {
        // 检查是否所有问题都已回答
        try {
            JSONArray questionsArray = new JSONArray(currentSurvey.getQuestions());
            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject questionObj = questionsArray.getJSONObject(i);
                String questionId = questionObj.getString("id");
                
                if (!answers.containsKey(questionId)) {
                    Toast.makeText(this, "请回答所有问题", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // 构建答案JSON
            JSONArray answersArray = new JSONArray();
            for (Map.Entry<String, String> entry : answers.entrySet()) {
                JSONObject answerObj = new JSONObject();
                answerObj.put("question_id", entry.getKey());
                answerObj.put("answer", entry.getValue());
                answersArray.put(answerObj);
            }
            
            // 获取当前时间
            String submitTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            
            // 更新问卷答案
            int result = surveyDao.updateSurveyAnswers(currentSurvey.getId(), answersArray.toString(), submitTime);
            
            if (result > 0) {
                // 更新任务状态
                taskDao.updateTaskStatus(taskId, "已完成", 1.0f);
                
                Toast.makeText(this, "问卷提交成功", Toast.LENGTH_SHORT).show();
                btnSubmit.setEnabled(false);
                btnSubmit.setText("已提交");
                
                // 返回上一页
                finish();
            } else {
                Toast.makeText(this, "提交失败，请重试", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "提交失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}