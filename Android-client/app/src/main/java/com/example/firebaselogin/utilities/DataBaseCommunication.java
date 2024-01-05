package com.example.firebaselogin.utilities;

import android.content.Context;
import android.util.Log;

import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

//Singleton
public class DataBaseCommunication {

    public static final String url = "http://35.158.32.95:8080";
    private static volatile DataBaseCommunication instance;
    final RequestQueue queue;

    private DataBaseCommunication(Context context){
        this.queue = Volley.newRequestQueue(context);
    }
    public static synchronized DataBaseCommunication getInstance(Context context){
        if (instance == null) {
            synchronized (DataBaseCommunication.class) {
                if(instance == null)
                    instance = new DataBaseCommunication(context);
            }
        }
        return instance;
    }

    //get user information with his username
    public static CompletableFuture<JSONObject> getUserWithUserName(RequestQueue queue,String username){
        CompletableFuture<JSONObject> future= new CompletableFuture<>();
        String url = DataBaseCommunication.url + "/user/username/" + username;
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        future.complete(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                        if(error instanceof ParseError)
                            future.complete(new JSONObject());
                        future.completeExceptionally(new Throwable("couldn't retrieve information"));
                    }
                }
        );
        queue.add(getRequest);
        return future;
    }
    // gets user_id and returns all info about that user id
    public static CompletableFuture<JSONObject> getUser(RequestQueue queue,String user_id) {
        CompletableFuture<JSONObject> future= new CompletableFuture<>();
        String url = DataBaseCommunication.url + "/user/" + user_id;
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        future.complete(response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                        future.completeExceptionally(new Throwable("couldn't retrieve information"));
                    }
                }
        );
        queue.add(getRequest);
        return future;
    }

    public RequestQueue getQueue() {
        return queue;
    }
}
