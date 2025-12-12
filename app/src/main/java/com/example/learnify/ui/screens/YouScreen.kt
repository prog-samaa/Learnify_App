package com.example.learnify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.learnify.data.local.CourseEntity
import com.example.learnify.viewmodel.UserViewModel
import com.example.learnify.ui.CourseViewModel
import com.example.learnify.ui.components.CourseCard
import com.example.learnify.ui.theme.AppBackgroundColor
import com.example.learnify.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    courseViewModel: CourseViewModel
) {
    val user by userViewModel.currentUser
    val errorMessage by userViewModel.errorMessage
    val favoriteCourses by courseViewModel.favoriteCourses.observeAsState(emptyList())
    val watchLaterCourses by courseViewModel.watchLaterCourses.observeAsState(emptyList())
    val doneCourses by courseViewModel.doneCourses.observeAsState(emptyList())
    val scrollState = rememberScrollState()

    LaunchedEffect(user?.uid) {
        if (user != null) {
            courseViewModel.initializeFavorites()
        }
    }

    if (user == null) {
        if (!errorMessage.isNullOrEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error: $errorMessage",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = PrimaryColor)
                    Text(
                        text = "Loading user data...",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(AppBackgroundColor)
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7D5260)),
                    contentAlignment = Alignment.Center
                ) {
                    if (user!!.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user!!.imageUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(

                            user!!.name.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(user!!.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Text(user!!.email, color = Color.Gray)
                Text(user!!.phone, color = Color.Gray)

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { navController.navigate("edit_profile") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                ) {
                    Text("Edit Profile", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(24.dp))

                ProfileCoursesSection(
                    title = "Liked Courses",
                    icon = Icons.Default.Favorite,
                    courses = favoriteCourses,
                    navController = navController,
                    emptyMessage = "No favorite courses yet"
                )

                Spacer(Modifier.height(20.dp))

                ProfileCoursesSection(
                    title = "Watch Later",
                    icon = Icons.Default.WatchLater,
                    courses = watchLaterCourses,
                    navController = navController,
                    emptyMessage = "No courses in watch later"
                )

                Spacer(Modifier.height(20.dp))

                ProfileCoursesSection(
                    title = "Completed Courses",
                    icon = Icons.Default.CheckBox,
                    courses = doneCourses,
                    navController = navController,
                    emptyMessage = "No completed courses yet"
                )

                Spacer(Modifier.height(20.dp))

                ProfileStatsSection(
                    favoriteCount = favoriteCourses.size,
                    watchLaterCount = watchLaterCourses.size,
                    doneCount = doneCourses.size
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        userViewModel.logout()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        "Logout",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun ProfileCoursesSection(
    title: String,
    icon: ImageVector,
    courses: List<CourseEntity>,
    navController: NavController,
    emptyMessage: String = "No courses found"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = PrimaryColor)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "(${courses.size})",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(12.dp))

            if (courses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyMessage,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(courses, key = { it.id }) { course ->
                        CourseCard(
                            course = course,
                            cardWeight = 245,
                            cardHeight = 300,
                            onCourseClick = { selectedCourse ->
                                navController.navigate("courseDetails/${selectedCourse.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStatsSection(
    favoriteCount: Int,
    watchLaterCount: Int,
    doneCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Learning Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    count = favoriteCount,
                    label = "Favorites",
                    color = Color(0xFFFF6B6B)
                )

                StatItem(
                    count = watchLaterCount,
                    label = "Watch Later",
                    color = Color(0xFF4ECDC4)
                )
                StatItem(
                    count = doneCount,
                    label = "Completed",
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    count = favoriteCount + watchLaterCount + doneCount,
                    label = "Total",
                    color = Color(0xFFFFD93D)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}
