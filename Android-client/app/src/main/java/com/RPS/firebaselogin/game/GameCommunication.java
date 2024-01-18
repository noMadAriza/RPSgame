package com.RPS.firebaselogin.game;


import static com.RPS.firebaselogin.game.GameConstants.BOARD_SIZE;

import com.RPS.firebaselogin.utilities.SocketIOManager;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.socket.client.Ack;
import io.socket.client.Socket;

public class GameCommunication {
    Socket socket;
    GameActivity gameActivity;
    GameLogic gameLogic;

    public GameCommunication(GameLogic gameLogic,GameActivity gameActivity){
        this.gameLogic = gameLogic;
        this.gameActivity = gameActivity;
        socket = SocketIOManager.getInstance().getSocket();
        startListening();
        startGame();
    }

    // all listening functions
    public void startListening(){
        Thread thread = new Thread(() ->{
            /* gets an call from server to update the game */
            socket.on("updateFromServer", args -> {
                JSONObject jsonObject = (JSONObject) args[0];
                try {
                    gameLogic.getDataFromServer(jsonObject);
                    gameActivity.updateUI(gameLogic.getGamePlayers());
                    gameLogic.setMyTurn(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
            // gets its turn from server
            socket.on("turn", args -> {
                System.out.println("myTurn");
                gameLogic.setMyTurn(true);
            });
            // show choosing menu
            socket.on("showWarMenu", args -> {
                System.out.println("got showing menu emit !");
                gameActivity.showMenu();
            });
            // receives the color of the winner
            socket.on("winner", args -> {
                String winner = (String) args[0];
                if(winner.equalsIgnoreCase( "disconnected") || winner.equalsIgnoreCase(gameLogic.getColor().name()))
                    gameLogic.finishGame(true);
                else
                    gameLogic.finishGame(false);
                close();
            });
            //both players have joined therefore we can start the game
            socket.on("startGame", args -> {
                gameLogic.startGame();
            });
        });
        thread.start();
    }
    /* parameters: player matrix and lobbyId,
    sends to the lobbyID the game board information */
    public void updateServer(Player[][] gamePlayers, int lobbyID) throws JSONException {
        gamePlayers = (Player[][]) GameLogic.rotate(gamePlayers,gameLogic.getColor());
        JSONArray jsonMatrix = new JSONArray();
        for (int row = 0; row < BOARD_SIZE; row++) {
            JSONArray jsonInnerArray = new JSONArray(); // for each row
            for (int column = 0; column < BOARD_SIZE; column++) {
                JSONObject playerObject = new JSONObject();
                if(gamePlayers[row][column] != null) { // sends all the players
                    playerObject.put("value", gamePlayers[row][column].getType().ordinal());
                    playerObject.put("visible", gamePlayers[row][column].getVisible());
                    playerObject.put("color",gamePlayers[row][column].getMycolor().name());
                }
                else{
                    playerObject.put("value", Player.Players.EMPTY_CELL.ordinal());
                }
                jsonInnerArray.put(playerObject);
            }
            jsonMatrix.put(jsonInnerArray);
        }
        String json = new Gson().toJson(jsonMatrix);
        gameLogic.setMyTurn(false);
        socket.emit("updateFromClient",json,lobbyID,gameLogic.getColor().name());
    }

    /* sends the server a notification about a tie in the war and returns the new types of players each client chose (redPlayer,bluePlayer) */
    public Player.Players[] sendTie(int lobbyID) throws InterruptedException {
        AtomicReference<Player.Players[]> type = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            CountDownLatch mutex = new CountDownLatch(1);
            System.out.println("in the new thread!");
            socket.emit("tie", lobbyID, (Ack) args -> {
                JSONObject jsonObject = (JSONObject) args[0];
                System.out.println("in sendTie: " + jsonObject);
                try {
                    Player.Players[] playersTypes = new Player.Players[2];
                    playersTypes[0] = Player.Players.values()[jsonObject.getInt("redPlayer")];
                    playersTypes[1] = Player.Players.values()[jsonObject.getInt("bluePlayer")];
                    type.set(playersTypes);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mutex.countDown();
            });
            try {
                mutex.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        thread.join();
        System.out.println("after new thread finishes");
        return type.get();
    }

    //sends type to the server after clicking on war menu
    public void sendMenuChoose(Player.Players type){
        socket.emit("clickedOnMenu", gameLogic.getLobbyID(),gameLogic.getColor().name(),type.ordinal());
    }

    /* gets boolean winner and realGame
    if realGame = true: meaning a game already started, wont close all listening sockets
    else: will close
    not closing because the server will send us who is the winner and the loser later!
     */
    public void winner(boolean winner,boolean realGame){
        if(winner)
            socket.emit("endGame",gameLogic.getLobbyID(),gameLogic.getColor());
        else
            socket.emit("endGame",gameLogic.getLobbyID(),gameLogic.getOtherColor());
        if(!realGame)
            close();
    }

    public void startGame(){
        new Thread(() -> {
            socket.emit("startGame",gameLogic.getLobbyID());
        }).start();
    }
    private void close(){
        socket.off("updateFromServer");
        socket.off("turn");
        socket.off("showWarMenu");
        socket.off("winner");
        socket.off("startGame");
        gameActivity = null;
        gameLogic = null;
        socket = null;
    }


}
