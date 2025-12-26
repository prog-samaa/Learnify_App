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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.learnify.ui.theme.Green
import com.example.learnify.ui.theme.PrimaryColor
import com.example.learnify.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun SignUpScreen(navController: NavController, viewModel: UserViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.value = null
        viewModel.isSuccess.value = false
    }

    val context = LocalContext.current

    fun validateEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return if (email.isEmpty()) {
            emailError = "Email is required"
            false
        } else if (!emailRegex.matches(email)) {
            emailError = "Invalid email format (e.g., user@example.com)"
            false
        } else {
            emailError = ""
            true
        }
    }

    fun validatePhone(phone: String): Boolean {
        return if (phone.isEmpty()) {
            phoneError = "Phone number is required"
            false
        } else if (!phone.startsWith("01")) {
            phoneError = "Phone must start with 01"
            false
        } else if (phone.length != 11) {
            phoneError = "Phone must be exactly 11 digits"
            false
        } else if (!phone.all { it.isDigit() }) {
            phoneError = "Phone must contain only numbers"
            false
        } else {
            phoneError = ""
            true
        }
    }

    fun validatePassword(password: String): Boolean {
        return when {
            password.isEmpty() -> {
                passwordError = "Password is required"
                false
            }
            password.length < 8 -> {
                passwordError = "Password must be at least 8 characters"
                false
            }
            !password.any { it.isUpperCase() } -> {
                passwordError = "Password must contain at least one uppercase letter"
                false
            }
            !password.any { it.isDigit() } -> {
                passwordError = "Password must contain at least one number"
                false
            }
            !password.any { !it.isLetterOrDigit() } -> {
                passwordError = "Password must contain at least one special character"
                false
            }
            else -> {
                passwordError = ""
                true
            }
        }
    }

    fun validateName(name: String): Boolean {
        return if (name.isEmpty()) {
            nameError = "Name is required"
            false
        } else if (name.length < 2) {
            nameError = "Name must be at least 2 characters"
            false
        } else {
            nameError = ""
            true
        }
    }

    fun validateAll(): Boolean {
        val isNameValid = validateName(name)
        val isEmailValid = validateEmail(email)
        val isPhoneValid = validatePhone(phone)
        val isPasswordValid = validatePassword(password)
        return isNameValid && isEmailValid && isPhoneValid && isPasswordValid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            fontSize = 34.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create a new account to get started and enjoy seamless access to our features.",
            fontSize = 12.sp,
            color = Color.Gray,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(35.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (nameError.isNotEmpty()) validateName(it)
            },
            singleLine = true,
            placeholder = { Text("Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(50),
            isError = nameError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(55.dp)
        )
        if (nameError.isNotEmpty()) {
            Text(
                text = nameError,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (emailError.isNotEmpty()) validateEmail(it)
            },
            singleLine = true,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = {
                val digitsOnly = it.filter { char -> char.isDigit() }
                phone = if (digitsOnly.length <= 11) digitsOnly else digitsOnly.take(11)
                if (phoneError.isNotEmpty()) validatePhone(phone)
            },
            singleLine = true,
            placeholder = { Text("Phone ") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(50),
            isError = phoneError.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(55.dp)
        )
        if (phoneError.isNotEmpty()) {
            Text(
                text = phoneError,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedTextField(
            value = password,
            singleLine = true,
            onValueChange = {
                password = it
                if (passwordError.isNotEmpty()) validatePassword(it)
            },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        val coroutineScope = rememberCoroutineScope()

        Button(
            onClick = {
                if (validateAll()) {
                    coroutineScope.launch {
                        try {
                            viewModel.register(name, email, phone, password)
                            Toast.makeText(context, "Account created successfully!", Toast.LENGTH_SHORT).show()
                            showSuccessMessage = true

                            kotlinx.coroutines.delay(1000)
                            navController.navigate("login") {
                                popUpTo("signup") { inclusive = true }
                            }
                            showSuccessMessage = false

                        } catch (e: Exception) {
                            Toast.makeText(context, e.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            interactionSource = remember { MutableInteractionSource() },
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
        ) {
            Text(text = "Register", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Already have an account? ", fontSize = 14.sp)
            Text(
                text = "Login",
                color = PrimaryColor,
                fontSize = 14.sp,
                modifier = Modifier.clickable { navController.navigate("login") }
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        if (showSuccessMessage) {
            Text(
                text = "Account created successfully!",
                color = Green,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )
        }

        viewModel.errorMessage.value?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
