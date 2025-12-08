package com.payfunds.wallet.network

import com.payfunds.wallet.core.App
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object PayFundRetrofitInstance {


    private val BASE_URL = App.appConfigProvider.backendBaseUrl
    private val SWAP_BASE_URL = "https://bb04746c9fe2.ngrok-free.app/api/v1/swap/"

    val payFundApi: PayFundApiService by lazy {
        providePayfundApiService(provideRetrofit(provideOkHttpClient()))
    }

    val payFundSwapApi: PayFundApiService by lazy {
        providePayfundApiService(provideSwapRetrofit(provideOkHttpClient()))
    }

    private fun providePayfundApiService(retrofit: Retrofit): PayFundApiService =
        retrofit.create(PayFundApiService::class.java)

    private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun provideSwapRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(SWAP_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}