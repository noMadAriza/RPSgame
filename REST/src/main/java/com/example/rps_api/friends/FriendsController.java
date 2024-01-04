package com.example.rps_api.friends;

import com.example.rps_api.users.User;
import com.example.rps_api.users.UserService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
public class FriendsController {

    private final FriendsService friendsService;

    @Autowired
    public FriendsController(FriendsService friendsService){
        this.friendsService = friendsService;
    }

    @PostMapping ("/add/{user_id}")
    public void addFriend(@PathVariable String user_id, @RequestParam String other_id){
        friendsService.addFriend(user_id,other_id);
    }

    @GetMapping("/{user_id}")
    public List<User> getFriends(@PathVariable String user_id){
        return friendsService.getFriends(user_id);
    }


}
