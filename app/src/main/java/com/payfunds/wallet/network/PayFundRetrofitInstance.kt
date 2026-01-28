package com.payfunds.wallet.network

import com.payfunds.wallet.core.App
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object PayFundRetrofitInstance {


    private val BASE_URL = App.appConfigProvider.backendBaseUrl
    private val HOLO_BANK_BASE_URL = "http://51.112.97.203:5009/api/v1/"

    val payFundApi: PayFundApiService by lazy {
        providePayfundApiService(provideRetrofit(provideOkHttpClient()))
    }

    val holoBankApi: PayFundApiService by lazy {
        providePayfundApiService(provideHoloBankRetrofit(provideOkHttpClient()))
    }

    private fun providePayfundApiService(retrofit: Retrofit): PayFundApiService =
        retrofit.create(PayFundApiService::class.java)

    private fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun provideHoloBankRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(HOLO_BANK_BASE_URL)
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