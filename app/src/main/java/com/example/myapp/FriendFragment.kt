// FriendFragment.kt

package com.example.myapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myapp.models.Friend
import com.example.myapp.models.FriendRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class FriendFragment : Fragment(), InvitationListener {

    private lateinit var friendListLayout: LinearLayout
    private lateinit var friendRequestsLayout: LinearLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var apiService: ApiService
    private var userId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friend, container, false)
        friendListLayout = view.findViewById(R.id.friend_list)
        friendRequestsLayout = view.findViewById(R.id.friend_requests)
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        apiService = RetrofitClient.getClient().create(ApiService::class.java)

        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        Log.d("SharedPreferences", "Retrieved username from preferences: $username")
        if (username.isNullOrEmpty()) {
            Log.w(TAG, "Username not found or empty in SharedPreferences")
            return view
        }

        apiService.getUserIdByUsername(username).enqueue(object : Callback<Long> {
            override fun onResponse(call: Call<Long>, response: Response<Long>) {
                if (response.isSuccessful && response.body() != null) {
                    userId = response.body()!!
                    Log.d(TAG, "Successfully retrieved user ID: $userId")
                    loadFriends(userId)
                    loadFriendRequests(userId)
                } else {
                    val errorMessage = "Failed to get user ID: ${response.message()}"
                    Log.e(TAG, errorMessage)
                    Toast.makeText(requireActivity(), "Failed to get user ID", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Long>, t: Throwable) {
                val errorMessage = "Error: ${t.message}"
                Log.e(TAG, errorMessage, t)
                Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        val addButton = view.findViewById<ImageButton>(R.id.add_button)
        addButton.setOnClickListener {
            val searchBar = view.findViewById<EditText>(R.id.search_bar)
            val username = searchBar.text.toString().trim()
            if (username.isNotEmpty()) {
                sendFriendRequest(username)
            } else {
                Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun refreshData() {
        if (userId != 0L) {
            loadFriends(userId)
            loadFriendRequests(userId)
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun loadFriends(userId: Long) {
        apiService.getFriends(userId).enqueue(object : Callback<List<Friend>> {
            override fun onResponse(call: Call<List<Friend>>, response: Response<List<Friend>>) {
                if (response.isSuccessful && response.body() != null) {
                    displayFriends(response.body()!!)
                } else {
                    val errorMessage = "Failed to load friends list: ${response.message()}"
                    Log.e(TAG, errorMessage)
                    Toast.makeText(requireActivity(), "Failed to load friends list", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Friend>>, t: Throwable) {
                val errorMessage = "Error: ${t.message}"
                Log.e(TAG, errorMessage, t)
                Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadFriendRequests(userId: Long) {
        apiService.getFriendRequests(userId).enqueue(object : Callback<List<FriendRequest>> {
            override fun onResponse(
                call: Call<List<FriendRequest>>,
                response: Response<List<FriendRequest>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    displayFriendRequests(response.body()!!)
                } else {
                    val errorMessage = "Failed to load friend requests: ${response.message()}"
                    Log.e(TAG, errorMessage)
                    Toast.makeText(requireActivity(), "Failed to load friend requests", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<FriendRequest>>, t: Throwable) {
                val errorMessage = "Error: ${t.message}"
                Log.e(TAG, errorMessage, t)
                Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayFriends(friends: List<Friend>) {
        friendListLayout.removeAllViews()
        for (friend in friends) {
            val friendItem = layoutInflater.inflate(R.layout.friend_item, null)
            val friendName = friendItem.findViewById<TextView>(R.id.friend_name)
            val removeButton = friendItem.findViewById<Button>(R.id.remove_button)
            val requestToSessionButton = friendItem.findViewById<Button>(R.id.request_to_session)

            if (friend.friend_id?.username != null) {
                friendName.text = friend.friend_id.username
            } else {
                loadUsernameForFriend(friend, friendName)
            }

            removeButton.setOnClickListener {
                removeFriend(userId, friend.id)
            }

            requestToSessionButton.setOnClickListener {
                sendInvitationToSession(friendName.text.toString())
            }

            friendListLayout.addView(friendItem)
        }
    }

    private fun loadUsernameForFriend(friend: Friend, friendNameTextView: TextView) {
        apiService.getUsernameForFriend(friend.id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val username = response.body()?.string() ?: "Unknown User"
                        friendNameTextView.text = username
                    } catch (e: IOException) {
                        Log.e(TAG, "Error reading response body", e)
                        friendNameTextView.text = "Unknown User"
                    }
                } else {
                    friendNameTextView.text = "Unknown User"
                    Log.e(TAG, "Failed to get username for friend: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                friendNameTextView.text = "Unknown User"
                Log.e(TAG, "Error fetching username for friend", t)
            }
        })
    }

    private fun sendInvitationToSession(receiverUsername: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val senderUsername = sharedPreferences.getString("username", null) ?: ""
        if (senderUsername.isNotEmpty() && receiverUsername.isNotEmpty()) {
            WebSocketClient.sendInvitation(senderUsername, receiverUsername)
            Toast.makeText(requireContext(), "Invitation sent to $receiverUsername", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Invitation sent from $senderUsername to $receiverUsername")
        } else {
            Toast.makeText(requireContext(), "Failed to send invitation", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Failed to send invitation. senderUsername: $senderUsername, receiverUsername: $receiverUsername")
        }
    }

    private fun displayFriendRequests(friendRequests: List<FriendRequest>) {
        friendRequestsLayout.removeAllViews()
        for (request in friendRequests) {
            val requestItem = layoutInflater.inflate(R.layout.friend_request_item, null)
            val requestUserName = requestItem.findViewById<TextView>(R.id.request_user)
            val rejectButton = requestItem.findViewById<Button>(R.id.reject_request)
            val acceptButton = requestItem.findViewById<Button>(R.id.accept_request)

            requestUserName.text = request.sender.username

            rejectButton.setOnClickListener {
                rejectFriendRequest(request.id)
            }

            acceptButton.setOnClickListener {
                acceptFriendRequest(request.id)
            }

            friendRequestsLayout.addView(requestItem)
        }
    }

    private fun removeFriend(userId: Long, friendId: Long) {
        apiService.removeFriend(userId, friendId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireActivity(), "Friend removed", Toast.LENGTH_SHORT).show()
                    loadFriends(userId) // Предполагая, что необходимо обновить список друзей после удаления
                } else {
                    val errorMessage = "Failed to remove friend: ${response.message()}"
                    Log.e(TAG, errorMessage)
                    Toast.makeText(requireActivity(), "Failed to remove friend", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                val errorMessage = "Error: ${t.message}"
                Log.e(TAG, errorMessage, t)
                Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun rejectFriendRequest(requestId: Long) {
        apiService.rejectFriendRequest(requestId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireActivity(), "Friend request rejected", Toast.LENGTH_SHORT).show()
                    loadFriendRequests(userId)
                } else {
                    val errorMessage = "Failed to reject friend request: ${response.message()}"
                    Log.e(TAG, errorMessage)
                    Toast.makeText(requireActivity(), "Failed to reject friend request", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                val errorMessage = "Error: ${t.message}"
                Log.e(TAG, errorMessage, t)
                Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun acceptFriendRequest(requestId: Long) {
        apiService.acceptFriendRequest(requestId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireActivity(), "Friend request accepted", Toast.LENGTH_SHORT).show()
                    loadFriendRequests(userId)
                    loadFriends(userId)
                } else {
                    val errorMessage = "Failed to accept friend request: ${response.message()}"
                    Log.e(TAG, errorMessage)
                    Toast.makeText(requireActivity(), "Failed to accept friend request", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                val errorMessage = "Error: ${t.message}"
                Log.e(TAG, errorMessage, t)
                Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendFriendRequest(username: String) {
        apiService.sendFriendRequest(userId, username).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireActivity(), "Friend request sent", Toast.LENGTH_SHORT).show()
                    loadFriendRequests(userId)
                } else {
                    val errorMessage = "Failed to send friend request: ${response.message()}"
                    Log.e(TAG, errorMessage)
                    Toast.makeText(requireActivity(), "Failed to send friend request", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                val errorMessage = "Error: ${t.message}"
                Log.e(TAG, errorMessage, t)
                Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onInvitationReceived(senderUsername: String) {
        showInvitationDialog(senderUsername)
    }

    private fun sendJoinConfirmationToWebSocket(senderUsername: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val receiverUsername = sharedPreferences.getString("username", null) ?: ""
        if (receiverUsername.isNotEmpty()) {
            WebSocketClient.sendJoinConfirmation(senderUsername, receiverUsername)
        } else {
            Log.e(TAG, "Failed to send join confirmation. Receiver username not found.")
        }
    }

    private fun sendDeclineConfirmationToWebSocket(senderUsername: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val receiverUsername = sharedPreferences.getString("username", null) ?: ""
        if (receiverUsername.isNotEmpty()) {
            WebSocketClient.sendDeclineConfirmation(senderUsername, receiverUsername)
        } else {
            Log.e(TAG, "Failed to send decline confirmation. Receiver username not found.")
        }
    }

    private fun showInvitationDialog(senderUsername: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Invitation Received")
        alertDialogBuilder.setMessage("You received an invitation from $senderUsername. Do you want to join?")

        alertDialogBuilder.setPositiveButton("Join") { dialog, which ->
            sendJoinConfirmationToWebSocket(senderUsername)
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Decline") { dialog, which ->
            sendDeclineConfirmationToWebSocket(senderUsername)
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    companion object {
        private const val TAG = "FriendFragment"
    }
}
