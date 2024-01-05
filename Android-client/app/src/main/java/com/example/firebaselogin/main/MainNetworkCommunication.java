package com.example.firebaselogin.main;

import com.example.firebaselogin.game.GameLogic;
import com.example.firebaselogin.utilities.LinkedListPair;
import com.example.firebaselogin.utilities.SocketIOManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import io.socket.client.Ack;
import io.socket.client.Socket;

public class MainNetworkCommunication {

    final Socket socket;
    private final MainController controller;

    public MainNetworkCommunication(MainController controller){
        this.controller = controller;
        this.socket = SocketIOManager.getInstance().getSocket();
    }

    //get list of available lobbies
    public CompletableFuture<LinkedListPair> getLobbies() {
        CompletableFuture<LinkedListPair> future = new CompletableFuture<>();
        new Thread(() -> {
            socket.emit("getAllLobbiesAvailable",(Ack) response ->{
                try {
                    JSONArray jsonArrayLobbies = ((JSONObject)response[0]).getJSONArray("lobbies");
                    System.out.println(jsonArrayLobbies);
                    JSONArray jsonArrayCreators = ((JSONObject)response[0]).getJSONArray("creators");
                    LinkedList<Integer> lobbies = new LinkedList<>();
                    LinkedList<String> creators = new LinkedList<>();
                    CountDownLatch countDownLatch = new CountDownLatch(jsonArrayCreators.length());
                    for (int i = 0; i < jsonArrayCreators.length(); i++) {
                        controller.getUser(jsonArrayCreators.getString(i)).thenAccept(res -> {
                            try {
                                creators.push(res.getString("username"));
                                countDownLatch.countDown();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                    for (int i = 0; i < jsonArrayLobbies.length(); i++) {
                        lobbies.push(jsonArrayLobbies.getInt(i));
                    }
                    countDownLatch.await();
                    future.complete(new LinkedListPair(lobbies,creators));
                }catch(Exception e) {
                    e.printStackTrace(); }
            });
        }).start();
        return future;
    }

    //creates a new lobby in server and returns its ID
    public CompletableFuture<Integer> createLobby() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        new Thread(() -> {
            socket.emit("newLobby", (Ack) args -> {
                //lobby has been created in server
                JSONObject response = (JSONObject) args[0];
                try {
                    int lobbyID = response.getInt("lobbyID");
                    future.complete(lobbyID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }).start();
        return future;
    }

    //joins to a lobby given and return the color given to you
    public CompletableFuture<GameLogic.Color> joinLobby(int lobbyID){
        CompletableFuture<GameLogic.Color> future = new CompletableFuture<>();
        new Thread(() -> {
            socket.emit("joinLobby",lobbyID, (Ack) args ->{
                JSONObject response = (JSONObject) args[0];
                try{
                    GameLogic.Color color = null;
                    String string = response.getString("color");
                    if(string.equalsIgnoreCase("red"))
                        color = GameLogic.Color.RED;
                    else if(string.equalsIgnoreCase("blue"))
                        color = GameLogic.Color.BLUE;
                    future.complete(color);
                } catch (Exception e){
                    e.printStackTrace();
                }
            });
        }).start();
        return future;
    }
}
