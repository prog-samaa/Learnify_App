package com.example.learnify.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learnify.ui.theme.PrimaryColor
import com.example.learnify.viewmodel.UserViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: UserViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val loggedIn by viewModel.isLoggedIn
    LaunchedEffect(Unit) {
        viewModel.errorMessage.value = null
    }


    LaunchedEffect(loggedIn) {
        if (loggedIn) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 130.dp),   // ← مساحة فوق زي الصورة
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ---------- Title ----------
        Text(
            text = "Log in",
            fontSize = 45.sp,                               // ← أكبر
            color = Color.Black,
            fontWeight = FontWeight.Bold                   // ← بولد
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Enter your email and password\nAccess your account securely.",
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ---------- Email ----------
        OutlinedTextField(
            value = email,
            singleLine = true ,
            onValueChange = { email = it },
            placeholder = { Text("Email address") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray)
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)

        )

        Spacer(modifier = Modifier.height(15.dp))

        // ---------- Password ----------
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            placeholder = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Forgot password - centered
        Text(
            "Forgot Password?",
            color = PrimaryColor,
            fontSize = 13.sp,              // ← أصغر
            modifier = Modifier
                .clickable { navController.navigate("forgot") }
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(25.dp))

        // ---------- Login Button ----------
        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            interactionSource = remember { MutableInteractionSource() },
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 2.dp,
                pressedElevation = 6.dp
            )
        ) {
            Text("Login", fontSize = 18.sp, color = Color.White)
        }


        Spacer(modifier = Modifier.height(20.dp))

        // ---------- Create Account ----------
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don’t have an account? ",
                fontSize = 13.sp
            )
            Text(
                text = "Sign Up here",
                color = PrimaryColor,
                fontSize = 13.sp ,
                modifier = Modifier.clickable {
                    navController.navigate("signup")
                }
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Error message
        viewModel.errorMessage.value?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
