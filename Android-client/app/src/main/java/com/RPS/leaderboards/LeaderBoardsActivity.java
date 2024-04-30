package com.RPS.leaderboards;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.RPS.firebaselogin.R;
import com.RPS.utilities.InvitationSingleton;
import com.google.android.material.navigation.NavigationView;

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
    DrawerLayout drawerLayout;
    InvitationSingleton invitationSingleton;

    private GestureDetector gestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        invitationSingleton = InvitationSingleton.getInstance();

        controller = new LeaderBoardsController(this);

        refreshBtn = findViewById(R.id.refreshBtn);
        addFriendBtn = findViewById(R.id.searchBtn);
        friendsSearch = findViewById(R.id.usernameInput);
        friendsList = findViewById(R.id.friendsList);
        myInfo = findViewById(R.id.myInfo);
        drawerLayout = findViewById(R.id.drawer);

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshFriendsList();
            }
        });
        refreshFriendsList();
        showMyUserInfo();
        addFriendBtn.setOnClickListener(view -> {
            String username = friendsSearch.getText().toString();
            controller.addFriend(username).thenAccept(result ->{
                refreshFriendsList(); //posted the new friend!
            });
            runOnUiThread(() -> {
                friendsSearch.setText("");
            });
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(@NonNull MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(@NonNull MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onScroll(@Nullable MotionEvent motionEvent, @NonNull MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float v, float v1) {
                final int MIN_VELOCITY = 200;
                final int MIN_DISTANCE = 100;
                final int MAX_OFF_DISTANCE = 500;
                if(e1.getX() + MIN_DISTANCE < e2.getX() && v > MIN_VELOCITY && Math.abs(e2.getY() - e1.getY()) < MAX_OFF_DISTANCE) {
                    drawerLayout.open();
                    drawerLayout.setElevation(20);
                    return true;
                }
                else if(e1.getX() + MIN_DISTANCE > e2.getX() && Math.abs(v) > MIN_VELOCITY) {
                    drawerLayout.close();
                    drawerLayout.setElevation(-10);
                    return true;
                }
                return false;
            }
        });
        loadSideBar();
        invitationSingleton.setParams(this,this);
    }

    // loads the sidebar with all info needed
    private void loadSideBar(){
        controller.getFriendsRequests().thenAccept(res -> {
            NavigationView navigationView = findViewById(R.id.navigationViewOfDrawer);
            System.out.println(res);
            for(int i = 0; i < res.length();i++) {
                try {
                    String username = res.getJSONObject(i).getString("username");
                    String friend_id = res.getJSONObject(i).getString("user_id");
                    MenuItem menuItem = navigationView.getMenu().add(Menu.NONE,Menu.NONE,Menu.NONE,username);
                    menuItem.setEnabled(false);
                    View actionView = LayoutInflater.from(this).inflate(R.layout.friends_menu_item, null);
                    Button acceptBtn = actionView.findViewById(R.id.acceptFriendBtn);
                    Button rejectBtn = actionView.findViewById(R.id.rejectFriendBtn);
                    acceptBtn.setBackgroundColor(Color.parseColor("#2a737b"));
                    acceptBtn.setOnClickListener(v -> {
                        controller.addFriend(username);
                        actionView.setVisibility(View.GONE);
                        menuItem.setVisible(false);
                        runOnUiThread(() -> {
                            Toast.makeText(this,username + " added!",Toast.LENGTH_SHORT).show();
                        });
                    });
                    rejectBtn.setOnClickListener(v -> {
                        actionView.setVisibility(View.GONE);
                        controller.deleteUserWithUsername(username);
                        runOnUiThread(() -> {
                            Toast.makeText(this, username + " rejected!", Toast.LENGTH_SHORT).show();
                        });
                    });
                    menuItem.setActionView(actionView);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //shows my info player
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
    @SuppressLint("SetTextI18n")
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
                final int HEIGHT_SIZE_ROW = 100;
                runOnUiThread(() -> {
                    LinearLayout friendBox = new LinearLayout(getApplicationContext());
                    friendBox.setWeightSum(1);
                    friendBox.setOrientation(LinearLayout.HORIZONTAL);
                    friendBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,HEIGHT_SIZE_ROW));
                    friendBox.setGravity(Gravity.CENTER_VERTICAL);
                    TextView friendName = new TextView(getApplicationContext());
                    friendName.setLayoutParams(new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, .5f));
                    TextView scoreView = new TextView(getApplicationContext());
                    scoreView.setLayoutParams(new TableLayout.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, .2f));
                    friendName.setText(String.valueOf(place) + ". " + username);
                    scoreView.setText(Integer.toString(score));
                    friendBox.setOnLongClickListener(v -> {
                        PopupMenu popupMenu = new PopupMenu(getApplicationContext(),v);
                        popupMenu.inflate(R.menu.popping_menu_long_clicked_user);
                        popupMenu.setOnMenuItemClickListener(item -> {
                            switch (item.getItemId()){
                                case R.id.option1:
                                    controller.inviteUser(username);
                                    return true;
                                case R.id.option2:
                                    controller.deleteUserWithUsername(username);
                                    return true;
                                default:
                                    return false;
                            }
                        });
                        popupMenu.show();
                        return false;
                    });
                    if(finalIsOnline) {
                        friendBox.setBackgroundColor(Color.GREEN);
                    }
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

    @Override
    public boolean dispatchTouchEvent (MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }


}