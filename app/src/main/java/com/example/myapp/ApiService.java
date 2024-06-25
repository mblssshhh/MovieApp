package com.example.myapp;
import com.example.myapp.models.Friend;
import com.example.myapp.models.FriendRequest;
import com.example.myapp.models.User;
import com.example.myapp.models.UserResponse;


import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("register")
    Call<UserResponse> registerUser(@Body User user);

    @GET("/api/friends/{userId}")
    Call<List<Friend>> getFriends(@Path("userId") Long userId);

    @GET("/api/friends/requests/{userId}")
    Call<List<FriendRequest>> getFriendRequests(@Path("userId") Long userId);

    @GET("users/get_id")
    Call<Long> getUserIdByUsername(@Query("username") String username);

    @GET("/api/friends/user/{userId}")
    Call<User> getFriendById(@Path("userId") Long userId);

    @DELETE("/api/friends/remove/{userId}/{friendId}")
    Call<Void> removeFriend(@Path("userId") Long userId, @Path("friendId") Long friendId);

    @DELETE("/api/friends/requests/reject/{requestId}")
    Call<Void> rejectFriendRequest(@Path("requestId") Long requestId);

    @POST("/api/friends/requests/accept/{requestId}")
    Call<Void> acceptFriendRequest(@Path("requestId") Long requestId);

    @GET("/api/users/search")
    Call<List<User>> searchUsers(@Query("query") String query);

    @POST("/api/friends/send-request")
    Call<Void> sendFriendRequest(@Query("userId") Long userId, @Query("username") String username);

    @GET("/api/friends/username/{friendId}")
    Call<ResponseBody> getUsernameForFriend(@Path("friendId") Long friendId);
}
