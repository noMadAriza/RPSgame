package com.example.firebaselogin.game;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.example.firebaselogin.utilities.DataBaseCommunication;

import java.util.HashMap;
import java.util.Map;

public class GameDatabase {

    final RequestQueue queue;

    public GameDatabase(RequestQueue queue){
        this.queue = queue;
    }

    //change points to the user
    public void changePoints(String user_id,Boolean winner,int lobbyID){
        System.out.println("the lobby:" + lobbyID);
        new Thread(() ->{
            int points;
            if(winner)
                points = 30;
            else
                points = -30;
            String url = DataBaseCommunication.url + "/user/" + user_id + "/points";
            System.out.println(url);
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    response -> System.out.println("dataBase updated"),
                    error -> error.printStackTrace()
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("points", String.valueOf(points));
                    return params;
                }
            };
            queue.add(postRequest);
        }).start();

    }
}
