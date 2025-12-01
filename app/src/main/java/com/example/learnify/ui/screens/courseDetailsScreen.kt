package com.example.learnify.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.learnify.ui.CourseViewModel
import com.example.learnify.ui.theme.PrimaryColor
import androidx.compose.runtime.rememberCoroutineScope
import com.example.learnify.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun courseDetailsScreen(
    courseId: String?,
    navController: NavHostController,
    viewModel: CourseViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    //  استخدام StateFlow
    val currentCourse by viewModel.currentCourse.collectAsState()

    //  استخدام LaunchedEffect لإعادة تحميل الكورس
    LaunchedEffect(key1 = courseId) {
        courseId?.let { viewModel.loadCourse(it) }
    }

    currentCourse?.let { course ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEBEBF8))
                .padding(16.dp)
        ) {
            AsyncImage(
                model = course.imageUrl,
                contentDescription = course.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(2f),
                    color = Color.Black
                )

                // Like Button
                IconButton(onClick = {
                    coroutineScope.launch {
                        val newFavoriteState = !course.isFavorite

                        viewModel.toggleFavorite(course.id, newFavoriteState)

                        // تحديث في Firebase User
                        if (newFavoriteState) {
                            userViewModel.addToFavorites(course.id)
                            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                        } else {
                            userViewModel.removeFromFavorites(course.id)
                            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = if (course.isFavorite) Color.Red else Color.Gray
                    )
                }

                // Watch Later Button
                IconButton(onClick = {
                    coroutineScope.launch {
                        val newWatchLaterState = !course.isWatchLater

                        viewModel.toggleWatchLater(course.id, newWatchLaterState)

                        // تحديث في Firebase User
                        if (newWatchLaterState) {
                            userViewModel.addToWatchlist(course.id)
                            Toast.makeText(context, "Added to watchlist", Toast.LENGTH_SHORT).show()
                        } else {
                            userViewModel.removeFromWatchlist(course.id)
                            Toast.makeText(context, "Removed from watchlist", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.WatchLater,
                        contentDescription = "Watch Later",
                        tint = if (course.isWatchLater) Color.Blue else Color.Gray
                    )
                }

                IconButton(onClick = {
                    coroutineScope.launch {
                        val newDoneState = !course.isDone
                        viewModel.toggleDone(course.id, newDoneState)

                        if (newDoneState) {
                            userViewModel.addToDoneCourses(course.id)
                            Toast.makeText(context, "Marked as completed", Toast.LENGTH_SHORT).show()
                        } else {
                            userViewModel.removeFromDoneCourses(course.id)
                            Toast.makeText(context, "Removed from completed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.CheckBox,
                        contentDescription = "Mark as Done",
                        tint = if (course.isDone) Color(0xFF4CAF50) else Color.Gray
                    )
                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "By ${course.channelTitle}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = course.description,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=${course.id}"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = Color.White
                )
            ) {
                Text("Watch on YouTube")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    } ?: run {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEBEBF8)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = PrimaryColor)
                Text(
                    text = "Loading course...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}