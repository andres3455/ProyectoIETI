package com.ieti.proyectoieti.models;

public class PlaylistInfo {
    private String id;
    private String name;
    private String spotifyUrl;
    private Integer followers;
    private String imageUrl;

    public PlaylistInfo() {
    }

    public PlaylistInfo(String id, String name, String spotifyUrl, Integer followers, String imageUrl) {
        this.id = id;
        this.name = name;
        this.spotifyUrl = spotifyUrl;
        this.followers = followers;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpotifyUrl() {
        return spotifyUrl;
    }

    public void setSpotifyUrl(String spotifyUrl) {
        this.spotifyUrl = spotifyUrl;
    }

    public Integer getFollowers() {
        return followers;
    }

    public void setFollowers(Integer followers) {
        this.followers = followers;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}