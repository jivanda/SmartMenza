package com.example.smartmenza.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitInstance {

    private const val BASE_URL_LOCAL = "http://10.0.2.2:5246/"
    private const val BASE_URL_PROD =
        "https://smartmenza-h5csfahadafnajaq.swedencentral-01.azurewebsites.net/"

    // True lokalno, false Azure
    private const val USE_LOCAL = true

    private val BASE_URL = if (USE_LOCAL) BASE_URL_LOCAL else BASE_URL_PROD

    private fun logging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    private fun getOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logging())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    private fun getUnsafeOkHttpClient(): OkHttpClient {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) { }

                override fun checkServerTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) { }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        return OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(logging())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getClient(): OkHttpClient {
        val isLocal = BASE_URL.contains("10.0.2.2") || BASE_URL.contains("localhost")
        val isHttps = BASE_URL.startsWith("https://")
        return if (isLocal && isHttps) getUnsafeOkHttpClient() else getOkHttpClient()
    }

    val api: SmartMenzaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(getClient())
            .build()
            .create(SmartMenzaApi::class.java)
    }
}