package com.br.urlshortener

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HttpClientBuilder {

    inline fun <reified T> createService(
        url: String,
        converterFactory: Converter.Factory = GsonConverterFactory.create(),
    ): T {
        val okHttpClient = OkHttpClient.Builder().apply {
            val logging = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG) {
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                addInterceptor(logging)
            }
        }.build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
            .create(T::class.java)
    }

}