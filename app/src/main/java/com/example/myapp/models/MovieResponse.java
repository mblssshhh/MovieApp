package com.example.myapp.models;

import com.google.gson.annotations.SerializedName;

public class MovieResponse {

    @SerializedName("Title")
    private String title;

    @SerializedName("Poster")
    private String posterUrl;

    @SerializedName("imdbRating")
    private String rating;

    @SerializedName("Plot")
    private String description;

    public MovieResponse(String title, String posterUrl, String rating, String description) {
        this.title = title;
        this.posterUrl = posterUrl;
        this.rating = rating;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }
}

