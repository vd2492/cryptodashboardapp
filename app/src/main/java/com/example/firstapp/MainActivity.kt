package com.example.firstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstapp.ui.theme.FirstappTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstapp.network.CoinLoreApi
import com.example.firstapp.model.CoinData
import com.example.firstapp.model.CoinResponse
//import androidx.compose.ui.text.style.Shadow
import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.drawscope.drawBehind
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle

data class CryptoCurrency(
    val symbol: String,
    val name: String,
    val price: Double,
    val percentageChange: Double,
    val marketCap: Double,
    val iconUrl: String = "https://raw.githubusercontent.com/spothq/cryptocurrency-icons/master/128/color/${symbol.toLowerCase()}.png"
)

class CryptoViewModel : ViewModel() {
    private val api: CoinLoreApi = Retrofit.Builder()
        .baseUrl("https://api.coinlore.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create<CoinLoreApi>(CoinLoreApi::class.java)

    var coins by mutableStateOf<List<CoinData>>(emptyList())
    var isLoading by mutableStateOf(true)
    var error by mutableStateOf<String?>(null)
    var selectedFilter by mutableStateOf(CryptoFilter.ALL)

    enum class CryptoFilter {
        ALL, TOP_GAINERS, TOP_LOSERS
    }

    init {
        fetchCoins()
    }

    fun getFilteredCoins(searchQuery: String = ""): List<CoinData> {
        val searchFiltered = coins.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.symbol.contains(searchQuery, ignoreCase = true)
        }
        
        return when (selectedFilter) {
            CryptoFilter.TOP_GAINERS -> searchFiltered.sortedByDescending { it.percent_change_24h }.take(10)
            CryptoFilter.TOP_LOSERS -> searchFiltered.sortedBy { it.percent_change_24h }.take(10)
            CryptoFilter.ALL -> searchFiltered
        }
    }

    fun updateFilter(filter: CryptoFilter) {
        selectedFilter = filter
    }

    private fun fetchCoins() {
        viewModelScope.launch {
            try {
                isLoading = true
                error = null
                val response = api.getCoins()
                coins = response.data
            } catch (e: Exception) {
                error = e.message
            } finally {
                isLoading = false
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirstappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CryptoDashboard()
                }
            }
        }
    }
}

@Composable
fun CryptoDashboard(viewModel: CryptoViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredCoins = viewModel.getFilteredCoins(searchQuery)

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A237E), // Deep blue
            Color(0xFF000051)  // Darker blue
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Watermark
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = "developed by vishruth",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic
                )
            }

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Crypto Dashboard",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                )
            }

            // Glowing Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp)),
                placeholder = { Text("Search cryptocurrencies...", color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color(0xFF64B5F6),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            // Animated Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = viewModel.selectedFilter == CryptoViewModel.CryptoFilter.ALL,
                    onClick = { viewModel.updateFilter(CryptoViewModel.CryptoFilter.ALL) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF3949AB).copy(alpha = 0.3f),
                        labelColor = Color.White,
                        selectedContainerColor = Color(0xFF64B5F6)
                    )
                )
                FilterChip(
                    selected = viewModel.selectedFilter == CryptoViewModel.CryptoFilter.TOP_GAINERS,
                    onClick = { viewModel.updateFilter(CryptoViewModel.CryptoFilter.TOP_GAINERS) },
                    label = { Text("Top Gainers") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF3949AB).copy(alpha = 0.3f),
                        labelColor = Color.White,
                        selectedContainerColor = Color(0xFF64B5F6)
                    )
                )
                FilterChip(
                    selected = viewModel.selectedFilter == CryptoViewModel.CryptoFilter.TOP_LOSERS,
                    onClick = { viewModel.updateFilter(CryptoViewModel.CryptoFilter.TOP_LOSERS) },
                    label = { Text("Top Losers") },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF3949AB).copy(alpha = 0.3f),
                        labelColor = Color.White,
                        selectedContainerColor = Color(0xFF64B5F6)
                    )
                )
            }

            when {
                viewModel.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                viewModel.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${viewModel.error}",
                            color = Color.Red
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredCoins) { coin ->
                            FuturisticCryptoCard(
                                CryptoCurrency(
                                    symbol = coin.symbol,
                                    name = coin.name,
                                    price = coin.price_usd,
                                    percentageChange = coin.percent_change_24h,
                                    marketCap = coin.market_cap_usd
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FuturisticCryptoCard(crypto: CryptoCurrency) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3949AB).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF3949AB).copy(alpha = 0.2f),
                        Color(0xFF303F9F).copy(alpha = 0.3f)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = crypto.iconUrl,
                        contentDescription = "${crypto.name} icon",
                        modifier = Modifier.size(30.dp),
                        error = painterResource(id = R.drawable.ic_crypto_fallback)
                    )
                    Column {
                        Text(
                            text = crypto.symbol,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                        Text(
                            text = crypto.name,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Market Cap: $${formatNumber(crypto.marketCap)}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$${String.format("%.2f", crypto.price)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.1f", crypto.percentageChange)}%",
                        color = if (crypto.percentageChange >= 0) 
                            Color(0xFF00E676) else Color(0xFFFF1744),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatNumber(number: Double): String {
    return when {
        number >= 1_000_000_000 -> String.format("%.1fB", number / 1_000_000_000.0)
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        else -> String.format("%.0f", number)
    }
}