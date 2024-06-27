package com.example.myapp

import InvitationListener
import WebSocketClient
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.myapp.models.Movie

class HomeFragment : Fragment(R.layout.fragment_home), InvitationListener {

    private lateinit var startSessionButton: Button
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var sharedPreferences: SharedPreferences
    private var senderUsername: String = ""
    private var receiverUsername: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webSocketClient = WebSocketClient.getInstance()
        webSocketClient.registerInvitationListener(this)

        startSessionButton = view.findViewById(R.id.startSessionButton)
        startSessionButton.setOnClickListener {
            startSession()
        }

        sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        senderUsername = sharedPreferences.getString("username", null) ?: ""
    }

    private fun startSession() {
        if (senderUsername.isNotEmpty() && receiverUsername.isNotEmpty()) {
            WebSocketClient.sendStartSession(senderUsername, receiverUsername)
            Log.d(TAG, "Start session request sent from $senderUsername to $receiverUsername")
        } else {
            Log.e(TAG, "Failed to start session. Usernames not found.")
        }
    }

    private fun openSessionActivity(senderUsername: String, receiverUsername: String) {

        val intent = Intent(requireActivity(), SessionActivity::class.java).apply {
            putExtra("SENDER", senderUsername)
            putExtra("RECEIVER", receiverUsername)
        }
        startActivity(intent)
    }
    override fun onInvitationReceived(senderUsername: String) {

    }

    override fun onSessionCreate(senderUsername: String, receiverUsername: String) {
        Log.d(TAG, "recievername: $receiverUsername")
        this.receiverUsername = receiverUsername
    }

    override fun onSessionStart(senderUsername: String, receiverUsername: String) {
        Log.d(ContentValues.TAG, "Users to session: $senderUsername, $receiverUsername")
        openSessionActivity(senderUsername, receiverUsername)
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}
