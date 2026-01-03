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
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape


@Composable
fun CourseDetailsScreen(
    courseId: String?,
    navController: NavHostController,
    viewModel: CourseViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val courseDetails by viewModel.getCourseById(courseId ?:
    "").observeAsState()

    LaunchedEffect(courseId) {
        courseId?.let { viewModel.loadCourse(it) }
    }

    if (courseDetails == null) {
        Box(
            modifier =
                Modifier.fillMaxSize().background(AppBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment =
                Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PrimaryColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading course details...", color = Color.Gray)
            }
        }
    } else {
        courseDetails?.let { course ->
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
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement =
                        Arrangement.spacedBy(8.dp)) {

                        IconButton(onClick = {
                            coroutineScope.launch {
                                val newState = !course.isFavorite
                                viewModel.toggleFavorite(course.id,
                                    newState)
                                if (newState)
                                    userViewModel.addToFavorites(course.id)
                                else
                                    userViewModel.removeFromFavorites(course.id)

                                Toast.makeText(context, if(newState)
                                    "Added to favorites" else "Removed", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                tint = if (course.isFavorite)
                                    Color.Red else Color.Gray.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(onClick = {
                            coroutineScope.launch {
                                val newState = !course.isWatchLater
                                viewModel.toggleWatchLater(course.id,
                                    newState)
                                if (newState)
                                    userViewModel.addToWatchlist(course.id)
                                else
                                    userViewModel.removeFromWatchlist(course.id)

                                Toast.makeText(context, if(newState)
                                    "Added to Watchlist" else "Removed", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector =
                                    Icons.Default.WatchLater,
                                contentDescription = "Watch Later",
                                tint = if (course.isWatchLater)
                                    Color(0xFF7232BE) else Color.Gray.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(onClick = {
                            coroutineScope.launch {
                                val newState = !course.isDone
                                viewModel.toggleDone(course.id,
                                    newState)
                                if (newState)
                                    userViewModel.addToDoneCourses(course.id)
                                else
                                    userViewModel.removeFromDoneCourses(course.id)

                                Toast.makeText(context, if(newState)
                                    "Course Completed!" else "Removed from Done",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.CheckBox,
                                contentDescription = "Done",
                                tint = if (course.isDone)
                                    Color(0xFF4CAF50) else Color.Gray.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ليصافت سروكلا
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Channel: ${course.channelTitle}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PrimaryColor,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Published: ${course.publishedAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/playlist?list=${course.id}"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors =
                        ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start Learning on YouTube", fontWeight =
                        FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}