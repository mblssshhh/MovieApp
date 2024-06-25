package com.example.myapp

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketClient public constructor() {

    private var webSocket: WebSocket? = null
    private var invitationListener: InvitationListener? = null

    init {
        val client = OkHttpClient.Builder()
            .readTimeout(3, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("ws://192.168.68.114:8080/ws/websocket")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket Opened")
                val connectMessage =
                    "CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\u0000"
                webSocket.send(connectMessage)

                subscribeToTopic("/topic/invitations")
                subscribeToTopic("/topic/sessions")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                processWebSocketMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket Closed $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket Error: ${t.message}")
            }
        })
    }

    private fun subscribeToTopic(topic: String) {
        val subscriptionMessage =
            "SUBSCRIBE\nid:sub-${System.currentTimeMillis()}\ndestination:$topic\n\n\u0000"
        Log.d(TAG, "Sending subscription message: $subscriptionMessage")
        webSocket?.send(subscriptionMessage)
        Log.d(TAG, "Subscribed to topic: $topic")
    }

    fun registerInvitationListener(listener: InvitationListener) {
        invitationListener = listener
        Log.d(TAG, "InvitationListener registered: $listener")
    }

    private fun processWebSocketMessage(text: String) {
        Log.d(TAG, "Received message from WebSocket: $text")
        val gson = Gson()
        try {
            val stompCommand = text.substringBefore("\n")
            if (stompCommand == "MESSAGE") {
                val body = text.substringAfter("\n\n").substringBefore("\u0000")
                val invitationMessage = gson.fromJson(body, InvitationMessage::class.java)
                Log.d(
                    TAG,
                    "Invitation received from ${invitationMessage.sender} to ${invitationMessage.receiver}"
                )
                if (text.contains("/topic/invitations")) {
                    Log.d(TAG, "Received invitation from ${invitationMessage.sender} to ${invitationMessage.receiver}")
                    invitationListener?.onInvitationReceived(invitationMessage.sender)
                } else if (text.contains("/topic/declinedInvitations")) {
                    Log.d(TAG, "Received declined invitation from ${invitationMessage.sender} to ${invitationMessage.receiver}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse message: ${e.message}")
        }
    }


    fun setInvitationListener(listener: InvitationListener) {
        invitationListener = listener
    }

    fun disconnect() {
        webSocket?.cancel()
        webSocket = null
    }

    companion object {
        private const val TAG = "WebSocketClient"
        private var instance: WebSocketClient? = null

        @Synchronized
        fun getInstance(): WebSocketClient {
            if (instance == null) {
                instance = WebSocketClient()
            }
            return instance!!
        }

        fun sendInvitation(sender: String, receiver: String) {
            val message = InvitationMessage(sender, receiver)
            val gson = Gson()
            val jsonMessage = gson.toJson(message)

            val stompMessage =
                "SEND\ndestination:/app/invitation\ncontent-type:application/json\ncontent-length:${jsonMessage.length}\n\n$jsonMessage\u0000"

            Log.d(TAG, "Sending invitation message: $stompMessage")

            try {
                val result = getInstance().webSocket?.send(stompMessage)
                Log.d(TAG, "Invitation message send result: $result")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send invitation message: ${e.message}")
            }
        }

        fun sendJoinConfirmation(sender: String, receiver: String) {
            val confirmationMessage = SessionMessage(sender, receiver)
            val gson = Gson()
            val jsonMessage = gson.toJson(confirmationMessage)
            val stompMessage =
                "SEND\ndestination:/app/joinConfirmation\ncontent-type:application/json\ncontent-length:${jsonMessage.length}\n\n$jsonMessage\u0000"
            Log.d(TAG, "Sending join confirmation message: $stompMessage")
            val result = getInstance().webSocket?.send(stompMessage)
            Log.d(TAG, "Join confirmation message send result: $result")
        }

        fun sendDeclineConfirmation(sender: String, receiver: String) {
            val confirmationMessage = SessionMessage(sender, receiver)
            val gson = Gson()
            val jsonMessage = gson.toJson(confirmationMessage)
            val stompMessage =
                "SEND\ndestination:/app/declineConfirmation\ncontent-type:application/json\ncontent-length:${jsonMessage.length}\n\n$jsonMessage\u0000"
            Log.d(TAG, "Sending decline confirmation message: $stompMessage")
            val result = getInstance().webSocket?.send(stompMessage)
            Log.d(TAG, "Decline confirmation message send result: $result")
        }
    }
}

data class InvitationMessage(val sender: String, val receiver: String)
data class SessionMessage(val sender: String, val receiver: String)

interface InvitationListener {
    fun onInvitationReceived(senderUsername: String)
}
