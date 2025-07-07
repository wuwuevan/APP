package com.example.myapplication.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.data.AppDatabase;
import com.example.myapplication.data.User;
import com.example.myapplication.data.UserDao;
import com.example.myapplication.ui.health.InitialHealthMetricsActivity;
import com.example.myapplication.utils.SharedPreferencesUtil;

/**
 * Activity for selecting user gender after registration.
 */
public class GenderSelectionActivity extends AppCompatActivity {

    private ImageView ivMale;
    private ImageView ivFemale;
    private Button btnMale;
    private Button btnFemale;
    private Button btnConfirm;

    private String selectedGender;

    private UserDao userDao;
    private SharedPreferencesUtil spUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender_selection);

        userDao = AppDatabase.getInstance(this).userDao();
        spUtil = new SharedPreferencesUtil(this);

        initViews();
        setListeners();
    }

    private void initViews() {
        ivMale = findViewById(R.id.iv_male);
        ivFemale = findViewById(R.id.iv_female);
        btnMale = findViewById(R.id.btn_male);
        btnFemale = findViewById(R.id.btn_female);
        btnConfirm = findViewById(R.id.btn_confirm);
    }

    private void setListeners() {
        View.OnClickListener maleListener = v -> chooseGender("男");
        View.OnClickListener femaleListener = v -> chooseGender("女");

        ivMale.setOnClickListener(maleListener);
        btnMale.setOnClickListener(maleListener);
        ivFemale.setOnClickListener(femaleListener);
        btnFemale.setOnClickListener(femaleListener);

        btnConfirm.setOnClickListener(v -> confirmSelection());
    }

    private void chooseGender(String gender) {
        selectedGender = gender;
        btnConfirm.setVisibility(View.VISIBLE);
    }

    private void confirmSelection() {
        if (selectedGender == null) {
            return;
        }
        String username = spUtil.getString("current_username", "");
        if (!username.isEmpty()) {
            User user = userDao.getUserByUsername(username);
            if (user != null) {
                user.setGender(selectedGender);
                userDao.updateUser(user);
            }
        }
        Intent intent = new Intent(GenderSelectionActivity.this, InitialHealthMetricsActivity.class);
        startActivity(intent);
        finish();
    }
}
