package com.RPS.register;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.RPS.firebaselogin.R;
import com.RPS.main.MainActivity;

public class RegisterActivity extends AppCompatActivity {
    RegisterController logic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Button registerBtn = findViewById(R.id.registerBtn);
        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        EditText emailInput = findViewById(R.id.emailInput);
        logic = new RegisterController(this);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                String email = emailInput.getText().toString();
                if(verifyDetails(username,password,email)) {
                    try {
                        /* will accept or throw an error when the method returns from server and will show an appropriate message */
                        logic.createAccount(username,email,password)
                                .thenAccept(result -> {
                                    System.out.println(result);
                                    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    getApplicationContext().startActivity(intent);
                                })
                                        .exceptionally(ex -> {
                                            ex.printStackTrace();
                                            Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
                                            return null;
                                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else
                    System.out.println("registration couldn't be made due to invalid details");
            }
        });

    }
    /* verify all details are as supposed to be before making the request to register */
    private boolean verifyDetails(String username,String password,String email){
        if(!RegisterController.validEmail(email)){
            Toast.makeText(getApplicationContext(),"invalid email!",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!RegisterController.validPassword(password)){
            Toast.makeText(getApplicationContext(),"password must be at least " + RegisterController.PASS_MIN_CHAR + " characters",Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!RegisterController.validUsername(username)){
            Toast.makeText(getApplicationContext(),"username must be at least " + RegisterController.USER_MIN_CHAR + " characters",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    /* creates the account */
}