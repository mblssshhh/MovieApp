package com.example.myapp;

import com.example.myapp.models.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OMDbService {

    @GET("/")
    Call<MovieResponse> getMovie(
            @Query("t") String title,
            @Query("apikey") String apiKey
    );
}
