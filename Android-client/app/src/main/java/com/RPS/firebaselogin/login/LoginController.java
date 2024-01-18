package com.RPS.firebaselogin.login;

import android.content.Context;

import com.RPS.firebaselogin.utilities.DataBaseCommunication;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.CompletableFuture;

public class LoginController {
    private FirebaseAuth mAuth;

    DataBaseCommunication dataBaseCommunication;
    public LoginController(Context context){
        this.dataBaseCommunication = DataBaseCommunication.getInstance(context);
        mAuth = FirebaseAuth.getInstance();
    }

    //signs in with parameters given and returns a CompletableFuture with the boolean of state of sign in
    public CompletableFuture<Boolean> signIn(String email, String password) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                future.complete(true); // Sign-in success,
             else
                future.complete(false); // If sign-in fails
        });
        return future;
    }
}
