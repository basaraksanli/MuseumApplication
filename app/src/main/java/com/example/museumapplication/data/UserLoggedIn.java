package com.example.museumapplication.data;


public class UserLoggedIn {
    //Singleton
    private static UserLoggedIn instance = new UserLoggedIn();

    private String UID;
    private String providerUID;
    private String name;
    private String email;
    private String photoUrl;


    public void setUser(String UID,String providerUID, String email, String name, String photoUrl) {
        this.UID = UID;
        this.providerUID = providerUID;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public static UserLoggedIn getInstance() {
        return instance;
    }

    public String getProviderUID() {
        return providerUID;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
