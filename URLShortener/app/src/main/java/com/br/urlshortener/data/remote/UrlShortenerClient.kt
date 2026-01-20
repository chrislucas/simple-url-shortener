package com.br.urlshortener.data.remote

import com.br.urlshortener.data.remote.model.UrlShortenerDTO
import com.br.urlshortener.data.remote.model.UrlDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UrlShortenerClient {

    @POST("api/alias")
    suspend fun postUrl(@Body urlShortenerDTO: UrlShortenerDTO): Response<UrlDTO>

    @GET("api/alias/:id")
    suspend fun getUrlShortener(@Path("id") id: String): Response<UrlShortenerDTO>

}