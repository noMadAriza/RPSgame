package com.example.rps_api.users;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController implements ErrorController {
    private final UserService UserService;

    @Autowired
    public UserController(UserService userService) {
        this.UserService = userService;
    }

    //get user info with his id
    @GetMapping("/{id}")
    public User getUserWithID(@PathVariable("id") String id){
        return UserService.getUserById(id);
    }

    //get user info with his username
    @GetMapping("/username/{username}")
    public User getUserWithUsername(@PathVariable("username") String username){
        return UserService.getUserByUsername(username);
    }

    //add a user to the DB
    @PostMapping
    public void addUser(@RequestParam("id") String id,@RequestParam("username") String username,@RequestParam("email") String email){
        UserService.addUser(id,username,email);
    }

    //change the points of the user in the path add\decrease as the parameter it has in points
    @PostMapping("/{id}/points")
    public void changePoints(@PathVariable("id") String id,@RequestParam("points") String points){
        UserService.changePoints(id,Integer.parseInt(points));
    }

}
