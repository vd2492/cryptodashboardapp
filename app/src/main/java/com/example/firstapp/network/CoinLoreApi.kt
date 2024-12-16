package com.example.firstapp.network

import com.example.firstapp.model.CoinResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinLoreApi {
    @GET("api/tickers/")
    suspend fun getCoins(
        @Query("start") start: Int = 0,
        @Query("limit") limit: Int = 100
    ): CoinResponse
} 