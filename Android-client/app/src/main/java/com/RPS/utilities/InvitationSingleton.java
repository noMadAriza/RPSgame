package com.RPS.utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.RPS.firebaselogin.R;
import com.RPS.game.GameActivity;
import com.RPS.game.GameLogic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

import io.socket.client.Ack;
import io.socket.client.Socket;


public class InvitationSingleton {

    private static volatile InvitationSingleton instance;
    Activity activity;
    Context context;
    DataBaseCommunication dataBaseCommunication;

    private InvitationSingleton(){
        instance = this;
        Socket socket = SocketIOManager.getInstance().getSocket();
        dataBaseCommunication = DataBaseCommunication.getInstance(context);
        // start: didnt try yet !
        socket.on("invitation", args -> {
            String user_id = (String) args[0];
            DataBaseCommunication.getUser(dataBaseCommunication.getQueue(),user_id).thenAccept(data -> {
                try {
                    String username = data.getString("username");
                    invitedFriend(activity,context,username).thenAccept(res -> {
                        if(res)
                            socket.emit("acceptInvite",user_id);
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });
        // end: didnt try yet !
        socket.on("createdServerForGame", args -> {
            JSONObject json = (JSONObject) args[0];
            try {
                int lobbyID = json.getInt("lobbyID");
                System.out.println(lobbyID);
                joinLobby(lobbyID).thenAccept(color -> {
                    openGame(activity,context,lobbyID,color);
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public static synchronized InvitationSingleton getInstance(){
        if (instance == null) {
            synchronized (InvitationSingleton.class) {
                if(instance == null)
                    instance = new InvitationSingleton();
            }
        }
        return instance;
    }



    //listen for invitations
    public void setParams(Activity activity, Context context){
        this.activity = activity;
        this.context = context;
    }

    private CompletableFuture<GameLogic.Color> joinLobby(int lobbyID) {
        Socket socket = SocketIOManager.getInstance().getSocket();
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

    // if got invited and not in a game will show a popup of invitation
    private CompletableFuture<Boolean> invitedFriend(Activity activity, Context context, String username){
        CompletableFuture<Boolean> bool = new CompletableFuture<>();
        activity.runOnUiThread(() -> {
            View view = LayoutInflater.from(activity).inflate(R.layout.invitation_popup,null);
            activity.addContentView(view,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,200));
            view.setVisibility(View.VISIBLE);
            Button rejectButton = view.findViewById(R.id.rejectBtn);
            Button acceptButton = view.findViewById(R.id.acceptBtn);
            TextView textView = view.findViewById(R.id.usernamePlaceholder);
            textView.setText(username);
            rejectButton.setOnClickListener(view1 -> {
                // make the reject
                bool.complete(false);
                view.setVisibility(View.GONE);
            });
            acceptButton.setOnClickListener(view1 -> {
                // make it accept and create the lobby and join it using method in mainActivity
                bool.complete(true);
                view.setVisibility(View.GONE);
            });
        });
        return bool;
    }
    //open game activity
    private void openGame(Activity activity,Context context, int lobbyID, GameLogic.Color color){
        activity.runOnUiThread(() -> {
            Intent intent = new Intent(context, GameActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("lobbyID", lobbyID);
            intent.putExtra("color", color);
            context.startActivity(intent);
        });
    }
}
