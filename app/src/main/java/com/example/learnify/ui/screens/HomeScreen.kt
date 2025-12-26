package com.example.learnify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.learnify.R
import com.example.learnify.ui.components.CategoryButton
import com.example.learnify.ui.components.CourseRowScreen
import com.example.learnify.ui.components.LearnifyHeader
import com.example.learnify.ui.theme.PrimaryColor

@Composable
fun HomeScreen(
    selected: String? ,
    onSelect: (String) -> Unit,
    onHomeClicked: () -> Unit,
    navController: NavHostController
) {
    val programmingTrendingChannelId = "UC8butISFwT-Wl7EV0hUK0BQ"
    val medicalTrendingChannelId = "UCNI0qOojpkhsUtaQ4_2NUhQ"
    val engineeringTrendingChannelId = "UClqhvGmHcvWL9w3R48t9QXQ"
    val marketingTrendingChannelId = "UCaAx1xeTgF3rs4rBPDq6-Kw"
    val languageTrendingChannelId = "UCu8Lth4FT5HxaP0nypE-gTQ"
    val humanDevelopmentTrendingChannelId = "UCtYzVCmNxrshH4_bPO_-Y-A"

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {

        if (selected == null) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
            ) {
                LearnifyHeader(
                    onSearch = { searchQuery = it }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.08f)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categories",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor,
                    fontFamily = FontFamily(Font(R.font.playwrite)),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f)
            ) {
                CategoryButton(
                    selected = selected,
                    onSelect = onSelect
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.43f)
            ) {
                CourseRowScreen(
                    query = if (searchQuery.isBlank()) "Courses" else searchQuery,
                    isSearch = searchQuery.isNotBlank(),
                    navController = navController
                )
            }

        } else {
            when (selected) {
                "Programming" -> CategoryScreen(
                    CategoryName = "Programming Core",
                    QueryGrid = "programming courses",
                    TrendingChannelId = programmingTrendingChannelId,
                    navController = navController
                )

                "Engineering" -> CategoryScreen(
                    CategoryName = "Engineering Core",
                    QueryGrid = "Engineering courses",
                    TrendingChannelId = engineeringTrendingChannelId,
                    navController = navController
                )

                "Medical" -> CategoryScreen(
                    CategoryName = "Medical Core",
                    QueryGrid = "medical courses",
                    TrendingChannelId = medicalTrendingChannelId,
                    navController = navController
                )

                "Marketing" -> CategoryScreen(
                    CategoryName = "Marketing Core",
                    QueryGrid = "marketing courses",
                    TrendingChannelId = marketingTrendingChannelId,
                    navController = navController
                )

                "Language" -> CategoryScreen(
                    CategoryName = "Language Core",
                    QueryGrid = "language courses",
                    TrendingChannelId = languageTrendingChannelId,
                    navController = navController
                )

                "Human Development" -> CategoryScreen(
                    CategoryName = "Human Development Core",
                    QueryGrid = "Human Development Courses",
                    TrendingChannelId = humanDevelopmentTrendingChannelId,
                    navController = navController
                )
            }
        }
    }
}
