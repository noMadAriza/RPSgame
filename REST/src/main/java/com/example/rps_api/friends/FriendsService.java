package com.example.rps_api.friends;


import com.example.rps_api.users.User;
import com.example.rps_api.users.UserService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendsService {

    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;

    public FriendsService(JdbcTemplate jdbcTemplate,UserService userService){
        this.jdbcTemplate = jdbcTemplate;
        this.userService = userService;
    }
    public void addFriend(String user_id, String other_id) {
        jdbcTemplate.update("INSERT INTO friendships (user_id, friend_id) VALUES (?,?)",user_id,other_id);
    }


    public List<User> getFriends(String user_id) {
        String sql = "select friend_id from friendships where user_id = ?" +
                "AND friend_id in (SELECT user_id from friendships where friend_id = ?)";
        List<String> list = jdbcTemplate.queryForList(sql, String.class,user_id,user_id);
        List<User> users = list.stream().map(userId -> userService.getUserById(userId)).collect(Collectors.toList());
        return users;
    }

    public boolean deleteFriend(String id, String friendId) {
        String sql = "delete from friendships where user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql,id,friendId);
        return true;
    }
}
