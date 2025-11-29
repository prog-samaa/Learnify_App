package com.example.learnify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.learnify.viewmodel.UserViewModel
import com.example.learnify.ui.CourseViewModel
import com.example.learnify.ui.components.CourseCard
import com.example.learnify.ui.components.Loading
import com.example.learnify.ui.theme.AppBackgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    courseViewModel: CourseViewModel
) {

    val user = userViewModel.currentUser.value
    val errorMessage by userViewModel.errorMessage

    if (user == null) {
        // لو فيه رسالة خطأ، اعرضيها
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
            // لو مفيش error message
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "User not found",
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------------- PROFILE IMAGE ---------------
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD9C3FF)),
                contentAlignment = Alignment.Center
            ) {
                if (user.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = user.imageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        user.name.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Text(user.email, color = Color.Gray)
            Text(user.phone, color = Color.Gray)

            Spacer(Modifier.height(24.dp))

            // EDIT PROFILE BUTTON
            Button(
                onClick = { navController.navigate("edit_profile") },
                modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Profile")
            }

            Spacer(Modifier.height(24.dp))

            // -------- FAVORITES SECTION ----------
            ProfileCoursesSection(
                title = "Liked Courses",
                icon = Icons.Default.Favorite,
                ids = user.favorites,
                courseViewModel = courseViewModel,
                navController = navController
            )

            Spacer(Modifier.height(20.dp))

            // -------- WATCHLIST SECTION ----------
            ProfileCoursesSection(
                title = "Watch Later",
                icon = Icons.Default.WatchLater,
                ids = user.watchlist,
                courseViewModel = courseViewModel,
                navController = navController
            )

            Spacer(Modifier.height(20.dp))

            // LOGOUT BUTTON
            Button(
                onClick = {
                    userViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Logout", color = Color.White)
            }
        }
    }
}

@Composable
fun ProfileCoursesSection(
    title: String,
    icon: ImageVector,
    ids: List<String>,
    courseViewModel: CourseViewModel,
    navController: NavController
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF6650a4))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            if (ids.isEmpty()) {
                Text("No courses found", color = Color.Gray)
            } else {
                // Load all courses using your VM
                val courses by courseViewModel.getCoursesByIds(ids).observeAsState(emptyList())

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(courses) { course ->

                        CourseCard(
                            course = course,
                            cardWeight = 160,
                            cardHeight = 200,
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
