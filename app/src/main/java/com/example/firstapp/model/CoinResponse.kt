package com.example.firstapp.model

data class CoinResponse(
    val data: List<CoinData>,
    val info: Info
)

data class CoinData(
    val id: String,
    val symbol: String,
    val name: String,
    val price_usd: Double,
    val percent_change_24h: Double,
    val market_cap_usd: Double
)

data class Info(
    val coins_num: Int,
    val time: Long
) 