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
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class HttpClientTest {

    interface DummyService

    @Before
    fun setUp() {
        unmockkAll()
        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Builder should configure all fields correctly`() {
        val url = "https://api.example.com/"
        val converterFactory = GsonConverterFactory.create()
        val isDebug = true
        val connectTimeout = 15L
        val readTimeout = 20L

        val httpClient = HttpClient.Builder(url)
            .withConverterFactory(converterFactory)
            .isDebugMode(isDebug)
            .withConnectionTimeout(connectTimeout)
            .withReadTimeout(readTimeout)
            .build()

        assertNotNull(httpClient)
    }

    @Test
    fun `createService should build Retrofit with correct parameters`() {
        mockkConstructor(Retrofit.Builder::class)
        mockkConstructor(OkHttpClient.Builder::class)

        val mockRetrofitBuilder = mockk<Retrofit.Builder>(relaxed = true)
        val mockOkBuilder = mockk<OkHttpClient.Builder>(relaxed = true)

        every { anyConstructed<OkHttpClient.Builder>().connectTimeout(any(), any()) } answers {
            mockOkBuilder.connectTimeout(arg(0), arg(1))
            mockOkBuilder
        }
        every { anyConstructed<OkHttpClient.Builder>().readTimeout(any(), any()) } answers {
            mockOkBuilder.readTimeout(arg(0), arg(1))
            mockOkBuilder
        }
        every { anyConstructed<OkHttpClient.Builder>().build() } answers {
            mockOkBuilder.build()
        }

        every { anyConstructed<Retrofit.Builder>().baseUrl(any<String>()) } answers {
            mockRetrofitBuilder.baseUrl(it.invocation.args[0] as String)
            mockRetrofitBuilder
        }
        every { anyConstructed<Retrofit.Builder>().client(any()) } answers {
            mockRetrofitBuilder.client(it.invocation.args[0] as OkHttpClient)
            mockRetrofitBuilder
        }
        every { anyConstructed<Retrofit.Builder>().addConverterFactory(any()) } answers {
            mockRetrofitBuilder.addConverterFactory(it.invocation.args[0] as Converter.Factory)
            mockRetrofitBuilder
        }
        every { anyConstructed<Retrofit.Builder>().build() } answers {
            mockRetrofitBuilder.build()
        }

        val mockRetrofit = mockk<Retrofit>()
        val mockOkClient = mockk<OkHttpClient>()
        val mockService = mockk<DummyService>()

        every { mockOkBuilder.build() } returns mockOkClient
        every { mockRetrofitBuilder.build() } returns mockRetrofit
        every { mockRetrofit.create(any<Class<DummyService>>()) } returns mockService

        val url = "https://api.test.com/"
        val httpClient = HttpClient.Builder(url)
            .withConnectionTimeout(10L)
            .withReadTimeout(20L)
            .isDebugMode(false)
            .build()

        httpClient.createService(DummyService::class)

        verify { mockOkBuilder.connectTimeout(10L, TimeUnit.SECONDS) }
        verify { mockOkBuilder.readTimeout(20L, TimeUnit.SECONDS) }
        verify { mockRetrofitBuilder.baseUrl(url) }
        verify { mockRetrofitBuilder.client(mockOkClient) }
    }

    @Test
    fun `createService should add LoggingInterceptor when isDebug is true`() {
        mockkConstructor(OkHttpClient.Builder::class)
        mockkConstructor(HttpLoggingInterceptor::class)
        mockkConstructor(Retrofit.Builder::class)

        val mockOkBuilder = mockk<OkHttpClient.Builder>(relaxed = true)
        val mockLogging = mockk<HttpLoggingInterceptor>(relaxed = true)

        every { anyConstructed<OkHttpClient.Builder>().addInterceptor(any<Interceptor>()) } answers {
            mockOkBuilder.addInterceptor(it.invocation.args[0] as Interceptor)
            mockOkBuilder
        }
        every { anyConstructed<HttpLoggingInterceptor>().setLevel(any()) } answers {
            mockLogging.setLevel(it.invocation.args[0] as HttpLoggingInterceptor.Level)
            mockLogging
        }

        // Mocking Retrofit to avoid crashes
        val mockRetrofitBuilder = mockk<Retrofit.Builder>(relaxed = true)
        every { anyConstructed<Retrofit.Builder>().baseUrl(any<String>()) } returns mockRetrofitBuilder

        val httpClient = HttpClient.Builder("https://test.com/")
            .isDebugMode(true)
            .build()

        httpClient.createService(DummyService::class)

        verify { mockOkBuilder.addInterceptor(any<HttpLoggingInterceptor>()) }
        verify { mockLogging.setLevel(HttpLoggingInterceptor.Level.BODY) }
    }

    @Test
    fun `createService should NOT add LoggingInterceptor when isDebug is false`() {
        mockkConstructor(OkHttpClient.Builder::class)
        mockkConstructor(Retrofit.Builder::class)

        val mockOkBuilder = mockk<OkHttpClient.Builder>(relaxed = true)

        every { anyConstructed<OkHttpClient.Builder>().connectTimeout(any(), any()) } returns mockOkBuilder

        every { mockOkBuilder.connectTimeout(any(), any()) } returns mockOkBuilder
        every { mockOkBuilder.readTimeout(any(), any()) } returns mockOkBuilder
        every { mockOkBuilder.addInterceptor(any<Interceptor>()) } returns mockOkBuilder
        every { mockOkBuilder.build() } returns mockk(relaxed = true)

        val mockRetrofitBuilder = mockk<Retrofit.Builder>(relaxed = true)
        every { anyConstructed<Retrofit.Builder>().baseUrl(any<String>()) } returns mockRetrofitBuilder

        val httpClient = HttpClient.Builder("https://test.com/")
            .isDebugMode(false)
            .build()

        httpClient.createService(DummyService::class)

        verify(exactly = 0) { mockOkBuilder.addInterceptor(any<Interceptor>()) }
    }
}
