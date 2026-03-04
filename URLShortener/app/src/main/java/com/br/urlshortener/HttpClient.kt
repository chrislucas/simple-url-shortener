package com.br.urlshortener

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.jvm.java
import kotlin.reflect.KClass

class HttpClient private constructor(
    private val url: String,
    private val converterFactory: Converter.Factory = GsonConverterFactory.create(),
    private val isDebug: Boolean,
    private val connectTimeoutInSeconds: Long = 30L,
    private val readTimeoutInSeconds: Long = 30L
) {

    class Builder(
        private var url: String,
        private var converterFactory: Converter.Factory = GsonConverterFactory.create(),
        private var isDebugMode: Boolean = false,
        private var connectTimeoutInSeconds: Long = 30L,
        private var readTimeoutInSeconds: Long = 30L
    ) {

        fun isDebugMode(isDebug: Boolean): Builder {
            return this.apply { this.isDebugMode = isDebug }
        }

        fun withConnectionTimeout(seconds: Long): Builder {
            return this.apply { this.connectTimeoutInSeconds = seconds }
        }

        fun withReadTimeout(seconds: Long): Builder {
            return this.apply { this.readTimeoutInSeconds = seconds }
        }

        fun withConverterFactory(converterFactory: Converter.Factory): Builder {
            return this.apply { this.converterFactory = converterFactory }
        }

        fun build(): HttpClient = HttpClient(
            this.url,
            this.converterFactory,
            this.isDebugMode,
            this.connectTimeoutInSeconds,
            this.readTimeoutInSeconds,
        )
    }

    fun <T : Any> createService(serviceClass: KClass<T>): T {
        val okHttpClient = OkHttpClient.Builder().apply {
            if (isDebug) {
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                addInterceptor(logging)
            }
            connectTimeout(connectTimeoutInSeconds, TimeUnit.SECONDS)
            readTimeout(readTimeoutInSeconds, TimeUnit.SECONDS)
        }.build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
            .create(serviceClass.java)
    }
}
