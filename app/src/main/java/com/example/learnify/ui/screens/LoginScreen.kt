package com.example.learnify.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learnify.ui.theme.Green
import com.example.learnify.ui.theme.PrimaryColor
import com.example.learnify.viewmodel.UserViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: UserViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val loggedIn by viewModel.isLoggedIn
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.errorMessage.value = null
    }

    LaunchedEffect(loggedIn) {
        if (loggedIn) {
            showSuccessMessage = true
            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(500)
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return if (email.isEmpty()) {
            emailError = "Email is required"
            false
        } else if (!emailRegex.matches(email)) {
            emailError = "Invalid email format"
            false
        } else {
            emailError = ""
            true
        }
    }

    fun validatePassword(password: String): Boolean {
        return if (password.isEmpty()) {
            passwordError = "Password is required"
            false
        } else {
            passwordError = ""
            true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 130.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Log in",
            fontSize = 45.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Enter your email and password\nAccess your account securely.",
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            singleLine = true,
            onValueChange = {
                email = it
                if (emailError.isNotEmpty()) validateEmail(it)
            },
            placeholder = { Text("Email address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(50),
            isError = emailError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(55.dp)
        )
        if (emailError.isNotEmpty()) {
            Text(
                text = emailError,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (passwordError.isNotEmpty()) validatePassword(it)
            },
            singleLine = true,
            placeholder = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(50),
            isError = passwordError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(55.dp)
        )
        if (passwordError.isNotEmpty()) {
            Text(
                text = passwordError,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            "Forgot Password?",
            color = PrimaryColor,
            fontSize = 13.sp,
            modifier = Modifier
                .clickable { navController.navigate("forgot") }
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                val isEmailValid = validateEmail(email)
                val isPasswordValid = validatePassword(password)
                if (isEmailValid && isPasswordValid) {
                    viewModel.login(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            interactionSource = remember { MutableInteractionSource() },
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
        ) {
            Text("Login", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Don't have an account? ", fontSize = 13.sp)
            Text(
                text = "Sign Up here",
                color = PrimaryColor,
                fontSize = 13.sp,
                modifier = Modifier.clickable { navController.navigate("signup") }
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        if (showSuccessMessage) {
            Text(
                text = "Login successful!",
                color = Green,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        viewModel.errorMessage.value?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp
            )
        }
    }
}
