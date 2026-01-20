package com.br.urlshortener

import io.mockk.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class HttpClientBuilderTest {

    interface DummyService

    private var originalDebug: Boolean = BuildConfig.DEBUG

    @BeforeEach
    fun setUp() {
        unmockkAll()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        restoreBuildConfigDebug()
    }

    private fun setBuildConfigDebug(value: Boolean) {
        try {
            val field = BuildConfig::class.java.getDeclaredField("DEBUG")
            field.isAccessible = true
            field.setBoolean(null, value)
        } catch (e: Exception) {
            throw RuntimeException("Unable to set BuildConfig.DEBUG via reflection", e)
        }
    }

    private fun restoreBuildConfigDebug() {
        try {
            val field = BuildConfig::class.java.getDeclaredField("DEBUG")
            field.isAccessible = true
            field.setBoolean(null, originalDebug)
        } catch (e: Exception) {
            // best-effort restore; if it fails, let tests fail so developer can fix environment
        }
    }

    @Test
    fun `createService should build retrofit and create service`() {
        setBuildConfigDebug(false)

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

        val service = HttpClientBuilder.createService<DummyService>("http://example.com/")

        assertSame(mockService, service)
        verify { anyConstructed<Retrofit.Builder>().baseUrl("http://example.com/") }
        verify { anyConstructed<Retrofit.Builder>().addConverterFactory(ofType<Converter.Factory>()) }
        verify { anyConstructed<Retrofit.Builder>().build() }
        verify { mockRetrofit.create(DummyService::class.java) }
    }

    @Test
    fun `createService should add logging interceptor when debug true`() {
        setBuildConfigDebug(true)

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

        val service = HttpClientBuilder.createService<DummyService>("http://example.com/")
        assertSame(mockService, service)

        verify { anyConstructed<HttpLoggingInterceptor>().setLevel(HttpLoggingInterceptor.Level.BODY) }
        verify { anyConstructed<OkHttpClient.Builder>().addInterceptor(ofType<HttpLoggingInterceptor>()) }
    }

    @Test
    fun `createService should not add logging interceptor when debug false`() {
        setBuildConfigDebug(false)

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

        val service = HttpClientBuilder.createService<DummyService>("http://example.com/")
        assertSame(mockService, service)

        verify(exactly = 0) { anyConstructed<OkHttpClient.Builder>().addInterceptor(any<Interceptor>()) }
    }
}