package com.example.rps_api.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User getUserById(String id) {
        // Use jdbcTemplate to execute the query and map the result to the User class
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE user_id = ?", new BeanPropertyRowMapper<User>(User.class),id);
        }catch (EmptyResultDataAccessException e){
            // no such id
            return null;
        }
    }
    public User getUserByUsername(String username){
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE username = ?", new BeanPropertyRowMapper<User>(User.class), username);
        }catch (EmptyResultDataAccessException e){
            // no such id
            return null;
        }
    }

    public List<User> getUsers(){
        String sql = "select * from users";
        try {
            return jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                String id = resultSet.getString("user_id");
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                int score = resultSet.getInt("score");
                User user = new User(id, username, email, score);
                return user;
            });
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public void addUser(String id, String username, String email) {
        jdbcTemplate.update("insert into users (user_id, username, email) VALUES (?,?,?)",id,username,email);
    }

    public void changePoints(String id, int points) {
        jdbcTemplate.update("Update game.users set score = score + ? where user_id = ?",points,id);
    }
}
