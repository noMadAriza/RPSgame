package com.example.firebaselogin.register;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.example.firebaselogin.utilities.DataBaseCommunication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class RegisterController {
    public static final int EMAIL_MIN_CHAR = 7;
    public static final int USER_MIN_CHAR = 5;
    public static final int PASS_MIN_CHAR = 6;

    DataBaseCommunication dataBaseCommunication;
    private final FirebaseAuth mAuth;

    public RegisterController(Context context){
        dataBaseCommunication = DataBaseCommunication.getInstance(context);
        mAuth = FirebaseAuth.getInstance();
    }

    /* creates a new user with the parameters in the database */
    private void createUser(String id, String username, String Email){
        Thread thread = new Thread(() -> {
            RequestQueue queue = dataBaseCommunication.getQueue();
            StringRequest postRequest = new StringRequest(Request.Method.POST, DataBaseCommunication.url + "/user",
                    response -> System.out.println("dataBase updated"),
                    error -> error.printStackTrace()
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", id);
                    params.put("username", username);
                    params.put("email",Email);

                    return params;
                }
            };
            queue.add(postRequest);
        });
        thread.start();
    }

    /* creates the account and returns a future */
    public CompletableFuture<String> createAccount(String username, String email, String password) throws InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    createUser(user.getUid(), username, email);
                    future.complete("user has been created!");
                } else {
                    // If registration fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    if (task.getException() instanceof FirebaseAuthUserCollisionException)
                        future.completeExceptionally(new Exception("Email is already being used!"));
                    else
                        future.completeExceptionally(new Exception("couldn't complete the registration"));
                }
            });
        return future;
    }

    //makes sure the email is at least as long as needed
    public static boolean validEmail(String email){
        if(email.length() < EMAIL_MIN_CHAR)
            return false;
        return true;
    }
    //makes sure the password is at least as long as needed
    public static boolean validPassword(String pass){
        if(pass.length() < PASS_MIN_CHAR)
            return false;
        return true;
    }
    //makes sure the username is at least as long as needed
    public static boolean validUsername(String username){
        if(username.length() < USER_MIN_CHAR)
            return false;
        return true;
    }
}
