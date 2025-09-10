package com.example.qlsukien;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    EditText Username, Password;
    TextView txtRegister;
    Button btnLogin, btnRegister;

    // Gọi đúng API trả danh sách người dùng (users)
    String URL = "https://baidoxe.onrender.com/api/users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        TextView txtRegister = findViewById(R.id.txtRegister);




        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình đăng ký
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });




    }




}
