package com.example.rps_api.users;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.beans.ConstructorProperties;

public class User {
    @JsonProperty("user_id")
    private String user_id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("email")
    private String email;
    @JsonProperty("score")
    private int score;

    public User(){

    }
    public User(String user_id,String username,String email,int score){
        setUser_id(user_id);
        setUsername(username);
        setEmail(email);
        setScore(score);
    }
    public void setScore(int score) {
        this.score = score;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
