package com.RPS.firebaselogin.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.RPS.firebaselogin.R;
import com.RPS.firebaselogin.register.RegisterActivity;
import com.RPS.firebaselogin.register.RegisterController;
import com.RPS.firebaselogin.main.MainActivity;

public class LoginActivity extends AppCompatActivity {
    private final Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        LoginController logic = new LoginController(this);
        super.onStart();

        Button registerBtn = findViewById(R.id.registerBtnPage);
        Button loginBtn = findViewById(R.id.loginBtn);
        EditText myEmail = findViewById(R.id.EmailTxt);
        EditText myPass = findViewById(R.id.passwordContainer);

        /*  clicking on login btn */
        loginBtn.setOnClickListener(view -> {
            String email = myEmail.getText().toString();
            String password = myPass.getText().toString();
            if(verifyLogin(email,password)) {
                logic.signIn(email, password)
                        .thenAccept(result -> {
                                if(result)
                                    successfulLogin(email);
                                else
                                    Toast.makeText(getApplicationContext(), "email or password are incorrect!", Toast.LENGTH_SHORT).show();

                        });

            }
            else
                System.out.println("the login failed due to invalid details");
        });

        registerBtn.setOnClickListener(view -> {
            /* open register tab */
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(intent);
        });
    }

    // shows a prompt with successful login and opens Main activity as a root
    private void successfulLogin(String name){
        Toast.makeText(getApplicationContext(), "welcome back " + name + "!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    /* gets email and password as parameters returns true if valid, else returns false and shows an appropriate massage */
    private boolean verifyLogin(String email, String pass){
        if(!RegisterController.validEmail(email)) {
            Toast.makeText(getApplicationContext(), "invalid mail!", Toast.LENGTH_LONG).show();
            return false;
        }
        else if(!RegisterController.validPassword(pass)) {
            Toast.makeText(getApplicationContext(), "invalid password!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}