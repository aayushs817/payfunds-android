package com.payfunds.wallet.network

import com.payfunds.wallet.core.App
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

object PayFundWebSocketHandler {

    private val BASE_URL_COINAPI = "wss://ws.coinapi.io/v1/apikey-${App.appConfigProvider.coinApiKey}"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    fun connect(onMessage: (String) -> Unit, onError: (String) -> Unit) {
        val request = Request.Builder()
            .url(BASE_URL_COINAPI)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket connected!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onError(t.localizedMessage ?: "WebSocket Error")
            }
        })
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "Closing Connection")
    }
}
