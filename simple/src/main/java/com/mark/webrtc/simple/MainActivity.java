package com.mark.webrtc.simple;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import cn.aorise.webrtc.chat.ChatClient;
import cn.aorise.webrtc.chat.User;

/**
 * <pre>
 *     author : Mark
 *     e-mail : makun.cai@aorise.org
 *     time   : 2018/12/24
 *     desc   : TODO
 *     version: 1.0
 * </pre>
 */
public class MainActivity extends AppCompatActivity {

    private EditText callName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        callName = findViewById(R.id.et_call_username);
    }

    public void callVideo(View view) {
        if (TextUtils.isEmpty(callName.getText().toString().trim())){
            Toast.makeText(MainActivity.this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = new User();
        user.setUserName(callName.getText().toString().trim());
        ChatClient.getInstance().getSignalManager().call(this,user,true,false);
    }

    public void callAudio(View view) {
        if (TextUtils.isEmpty(callName.getText().toString().trim())){
            Toast.makeText(MainActivity.this, "用户名不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = new User();
        user.setUserName(callName.getText().toString().trim());
        ChatClient.getInstance().getSignalManager().call(this,user,false,false);
    }
}
