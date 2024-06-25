package com.example.myapp.models;

public class User {
    public Long id;
    public String username;
    public String email;
    public String password;

    public User(String username, String email, String password) {
        this(username, email, password, null);
    }

    public User(String username, String email, String password, Long id) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;

    }
}

