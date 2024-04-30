package com.RPS.leaderboards;

import com.RPS.utilities.DataBaseCommunication;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

public class LeaderBoardsController {

    final LeaderBoardsDatabase leaderBoardsDatabase;
    final LeaderBoardsNetwork networkCommunication;
    final LeaderBoardsActivity activity;
    final FirebaseAuth mAuth;

    public LeaderBoardsController(LeaderBoardsActivity activity){
        this.leaderBoardsDatabase = new LeaderBoardsDatabase(activity.getApplicationContext(), DataBaseCommunication.getInstance(activity).getQueue());
        this.networkCommunication = new LeaderBoardsNetwork(this);
        this.activity = activity;
        this.mAuth = FirebaseAuth.getInstance();
    }

    //get list of friends of given id that are connected and disconnected
    public CompletableFuture<JSONObject> getFriendsConnected(){
        CompletableFuture<JSONObject> future = new CompletableFuture<>();
        new Thread(() -> {
            leaderBoardsDatabase.findFriends(mAuth.getUid())
                    .thenApply(result -> { // when findFriends completes
                        return networkCommunication.getFriends(result); // the new completefuture
                    }).whenComplete((response,exception) -> { //when getFriends completes
                        if(exception != null){
                            future.completeExceptionally(new Throwable("error"));
                        }
                        else{
                            future.complete(response.join());
                        }
                    });
        }).start();
        return future;
    }

    //add username to be a friend of the client
    public CompletableFuture<Void> addFriend(String username){
        CompletableFuture<Void> future = new CompletableFuture<>();
        new Thread(() -> {
            leaderBoardsDatabase.getUserWithUserName(username).thenAccept(result -> {
                try {
                    if(result.length() == 0)
                        activity.showPopup("user doesn't exist!");
                    else
                        leaderBoardsDatabase.addFriend(mAuth.getUid(), result.getString("user_id"));
                }catch (Exception e){ e.printStackTrace(); }
            });
            future.complete(null);
        }).start();
        return future;
    }

    public CompletableFuture<JSONObject> getUser(){
        return leaderBoardsDatabase.getUserWithID(mAuth.getUid());
    }

    // inviting a user with username given
    public void inviteUser(String username) {
        leaderBoardsDatabase.getUserWithUserName(username).thenAcceptAsync(user -> {
            try {
                networkCommunication.inviteUser(user.getString("user_id"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    //deletes from friends the user with the username given
    public void deleteUserWithUsername(String username) {
        leaderBoardsDatabase.getUserWithUserName(username).thenAccept(res -> {
            try {
                leaderBoardsDatabase.deleteUserFriend(mAuth.getUid(),res.getString("user_id"));
                leaderBoardsDatabase.deleteUserFriend(res.getString("user_id"),mAuth.getUid());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    /* returns JsonArray with User info of every player in the requests */
    public CompletableFuture<JSONArray> getFriendsRequests() {
        CompletableFuture<JSONArray> response = leaderBoardsDatabase.getFriendsRequests(mAuth.getUid());
        response.thenAccept(res -> {
            System.out.println("sex");
        });
        System.out.println(response);
        return response;
    }
}
