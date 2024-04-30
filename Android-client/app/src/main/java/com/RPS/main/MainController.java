package com.RPS.main;

import com.RPS.utilities.DataBaseCommunication;
import com.RPS.game.GameLogic;
import com.RPS.utilities.LinkedListPair;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

public class MainController {

    final private MainDatabaseConnection databaseConnection;
    final private MainNetworkCommunication networkCommunication;
    final private MainActivity activity;
    final private FirebaseAuth mAuth;

    public MainController(MainActivity activity){
        mAuth = FirebaseAuth.getInstance();
        this.databaseConnection = new MainDatabaseConnection(DataBaseCommunication.getInstance(activity).getQueue());
        this.networkCommunication = new MainNetworkCommunication(this);
        this.activity = activity;
    }

    //sends to the network to create a new lobby and returns the lobby number created
    public CompletableFuture<Integer> createLobby() {
        CompletableFuture<Integer> res = new CompletableFuture<>();
        serverAvailable().thenAccept(lobbyID -> {
            if(lobbyID != -1)
                res.complete(lobbyID);
            else {
                networkCommunication.createLobby().thenAccept(lobbyID2 -> {
                    res.complete(lobbyID2);
                });
            }
        });
        return res;
    }

    //join to the lobbyID given
    public CompletableFuture<GameLogic.Color> joinLobby(int lobbyID){
        return networkCommunication.joinLobby(lobbyID);
    }

    //get list of available lobbies
    public CompletableFuture<LinkedListPair> getLobbies(){
        return networkCommunication.getLobbies();
    }

    public CompletableFuture<JSONObject> getUser(String user_id) {
        return DataBaseCommunication.getUser(DataBaseCommunication.getInstance(activity).getQueue(),user_id);

    }

    private CompletableFuture<Integer> serverAvailable() {
        return networkCommunication.serverAvailable();
    }
}
