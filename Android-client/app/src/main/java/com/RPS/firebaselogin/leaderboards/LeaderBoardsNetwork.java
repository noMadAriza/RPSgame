package com.RPS.firebaselogin.leaderboards;

import com.RPS.firebaselogin.utilities.SocketIOManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

import io.socket.client.Ack;
import io.socket.client.Socket;

public class LeaderBoardsNetwork {

    final Socket socket;
    LeaderBoardsController controller;

    public LeaderBoardsNetwork(LeaderBoardsController controller){
        this.controller = controller;
        this.socket = SocketIOManager.getInstance().getSocket();
    }

    // gets list of all user_id in the form of JSONArray and returns JSONObject with the first attribute containing connected users and second with offline users
    public CompletableFuture<JSONObject> getFriends(JSONArray list){
        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        socket.emit("getConnectedClients",list, (Ack) args -> {
            System.out.println("just got the response from server:" + ((JSONObject) args[0]).toString());
            future.complete((JSONObject) args[0]);
        });
        return future;
    }
}
