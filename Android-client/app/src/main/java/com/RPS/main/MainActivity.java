package com.RPS.main;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;
import com.RPS.utilities.InvitationSingleton;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.RPS.leaderboards.LeaderBoardsActivity;
import com.RPS.game.GameLogic;
import com.RPS.firebaselogin.R;
import com.RPS.game.GameActivity;
import com.RPS.utilities.LinkedListPair;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    LinearLayout serverList;
    MainController controller;
    InvitationSingleton invitationSingleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controller = new MainController(this);
        Button friendsBtn = findViewById(R.id.friends);
        serverList = findViewById(R.id.serverList);
        Button refreshBtn = findViewById(R.id.refreshBtn);
        Button newLobby = findViewById(R.id.playBtn);
        invitationSingleton = InvitationSingleton.getInstance();


        newLobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(() -> {
                    controller.createLobby().thenAccept(lobbyID -> {
                        //lobby created
                        controller.joinLobby(lobbyID).thenAccept(color -> {
                            //lobby joined
                            openGame(lobbyID,color);
                        });
                    });
                    newLobby.setClickable(false);
                    try{
                        sleep(4500);
                        newLobby.setClickable(true);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }).start();
            }
        });

        friendsBtn.setOnClickListener(view -> {
            runOnUiThread(() -> {
                Intent intent = new Intent(getApplicationContext(), LeaderBoardsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            });
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.getLobbies().thenAccept(res -> {
                    showServerList(res);
                });
            }
        });
        invitationSingleton.setParams(this,this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invitationSingleton.setParams(this,this);
    }

    /* shows the list of all lobbies to join with the name of their creator
                the function should get LinkedListPair which the first linkedlist is of Integers representing lobbies ID
                the second is for linkedlist of Strings for the creator of the lobbies
             */
    private void showServerList(LinkedListPair pair) {
        runOnUiThread(() -> {
            serverList.removeAllViews();
            LinkedList<Integer> lobbies = (LinkedList<Integer>) pair.getList1();
            LinkedList<String> creators = (LinkedList<String>) pair.getList2();
            for (int i = 0; i < lobbies.size(); i++) {
                int lobbyID = lobbies.get(i);
                String creator = creators.get(i);
                LinearLayout lobby = new LinearLayout(getApplicationContext());
                Button lobbyJoin = new Button(getApplicationContext());
                TextView newLobbyName = new TextView(getApplicationContext());
                lobbyJoin.setText("join");
                newLobbyName.setText(creator);
                lobby.addView(newLobbyName);
                lobby.addView(lobbyJoin);
                serverList.addView(lobby);
                lobbyJoin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new Thread(() -> {
                            controller.joinLobby(lobbyID).thenAccept(color -> {
                                //joined the lobby
                                openGame(lobbyID, color);
                            });
                        }).start();
                    }
                });
            }
        });
    }

    //open game activity
    private void openGame(int lobbyID,GameLogic.Color color){
        runOnUiThread(() -> {
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("lobbyID", lobbyID);
            intent.putExtra("color", color);
            getApplicationContext().startActivity(intent);
        });
    }

}
