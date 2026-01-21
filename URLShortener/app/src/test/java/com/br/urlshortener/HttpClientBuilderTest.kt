package com.br.urlshortener

import android.util.Log
import io.mockk.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Converter
import retrofit2.Retrofit

class HttpClientBuilderTest {

    interface DummyService

    @Before
    fun setUp() {
        unmockkAll()
        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false
        every { Log.println(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `createService should build retrofit and create service`() {
        mockkConstructor(Retrofit.Builder::class)
        val mockBuilder = mockk<Retrofit.Builder>(relaxed = true)
        val mockRetrofit = mockk<Retrofit>(relaxed = true)
        val mockService = mockk<DummyService>()

        every { mockBuilder.baseUrl(any<String>()) } returns mockBuilder
        every { mockBuilder.client(any<OkHttpClient>()) } returns mockBuilder
        every { mockBuilder.addConverterFactory(any<Converter.Factory>()) } returns mockBuilder
        every { mockBuilder.build() } returns mockRetrofit
        every { mockRetrofit.create(DummyService::class.java) } returns mockService

        every { anyConstructed<Retrofit.Builder>().baseUrl(any<String>()) } returns mockBuilder
        every { anyConstructed<Retrofit.Builder>().client(any<OkHttpClient>()) } returns mockBuilder
        every { anyConstructed<Retrofit.Builder>().addConverterFactory(any<Converter.Factory>()) } returns mockBuilder
        every { anyConstructed<Retrofit.Builder>().build() } returns mockRetrofit

        val service = HttpClientBuilder.createService<DummyService>(
            url = "http://example.com/",
            isDebug = false,
        )

        assertSame(mockService, service)
        verify { anyConstructed<Retrofit.Builder>().baseUrl("http://example.com/") }
        verify { anyConstructed<Retrofit.Builder>().build() }
        verify { mockRetrofit.create(DummyService::class.java) }
    }

    @Test
    fun `createService should add logging interceptor when debug true`() {
        mockkConstructor(OkHttpClient.Builder::class)
        mockkConstructor(HttpLoggingInterceptor::class)
        val mockOkBuilder = mockk<OkHttpClient.Builder>(relaxed = true)
        val mockOkClient = mockk<OkHttpClient>(relaxed = true)

        every { mockOkBuilder.addInterceptor(any<Interceptor>()) } returns mockOkBuilder
        every { mockOkBuilder.build() } returns mockOkClient

        every { anyConstructed<OkHttpClient.Builder>().addInterceptor(any<Interceptor>()) } returns mockOkBuilder
        every { anyConstructed<OkHttpClient.Builder>().build() } returns mockOkClient

        every {
            anyConstructed<HttpLoggingInterceptor>().setLevel(HttpLoggingInterceptor.Level.BODY)
        } returns mockk()

        // Retrofit builder mocked to complete the flow and return a service
        mockkConstructor(Retrofit.Builder::class)
        val mockRetrofitBuilder = mockk<Retrofit.Builder>(relaxed = true)
        val mockRetrofit = mockk<Retrofit>(relaxed = true)
        val mockService = mockk<DummyService>()
        every { mockRetrofitBuilder.baseUrl(any<String>()) } returns mockRetrofitBuilder
        every { mockRetrofitBuilder.client(any<OkHttpClient>()) } returns mockRetrofitBuilder
        every { mockRetrofitBuilder.addConverterFactory(any<Converter.Factory>()) } returns mockRetrofitBuilder
        every { mockRetrofitBuilder.build() } returns mockRetrofit
        every { mockRetrofit.create(DummyService::class.java) } returns mockService

        every { anyConstructed<Retrofit.Builder>().baseUrl(any<String>()) } returns mockRetrofitBuilder
        every { anyConstructed<Retrofit.Builder>().client(any<OkHttpClient>()) } returns mockRetrofitBuilder
        every { anyConstructed<Retrofit.Builder>().addConverterFactory(any<Converter.Factory>()) } returns mockRetrofitBuilder
        every { anyConstructed<Retrofit.Builder>().build() } returns mockRetrofit

        val service = HttpClientBuilder.createService<DummyService>(
            url = "http://example.com/",
            isDebug = true,
        )
        assertSame(mockService, service)

        verify { anyConstructed<HttpLoggingInterceptor>().setLevel(HttpLoggingInterceptor.Level.BODY) }
        verify { anyConstructed<OkHttpClient.Builder>().addInterceptor(ofType<HttpLoggingInterceptor>()) }
    }

    @Test
    fun `createService should not add logging interceptor when debug false`() {
        mockkConstructor(OkHttpClient.Builder::class)
        val mockOkBuilder = mockk<OkHttpClient.Builder>(relaxed = true)
        val mockOkClient = mockk<OkHttpClient>(relaxed = true)

        every { mockOkBuilder.addInterceptor(any<Interceptor>()) } returns mockOkBuilder
        every { mockOkBuilder.build() } returns mockOkClient

        every { anyConstructed<OkHttpClient.Builder>().addInterceptor(any<Interceptor>()) } returns mockOkBuilder
        every { anyConstructed<OkHttpClient.Builder>().build() } returns mockOkClient

        // Retrofit builder mocked to complete the flow and return a service
        mockkConstructor(Retrofit.Builder::class)
        val mockRetrofitBuilder = mockk<Retrofit.Builder>(relaxed = true)
        val mockRetrofit = mockk<Retrofit>(relaxed = true)
        val mockService = mockk<DummyService>()
        every { mockRetrofitBuilder.baseUrl(any<String>()) } returns mockRetrofitBuilder
        every { mockRetrofitBuilder.client(any<OkHttpClient>()) } returns mockRetrofitBuilder
        every { mockRetrofitBuilder.addConverterFactory(any<Converter.Factory>()) } returns mockRetrofitBuilder
        every { mockRetrofitBuilder.build() } returns mockRetrofit
        every { mockRetrofit.create(DummyService::class.java) } returns mockService

        every { anyConstructed<Retrofit.Builder>().baseUrl(any<String>()) } returns mockRetrofitBuilder
        every { anyConstructed<Retrofit.Builder>().client(any<OkHttpClient>()) } returns mockRetrofitBuilder
        every { anyConstructed<Retrofit.Builder>().addConverterFactory(any<Converter.Factory>()) } returns mockRetrofitBuilder
        every { anyConstructed<Retrofit.Builder>().build() } returns mockRetrofit

        val service = HttpClientBuilder.createService<DummyService>(
            url = "http://example.com/",
            isDebug = false,
        )
        assertSame(mockService, service)

        verify(exactly = 0) { anyConstructed<OkHttpClient.Builder>().addInterceptor(any<Interceptor>()) }
    }
}
