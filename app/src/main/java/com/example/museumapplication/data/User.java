package com.example.museumapplication.data;

public class User {
    private String email;
    private String name;

    public User(String EmailAddress, String Name) {
        email = EmailAddress;
        this.name = Name;
    }

    public String getStrEmailAddress() {
        return email;
    }

    public String getName() {
        return name;
    }

}
