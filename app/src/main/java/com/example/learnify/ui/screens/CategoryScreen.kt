package com.example.learnify.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.learnify.R
import com.example.learnify.ui.components.CourseGridScreen
import com.example.learnify.ui.components.CourseRowScreen
import com.example.learnify.ui.components.LearnifySearchBar
import com.example.learnify.ui.theme.PrimaryColor
import com.example.learnify.ui.theme.Light_Brown

@Composable
fun CategoryScreen(
    CategoryName: String,
    QueryGrid: String,
    TrendingChannelId: String,
    navController: NavHostController
) {
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.28f)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Light_Brown)
                .align(Alignment.TopCenter)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = CategoryName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryColor,
                            fontFamily = FontFamily(Font(R.font.playwrite)),
                            modifier = Modifier.weight(1f)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.category_screen_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth(0.08f)
                                .aspectRatio(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LearnifySearchBar(
                        onSearch = { query ->
                            searchQuery = query
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Trending",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor,
                        fontFamily = FontFamily(Font(R.font.playwrite)),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            item {
                CourseRowScreen(
                    query = TrendingChannelId,
                    isTrending = true,
                    navController = navController
                )
            }

            item {
                CourseGridScreen(
                    query = if (searchQuery.isBlank()) QueryGrid else searchQuery,
                    isSearch = searchQuery.isNotBlank(),
                    navController = navController
                )
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
