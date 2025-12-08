package com.payfunds.wallet.modules.balance.tron

import android.util.Log
import com.google.gson.Gson
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.balance.tron.tron_balance_response.TronBalanceResponse
import io.horizontalsystems.marketkit.models.BlockchainType
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TronBalanceService {

    private fun getTronWalletAddress(): String? {
        val tronWallet: Wallet? = App.walletManager.activeWallets.firstOrNull {
            it.token.blockchainType == BlockchainType.Tron
        }
        val adapter = tronWallet?.let { wallet ->
            App.adapterManager.getReceiveAdapterForWallet(wallet)
        }
        return adapter?.receiveAddress
    }


    suspend fun fetchTronAccountData(): TronBalanceResponse? {

        val tronWalletAddress = getTronWalletAddress()

        val url = "https://apilist.tronscanapi.com/api/accountv2?address=$tronWalletAddress"
        val client = OkHttpClient()

        return suspendCancellableCoroutine { continuation ->
            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val jsonString = response.body?.string()
                    if (jsonString != null) {
                        try {
                            val parsedResponse = Gson().fromJson(jsonString, TronBalanceResponse::class.java)
                            continuation.resume(parsedResponse)
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    } else {
                        continuation.resume(null)
                    }
                }
            })
        }
    }
}