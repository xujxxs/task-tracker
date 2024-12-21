package io.tasks_tracker.profile.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/profile")
public class ProfileController 
{
    @GetMapping
    public String getProfile()
    {
        //TODO: process POST request
        return new String();
    }
    
    @PutMapping
    public String updateProfile(@RequestBody String entity) 
    {
        //TODO: process POST request
        return entity;
    }

    @DeleteMapping
    public void deleteProfile()
    {
        //TODO: process POST request

    }
}
