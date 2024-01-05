package com.example.firebaselogin.leaderboards;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaselogin.R;
import com.example.firebaselogin.utilities.DataBaseCommunication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class
LeaderBoardsActivity extends AppCompatActivity {

    LeaderBoardsController controller;
    Button refreshBtn;
    Button addFriendBtn;
    TextView friendsSearch;
    LinearLayout friendsList;
    TextView myInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_boards);

        controller = new LeaderBoardsController(this);

        refreshBtn = findViewById(R.id.refreshBtn);
        addFriendBtn = findViewById(R.id.searchBtn);
        friendsSearch = findViewById(R.id.usernameInput);
        friendsList = findViewById(R.id.friendsList);
        myInfo = findViewById(R.id.myInfo);

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshFriendsList();
            }
        });
        refreshFriendsList();
        showMyUserInfo();
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = friendsSearch.getText().toString();
                controller.addFriend(username).thenAccept(result ->{
                    refreshFriendsList(); //posted the new friend!
                });
                runOnUiThread(() -> {
                    friendsSearch.setText("");
                });
            }
        });


    }

    private void showMyUserInfo() {
        controller.getUser().thenAccept(result -> {
            String userName = "";
            int score = 0;
            try {
                userName = result.getString("username");
                score = result.getInt("score");
            } catch (JSONException e) {
                e.printStackTrace();
                userName = "there's a problem with our systems right now";
                score = Integer.MIN_VALUE;
            }
            int finalScore = score;
            String finalUserName = userName;
            runOnUiThread(() -> {
                if(finalScore != Integer.MIN_VALUE)
                    myInfo.setText(finalUserName + "  "  + "score: " + finalScore);
                else
                    myInfo.setText(finalUserName);
            });
        });
    }

    //get list of friends and show them
    private void refreshFriendsList(){
        runOnUiThread(() -> {
            friendsList.removeAllViews();
        });
        new Thread(()  -> {
            controller.getFriendsConnected().thenAccept(result -> {
                showListOfFriends(result);
            });
        }).start();
    }

    //show the list of friends in the scrollbar in a descending order
    private void showListOfFriends(JSONObject jsonObject){
        try {
            JSONArray connected = jsonObject.getJSONArray("connected");
            JSONArray offline = jsonObject.getJSONArray("offline");
            int sizeCombined = connected.length() + offline.length();
            int connectedIndex = 0, offlineIndex = 0;
            boolean isOnline = false;
            //showing the leaderboards in descending order
            while(connectedIndex + offlineIndex < sizeCombined){
                JSONObject innerJsonObject;
                if(connected.length() > connectedIndex && (offlineIndex >= offline.length() || connected.getJSONObject(connectedIndex).getInt("score") >= offline.getJSONObject(offlineIndex).getInt("score"))){
                    innerJsonObject = connected.getJSONObject(connectedIndex);
                    connectedIndex++;
                    isOnline = true;
                }
                else{
                    innerJsonObject = offline.getJSONObject(offlineIndex);
                    offlineIndex++;
                }
                String username = innerJsonObject.getString("username");
                int place = connectedIndex + offlineIndex;
                int score = innerJsonObject.getInt("score");
                boolean finalIsOnline = isOnline;
                runOnUiThread(() -> {
                    LinearLayout friendBox = new LinearLayout(getApplicationContext());
                    friendBox.setWeightSum(1);
                    TextView friendName = new TextView(getApplicationContext());
                    friendName.setLayoutParams(new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, .5f));
                    TextView scoreView = new TextView(getApplicationContext());
                    scoreView.setLayoutParams(new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, .3f));
                    friendName.setText(String.valueOf(place) + ". " + username);
                    scoreView.setText(Integer.toString(score));
                    if(finalIsOnline)
                        friendBox.setBackgroundColor(Color.GREEN);
                    else
                        friendBox.setBackgroundColor(Color.RED);
                    friendBox.addView(friendName);
                    friendBox.addView(scoreView);
                    friendsList.addView(friendBox);
                });
                isOnline = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //shows a popup with the string given
    public void showPopup(String str) {
        runOnUiThread(() -> {
            Toast.makeText(this,str,Toast.LENGTH_LONG).show();
        });
    }
}