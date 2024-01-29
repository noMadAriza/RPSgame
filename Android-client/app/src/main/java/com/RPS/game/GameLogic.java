package com.RPS.game;

import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.UiThread;

import com.RPS.utilities.DataBaseCommunication;
import com.RPS.utilities.SocketIOManager;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

import io.socket.client.Ack;
import io.socket.client.Socket;

public class GameLogic {

    public enum Color{
        BLUE,RED
    }
    /* 2-dimensions array of the board with the pieces of the game on it */
    private ImageView[][] cellsImage;
    /* 2-dimensions array of the board with info of the pieces of game on it */
    private Player[][] gamePlayers;
    private int lobbyID;
    private MoveablePlayer lastClick = null;
    private Color color;
    private int cnt = 0; //non moving players have been put in place
    private Boolean myTurn = false;

    Socket socket;
    GameCommunication communication;
    GameDatabase database;
    GameActivity activity;
    FirebaseAuth mAuth;
    private Timer timer;

    public GameLogic(GameActivity activity,int lobby, Color color){
        this.lobbyID = lobby;
        this.color = color;
        mAuth = FirebaseAuth.getInstance();
        cellsImage = new ImageView[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
        gamePlayers = new Player[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
        this.communication = new GameCommunication(this,activity);
        socket = SocketIOManager.getInstance().getSocket();
        this.activity = activity;
        this.database = new GameDatabase(DataBaseCommunication.getInstance(activity).getQueue());
    }

    public Color getColor() {
        return color;
    }

    public ImageView[][] getCellsImage() {
        return cellsImage;
    }

    public MoveablePlayer getLastClick() {
        return lastClick;
    }

    public int getCnt() {
        return cnt;
    }
    public void addCnt(){
        cnt++;
    }
    public int getLobbyID() {
        return lobbyID;
    }

    /* return matrix of players */
    public Player[][] getGamePlayers() {
        return gamePlayers;
    }

    public Color getOtherColor(){
        int colorVal = color.ordinal();
        return Color.values()[(colorVal + 1) % 2];
    }

    public Boolean getMyTurn() {
        return myTurn;
    }

    public void setMyTurn(Boolean myTurn) {
        this.myTurn = myTurn;
        activity.showTurn(myTurn);
    }

    public static int[] findObjectPlace(TableLayout board, ImageView image){
        int[] place = new int[2];
        TableRow tableRow = (android.widget.TableRow) image.getParent();
        int column = (tableRow).indexOfChild(image);
        int row = board.indexOfChild(tableRow);
        place[0] = row;
        place[1] = column;
        return place;
    }

    // replace the last clicked player with the player given
    public void setLastClicked(MoveablePlayer player){
        if(this.lastClick != null){ //there was a clicked player before -- delete his highlight
            if(highlight(lastClick.row, lastClick.column - 1)) {
                activity.highlight(cellsImage[lastClick.row][lastClick.column - 1], lastClick.row, lastClick.column - 1, gamePlayers[lastClick.row][lastClick.column - 1], false);
                cellsImage[lastClick.row][lastClick.column - 1].setOnClickListener(null);
            }
            if(highlight(lastClick.row, lastClick.column + 1)){
                activity.highlight(cellsImage[lastClick.row][lastClick.column + 1],lastClick.row,lastClick.column + 1,gamePlayers[lastClick.row][lastClick.column + 1],false);
                cellsImage[lastClick.row][lastClick.column + 1].setOnClickListener(null);
            }
            if(highlight(lastClick.row - 1, lastClick.column)) {
                activity.highlight(cellsImage[lastClick.row - 1][lastClick.column], lastClick.row - 1, lastClick.column, gamePlayers[lastClick.row - 1][lastClick.column], false);
                cellsImage[lastClick.row - 1][lastClick.column].setOnClickListener(null);
            }
            if(highlight(lastClick.row + 1, lastClick.column)) {
                activity.highlight(cellsImage[lastClick.row + 1][lastClick.column], lastClick.row + 1, lastClick.column, gamePlayers[lastClick.row + 1][lastClick.column], false);
                cellsImage[lastClick.row + 1][lastClick.column].setOnClickListener(null);
            }
        }
        this.lastClick = player;
        if(player != null) {//player pressed is not null
            if (highlight(lastClick.row, lastClick.column - 1)) {
                activity.highlight(cellsImage[player.row][player.column - 1], player.row, player.column - 1, gamePlayers[player.row][player.column - 1], true);
                cellsImage[player.row][player.column - 1].setOnClickListener((view) -> {
                    move(player,MoveablePlayer.Direction.LEFT);
                });
            }
            if (highlight(lastClick.row, lastClick.column + 1)) {
                activity.highlight(cellsImage[player.row][player.column + 1], player.row, player.column + 1, gamePlayers[player.row][player.column + 1], true);
                cellsImage[player.row][player.column + 1].setOnClickListener((view) -> {
                    move(player,MoveablePlayer.Direction.RIGHT);
                });
            }
            if (highlight(lastClick.row - 1, lastClick.column)) {
                activity.highlight(cellsImage[player.row - 1][player.column], player.row - 1, player.column, gamePlayers[player.row - 1][player.column], true);
                cellsImage[player.row - 1][player.column].setOnClickListener((view) -> {
                    move(player,MoveablePlayer.Direction.FORWARD);
                });
            }
            if (highlight(lastClick.row + 1, lastClick.column)) {
                activity.highlight(cellsImage[player.row + 1][player.column], player.row + 1, player.column, gamePlayers[player.row + 1][player.column], true);
                cellsImage[player.row + 1][player.column].setOnClickListener((view) -> {
                    move(player,MoveablePlayer.Direction.BACKWARD);
                });
            }
        }

    }
    // returns true if the cell should be highlighted
    private boolean highlight(int row,int column){
        if(!insideGameBoard(row,column) || (gamePlayers[row][column] != null && gamePlayers[row][column].getMycolor() == color))
            return false;
        return true;
    }

    // gets data from server and updates the data in the local device
    public void getDataFromServer(JSONObject jsonObject) throws JSONException{
        startTimer();
        JSONObject[][] jsonMatrix = convertToMatrix(jsonObject);
        jsonMatrix = rotate(jsonMatrix,color);
        updateBoard(jsonMatrix);
    }

    /* gets a JsonObject and returns JsonObject[][] according to its data */
    private static JSONObject[][] convertToMatrix(JSONObject jsonObject) throws JSONException {
        JSONArray jsonArray = null;
        JSONObject[][] res = new JSONObject[GameConstants.BOARD_SIZE][GameConstants.BOARD_SIZE];
        try {
            jsonArray = jsonObject.getJSONArray("board");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int row = 0; row < jsonArray.length(); row++) {
            JSONArray innerArray = null;
            try { innerArray = jsonArray.getJSONArray(row);}
            catch (JSONException e) { e.printStackTrace(); }
            for (int column = 0; column < innerArray.length(); column++)
                res[row][column] = innerArray.getJSONObject(column);
        }
        return res;
    }

    /* in case game is BLUE: rotate 180 degrees
     * else return as is */
    public static Player[][] rotate(Player[][] matrix,Color color){
        if(color == Color.RED){
            return matrix;
        }
        Player[][] res = new Player[matrix.length][matrix[0].length];
        for (int row = 0; row < matrix.length; row++) {
            for (int column = 0; column < matrix[0].length; column++) {
                res[row][column] = matrix[matrix.length - 1 - row][matrix[0].length - 1 - column];
            }
        }
        return res;
    }

    /* in case game is BLUE: rotate 180 degrees
     * else return as is */
    private static JSONObject[][] rotate(JSONObject[][] matrix,Color color){
        if(color == Color.RED){
            return matrix;
        }
        JSONObject[][] res = new JSONObject[matrix.length][matrix[0].length];
        for (int row = 0; row < matrix.length; row++) {
            for (int column = 0; column < matrix[0].length; column++) {
                res[row][column] = matrix[matrix.length - 1 - row][matrix[0].length - 1 - column];
            }
        }
        return res;
    }

    /* gets JsonObject and updates the board based on it */
    private void updateBoard(JSONObject[][] matrix){
        for (int row = 0; row < GameConstants.BOARD_SIZE; row++) {
            if(cnt < 2 && row > 2){
                //meaning we are still chossing our initial non moving players so update only 2 upper lines
                break;
            }
            for (int column = 0; column < GameConstants.BOARD_SIZE; column++) {
                Boolean otherPlayer = false;
                int value = 0;
                Boolean visible = false;
                Player cell = gamePlayers[row][column];
                // if color of the object is the same as the enemy's color: otherPlayer will be true and value will hold its value
                try {
                    if(matrix[row][column].getString("color").equalsIgnoreCase(getOtherColor().name()))
                        otherPlayer = true;
                    visible = matrix[row][column].getBoolean("visible");
                    value = matrix[row][column].getInt("value");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // for each cell, remove all enemy's player previously in memory
                if(cell != null && cell.getMycolor() != color)
                    gamePlayers[row][column] = null;
                if(otherPlayer) { //true only when the the object is enemy's player
                    Player.Players PlayerVal = Player.Players.values()[value];
                    if(PlayerVal != Player.Players.EMPTY_CELL)
                        new NonMoveablePlayer(this, PlayerVal, getOtherColor(), row, column);
                }
                else{ //true only when the the object is our player
                    if(cell != null) { //the cell has to be not empty
                        if (visible) //if object is sent and is visible. change it to visible
                            cell.setVisible(true);
                        if (cell.getType().ordinal() != value) //if our player doesn't equals our type, we change it
                            cell.setType(Player.Players.values()[value]);
                        if(value == Player.Players.EMPTY_CELL.ordinal())
                            gamePlayers[row][column] = null;
                    }
                }
            }
        }
    }

    // make this player clickable
    public void makeClickable(MoveablePlayer player){
        cellsImage[player.row][player.column].setOnClickListener(view -> {
            setLastClicked(player);
        });
    }

    // if player can move, will move it, else will not and returns the abiility to move for that direction
    public void move(MoveablePlayer player,MoveablePlayer.Direction direction){
        new Thread(() -> {
            setLastClicked(null);
            if(!getMyTurn())
                return ;
            int newColumn = player.column;
            int newRow = player.row;
            switch (direction) {
                case LEFT:
                    newColumn--;
                    break;
                case RIGHT:
                    newColumn++;
                    break;
                case FORWARD:
                    newRow--;
                    break;
                case BACKWARD:
                    newRow++;
                    break;
            }
            try {
                //the square moving into has a player
                Player winner = player;
                if(gamePlayers[newRow][newColumn] != null) {
                    winner = war(player, gamePlayers[newRow][newColumn]);
                    System.out.println("the war was a success to: " + winner.getMycolor());
                }
                if(winner == player)
                    player.move(gamePlayers,cellsImage,direction);
                updateServer();
                activity.updateUI(gamePlayers);
                makeClickable(player);
                return ;
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    /* checks the move: if the square is empty, will move and returns true.
    if there's an enemy player. will go to war with and returns true as we used our turn in a war
    else: false
     */
    private boolean checkMove( MoveablePlayer player, MoveablePlayer.Direction direction){
        int newRow = player.row, newColumn = player.column;
        Player otherplayer;
        switch (direction) {
            case LEFT:
                newColumn--;
                break;
            case RIGHT:
                newColumn++;
                break;
            case FORWARD:
                newRow--;
                break;
            case BACKWARD:
                newRow++;
                break;
        }
        if(newRow >= GameConstants.BOARD_SIZE || newRow < 0 || newColumn >= GameConstants.BOARD_SIZE || newColumn < 0) // return false cause can't move to outside the board
            return false;
        if((otherplayer = gamePlayers[newRow][newColumn]) != null){ //in the place we move to there's a player
            if(otherplayer.getMycolor() == player.getMycolor()) //they're are both the same color
                return false;
            try {
                Player winner = war(player, gamePlayers[newRow][newColumn]);
                System.out.println("the war was a success to: " + winner.getMycolor()); // other players is an enemy
                if(player != winner)//lost the war
                    return true;
            }catch (Exception e){ e.printStackTrace(); }
        }
        player.move(gamePlayers,cellsImage,direction);
        return true;
    }

    /* player is attacking otherPlayer. returns the winner of the battle.
    in case of a tie we would need a re-choice
     */
    private Player war(Player player,Player otherPlayer) throws InterruptedException {
        CountDownLatch mutex = new CountDownLatch(1);
        int row = otherPlayer.row, column = otherPlayer.column;
        System.out.println("placement of otherPlayer: " + row + "," + column);
        if(color == Color.BLUE){ //inverted view in blue
            row = GameConstants.BOARD_SIZE - row - 1;
            column = GameConstants.BOARD_SIZE - column - 1;
        }
        socket.emit("getPlayer", lobbyID, row, column, (Ack) args -> {
            JSONObject jsonObject = (JSONObject) args[0];
            try {
                System.out.println(jsonObject);
                int type = jsonObject.getInt("value");
                otherPlayer.setType(Player.Players.values()[type]);
                otherPlayer.setVisible(true);
                mutex.countDown();
            }catch (Exception e){ e.printStackTrace(); }
        });
        mutex.await();
        System.out.println("int war!!\n my type is: " + player.getType().ordinal() + " his type: " + otherPlayer.getType().ordinal());
        Player winner = null;
        Player loser = null;
        while(player.getType() == otherPlayer.getType()){
            Player.Players[] playersChosen = tie();
            if(player.getMycolor() == Color.RED) {
                player.setType(playersChosen[0]);
                otherPlayer.setType(playersChosen[1]);
            }
            else{
                player.setType(playersChosen[1]);
                otherPlayer.setType(playersChosen[0]);
            }
        }
        if(otherPlayer.getType() == Player.Players.TRAP){
            winner = otherPlayer;
        }
        else if(otherPlayer.getType() == Player.Players.FLAG){
            winner = player;
            win(true);
        }
        else if (player.getType() == Player.Players.ROCK) {
            if (otherPlayer.getType() == Player.Players.PAPER)
                winner = otherPlayer;
            else
                winner = player;
        }
        else if (player.getType() == Player.Players.PAPER) {
            if (otherPlayer.getType() == Player.Players.SCISSORS)
                winner = otherPlayer;
            else
                winner = player;
        }
        else if(player.getType() == Player.Players.SCISSORS) {
            if (otherPlayer.getType() == Player.Players.ROCK)
                winner = otherPlayer;
            else
                winner = player;
        }
        if(player == winner)
            loser = otherPlayer;
        else
            loser = player;
        otherPlayer.setVisible(true);
        player.setVisible(true);
        gamePlayers[loser.row][loser.column] = null;
        cellsImage[loser.row][loser.column].setOnClickListener(null);
        return winner;
    }

    // sends to server the game has finished
    public void win(boolean winner) {
        if(cnt >= 2)
            communication.winner(winner,true);
        else
            communication.winner(winner,false);
    }

    // finishes the game. the input receives is boolean of whether you won. should be used only for server
    public void finishGame(boolean winner){
        database.changePoints(mAuth.getUid(),winner,lobbyID);
        database = null;
        communication = null;
        activity.finishGame(winner);
    }

    private Player.Players[] tie() {
        Player.Players[] array = new Player.Players[2];
        System.out.println("in a Tie");
        try {
            array = communication.sendTie(lobbyID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return array;
    }

    // convert player object to JSONObject and return it
    private JSONObject convertPlayerToJson(Player player) throws JSONException {
        JSONObject playerObject = new JSONObject();
        if(player != null) {
            playerObject.put("value", player.getType().ordinal());
            playerObject.put("visible", player.visible);
            playerObject.put("color", player.getMycolor().name());
        }
        else{
            playerObject.put("value", 0);
        }
        return playerObject;
    }

    //start the game
    public void startGame() {
        activity.initialGameState(cellsImage);
    }

    //updates the server
    public void updateServer() {
        new Thread(() -> {
            try {
                communication.updateServer(gamePlayers, lobbyID);
            }catch(Exception e) { e.printStackTrace(); }
        }).start();
    }

    //get the type selected from the war menu to the server
    public void sendMenuChoose(Player.Players type) {
        new Thread(() -> {
            communication.sendMenuChoose(type);
        }).start();
    }

    // returns true for inside the board squares
    private boolean insideGameBoard(int row,int column){
        if(row >= 0 && column >= 0 && row < GameConstants.BOARD_SIZE && column < GameConstants.BOARD_SIZE)
            return true;
        return false;
    }

    // returns true for Empty squares
    private boolean isEmptySquare(int row,int column){
        if(insideGameBoard(row,column) && (gamePlayers[row][column] == null || gamePlayers[row][column].getType() == Player.Players.EMPTY_CELL))
            return true;
        return false;
    }
    public void startTimer(){
        timer = new Timer(this,activity,activity.USER_TIME_TO_PLAY);
        timer.start();
    }
    public void stopTimer(){
        if(timer != null)
            timer.stopTimer();
    }
}