package com.example.museumapplication.data;


import android.graphics.Bitmap;

public class UserLoggedIn {
    //Singleton
    private static UserLoggedIn instance = new UserLoggedIn();

    private String UID;
    private String providerUID;
    private String name;
    private String email;
    private String photoUrl;
    private Bitmap profilePicture;


    public void setUser(User user) {
        this.UID = user.getUID();
        this.providerUID = user.getProviderUID();
        this.name = user.getDisplayName();
        this.email = user.getEmail();
        this.photoUrl = user.getPhotoURL();
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

    public Bitmap getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Bitmap profilePicture) {
        this.profilePicture = profilePicture;
    }
}
