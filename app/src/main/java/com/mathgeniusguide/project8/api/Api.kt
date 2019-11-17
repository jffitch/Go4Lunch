package com.mathgeniusguide.project8.api

import com.mathgeniusguide.project8.connectivity.ConnectivityInterceptor
import com.mathgeniusguide.project8.responses.details.DetailsResponse
import com.mathgeniusguide.project8.responses.place.PlaceResponse
import com.mathgeniusguide.project8.util.Constants
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {
    @GET("nearbysearch/json")
    suspend fun getPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String): Response<PlaceResponse>

    @GET("details/json")
    suspend fun getDetails(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String): Response<DetailsResponse>

    companion object {
        operator fun invoke(
            connectivityInterceptor: ConnectivityInterceptor
        ): Api {
            val requestInterceptor = Interceptor { chain ->

                val url = chain.request()
                    .url()
                    .newBuilder()
                    .addQueryParameter("key", Constants.API_KEY)
                    .build()
                val request = chain.request()
                    .newBuilder()
                    .url(url)
                    .build()

                return@Interceptor chain.proceed(request)
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(requestInterceptor)
                .addInterceptor(connectivityInterceptor)
                .build()

            val userMoshi = Moshi
                .Builder()
                .build()

            return Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(userMoshi))
                .build()
                .create(Api::class.java)
        }
    }
}