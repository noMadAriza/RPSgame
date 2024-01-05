package com.example.firebaselogin.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.firebaselogin.leaderboards.LeaderBoardsActivity;
import com.example.firebaselogin.game.GameLogic;
import com.example.firebaselogin.R;
import com.example.firebaselogin.game.GameActivity;
import com.example.firebaselogin.utilities.LinkedListPair;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    LinearLayout serverList;
    MainController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controller = new MainController(this);
        Button leaderboardsBtn = findViewById(R.id.leaderboardsBtn);
        Button newLobby = findViewById(R.id.addLobbyBtn);
        serverList = findViewById(R.id.serverList);
        Button refreshBtn = findViewById(R.id.refreshBtn);

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
                }).start();
            }
        });

        leaderboardsBtn.setOnClickListener(view -> {
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
