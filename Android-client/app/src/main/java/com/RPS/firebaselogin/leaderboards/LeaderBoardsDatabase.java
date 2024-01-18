package com.RPS.firebaselogin.leaderboards;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.RPS.firebaselogin.utilities.DataBaseCommunication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LeaderBoardsDatabase {
    final RequestQueue queue;

    public LeaderBoardsDatabase(RequestQueue queue){
        this.queue = queue;
    }

    // gets list of all friends of the user given
    public CompletableFuture<JSONArray> findFriends(String user_id){
        CompletableFuture<JSONArray> future = new CompletableFuture<>();
        String url = DataBaseCommunication.url + "/friends/" + user_id;
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
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

    //adds other_id user as a friend of user_id
    public void addFriend(String user_id,String other_id){
        String url = DataBaseCommunication.url + "/friends/add/" + user_id;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> System.out.println("dataBase updated"),
                error -> error.printStackTrace()
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<>();
                params.put("other_id", other_id);
                return params;
            }
        };
        queue.add(postRequest);
    }

    //get user information
    public CompletableFuture<JSONObject> getUserWithUserName(String username){
        return DataBaseCommunication.getUserWithUserName(queue,username);
    }

    public CompletableFuture<JSONObject> getUserWithID(String id) {
        return DataBaseCommunication.getUser(queue,id);
    }
}