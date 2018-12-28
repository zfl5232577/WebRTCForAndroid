package com.mark.webrtc.simple;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;


import cn.aorise.webrtc.chat.ChatClient;

public class LoginActivity extends AppCompatActivity {
    private Button sigin;
    private AutoCompleteTextView username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sigin = findViewById(R.id.email_sign_in_button);
        username = findViewById(R.id.email);
        sigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(username.getText().toString().trim())){
                    Toast.makeText(LoginActivity.this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                ChatClient.getInstance().login(username.getText().toString().trim(), "张三", "");
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();
            }
        });
    }

}
