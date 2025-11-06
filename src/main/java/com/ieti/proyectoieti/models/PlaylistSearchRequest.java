package com.ieti.proyectoieti.models;

public class PlaylistSearchRequest {
    private String genre;
    private String artist;
    private Integer limit;

    public PlaylistSearchRequest() {
        this.limit = 10; // default limit
    }

    public PlaylistSearchRequest(String genre, String artist, Integer limit) {
        this.genre = genre;
        this.artist = artist;
        this.limit = limit != null ? limit : 10;
    }

    // Getters and Setters
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
