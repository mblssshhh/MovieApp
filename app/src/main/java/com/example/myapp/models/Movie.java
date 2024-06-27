package com.example.myapp.models;

public class Movie {
    public String title;
    public String posterUrl;
    public String rating;
    public String description;

    public Movie(String title, String posterUrl, String rating, String description) {
        this.title = title;
        this.posterUrl = posterUrl;
        this.rating = rating;
        this.description = description;
    }
}
