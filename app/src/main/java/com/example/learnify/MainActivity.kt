package com.example.learnify

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.learnify.ui.screens.*
import com.example.learnify.ui.theme.LearnifyTheme
import com.example.learnify.ui.CourseViewModel
import com.example.learnify.ui.components.BottomNavigation
import com.example.learnify.ui.theme.AppBackgroundColor
import com.example.learnify.viewmodel.UserViewModel
import com.example.learnify.ui.TimerViewModel
import com.example.learnify.ui.screens.PomodoroScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LearnifyTheme {

                val navController = rememberNavController()
                var selected by remember { mutableStateOf<String?>(null) }

                // ViewModels
                val userViewModel: UserViewModel = viewModel()
                val courseViewModel: CourseViewModel = viewModel()
                val timerViewModel: TimerViewModel = viewModel()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val currentUser = FirebaseAuth.getInstance().currentUser

                val showBottomBar = currentUser != null &&
                        currentRoute !in listOf("login", "signup", "forgot", "courseDetails/{courseId}", "edit_profile")

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppBackgroundColor),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigation(
                                navController = navController,
                                onHomeClicked = { selected = null }
                            )
                        }
                    }
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = if (currentUser == null) "login" else "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        composable("login") { LoginScreen(navController, userViewModel) }
                        composable("signup") { SignUpScreen(navController, userViewModel) }
                        composable("forgot") { ForgotPasswordScreen(navController, userViewModel) }

                        composable("home") {
                            HomeScreen(
                                selected = selected,
                                onSelect = { selected = it },
                                onHomeClicked = { selected = null },
                                navController = navController
                            )
                        }

                        // ***** TIMER SCREEN *****
                        composable("pomodoro") {
                            PomodoroScreen(
                                navController = navController,
                                viewModel = timerViewModel
                            )
                        }

                        composable("todo") { ToDoScreen() }

                        composable("you") {
                            YouScreen(
                                navController = navController,
                                userViewModel = userViewModel,
                                courseViewModel = courseViewModel
                            )
                        }

                        composable("edit_profile") {
                            EditProfileScreen(navController, userViewModel)
                        }

                        composable(
                            route = "courseDetails/{courseId}",
                            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                            courseDetailsScreen(
                                courseId = courseId,
                                navController = navController,
                                viewModel = courseViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
