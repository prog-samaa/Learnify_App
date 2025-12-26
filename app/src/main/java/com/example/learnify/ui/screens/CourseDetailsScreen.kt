package com.example.learnify.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.learnify.ui.theme.AppBackgroundColor
import com.example.learnify.ui.theme.PrimaryColor
import com.example.learnify.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.learnify.R
import com.example.learnify.ui.viewModels.CourseViewModel

@Composable
fun CourseDetailsScreen(
    courseId: String?,
    navController: NavHostController,
    viewModel: CourseViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentCourse by viewModel.currentCourse.collectAsState()

    LaunchedEffect(courseId) {
        courseId?.let { viewModel.loadCourse(it) }
    }

    if (currentCourse == null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.internet_error_icon),
                contentDescription = "No Tasks Image",
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Error...",
                fontSize = 20.sp,
                color = Color.Gray,
                fontFamily = FontFamily(Font(R.font.playwrite))
            )
        }
    } else {
        currentCourse?.let { course ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(AppBackgroundColor)
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                val newFavoriteState = !course.isFavorite
                                viewModel.toggleFavorite(course.id, newFavoriteState)
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
                                tint = if (course.isFavorite) Color.Red else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(onClick = {
                            coroutineScope.launch {
                                val newWatchLaterState = !course.isWatchLater
                                viewModel.toggleWatchLater(course.id, newWatchLaterState)
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
                                tint = if (course.isWatchLater) Color(0xFF7232BE) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
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
                                tint = if (course.isDone) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "By ${course.channelTitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = " ${course.publishedAt}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/playlist?list=${course.id}")
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Watch on YouTube")
                }
            }
        }
    }
}
