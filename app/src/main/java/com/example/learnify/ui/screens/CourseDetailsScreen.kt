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
import androidx.compose.ui.unit.sp
import com.example.learnify.R
import com.example.learnify.ui.viewModels.CourseViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
@Composable
fun CourseDetailsScreen(
    courseId: String?,
    navController: NavHostController,
    viewModel: CourseViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val courseDetails by viewModel.getCourseById(courseId ?: "").observeAsState()

    LaunchedEffect(courseId) {
        courseId?.let { viewModel.loadCourse(it) }
    }

    if (courseDetails == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.internet_error_icon),
                    contentDescription = null,
                    modifier = Modifier.size(220.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Error ...",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily(Font(R.font.playwrite))
                )
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
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                val newState = !course.isFavorite
                                viewModel.toggleFavorite(course.id, newState)
                                if (newState) userViewModel.addToFavorites(course.id)
                                else userViewModel.removeFromFavorites(course.id)
                                Toast.makeText(
                                    context,
                                    if (newState) "Added to favorites" else "Removed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (course.isFavorite) Color.Red
                                else Color.Gray.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(onClick = {
                            coroutineScope.launch {
                                val newState = !course.isWatchLater
                                viewModel.toggleWatchLater(course.id, newState)
                                if (newState) userViewModel.addToWatchlist(course.id)
                                else userViewModel.removeFromWatchlist(course.id)
                                Toast.makeText(
                                    context,
                                    if (newState) "Added to Watchlist" else "Removed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.WatchLater,
                                contentDescription = null,
                                tint = if (course.isWatchLater) Color(0xFF7232BE)
                                else Color.Gray.copy(alpha = 0.6f)
                            )
                        }

                        IconButton(onClick = {
                            coroutineScope.launch {
                                val newState = !course.isDone
                                viewModel.toggleDone(course.id, newState)
                                if (newState) userViewModel.addToDoneCourses(course.id)
                                else userViewModel.removeFromDoneCourses(course.id)
                                Toast.makeText(
                                    context,
                                    if (newState) "Course Completed!" else "Removed from Done",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.CheckBox,
                                contentDescription = null,
                                tint = if (course.isDone) Color(0xFF4CAF50)
                                else Color.Gray.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = course.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "by .. ${course.channelTitle}",
                            color = PrimaryColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = course.publishedAt,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                var expanded by remember { mutableStateOf(false) }
                var hasOverflow by remember { mutableStateOf(false) }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Description",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (course.description.isNullOrBlank()) "No description"
                            else course.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = if (expanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis,
                            onTextLayout = { textLayoutResult ->
                                if (!expanded) hasOverflow = textLayoutResult.hasVisualOverflow
                            }
                        )
                        if (hasOverflow) {
                            TextButton(onClick = { expanded = !expanded }) {
                                Text(if (expanded) "Show less" else "Read more")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/playlist?list=${course.id}")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Start Learning on YouTube",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
