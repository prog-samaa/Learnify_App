package com.example.learnify.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.learnify.ui.components.Loading
import com.example.learnify.ui.theme.AppBackgroundColor
import com.example.learnify.ui.theme.PrimaryColor
import com.example.learnify.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: androidx.navigation.NavController,
    viewModel: UserViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userValue = viewModel.currentUser.value ?: run {
        Loading()
        return
    }

    var name by remember { mutableStateOf(userValue.name) }
    var phone by remember { mutableStateOf(userValue.phone) }
    var passwordCurrent by remember { mutableStateOf("") }
    var passwordNew by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var currentPasswordError by remember { mutableStateOf("") }
    var passwordMatchError by remember { mutableStateOf("") }
    var passwordRequirementsError by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.errorMessage.value) {
        viewModel.errorMessage.value?.let { error ->
            if (error.contains("Wrong current password", ignoreCase = true)) {
                currentPasswordError = "Current password is incorrect"
            }
            viewModel.errorMessage.value = null
        }
    }

    fun validateCurrentPassword(): Boolean {
        return if (passwordNew.isNotEmpty() && passwordCurrent.isEmpty()) {
            currentPasswordError = "Please enter current password"
            false
        } else {
            currentPasswordError = ""
            true
        }
    }

    fun validatePasswordMatch(): Boolean {
        return if (passwordNew.isNotEmpty() && passwordNew != passwordConfirm) {
            passwordMatchError = "Passwords do not match"
            false
        } else {
            passwordMatchError = ""
            true
        }
    }

    fun validatePasswordRequirements(): Boolean {
        return if (passwordNew.isNotEmpty()) {
            val validationResult = viewModel.validateNewPassword(passwordNew)
            if (!validationResult.isValid) {
                passwordRequirementsError = validationResult.message
                false
            } else {
                passwordRequirementsError = ""
                true
            }
        } else {
            passwordRequirementsError = ""
            true
        }
    }

    fun validateForm(): Boolean {
        val isPasswordMatchValid = validatePasswordMatch()
        val isCurrentPasswordValid = validateCurrentPassword()
        val isPasswordRequirementsValid = validatePasswordRequirements()
        return isPasswordMatchValid &&
                isCurrentPasswordValid &&
                isPasswordRequirementsValid
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(AppBackgroundColor)
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
                if (userValue.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = userValue.imageUrl,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = userValue.name.first().uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Change Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = passwordCurrent,
                        onValueChange = {
                            passwordCurrent = it
                            currentPasswordError = ""
                        },
                        label = { Text("Current Password") },
                        visualTransformation =
                            if (showCurrentPassword) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { showCurrentPassword = !showCurrentPassword }
                            ) {
                                Icon(
                                    imageVector =
                                        if (showCurrentPassword)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = currentPasswordError.isNotEmpty()
                    )

                    if (currentPasswordError.isNotEmpty()) {
                        Text(
                            text = currentPasswordError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passwordNew,
                        onValueChange = {
                            passwordNew = it
                            passwordMatchError = ""
                            passwordRequirementsError = ""
                        },
                        label = { Text("New Password") },
                        visualTransformation =
                            if (showNewPassword) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { showNewPassword = !showNewPassword }
                            ) {
                                Icon(
                                    imageVector =
                                        if (showNewPassword)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = passwordMatchError.isNotEmpty() ||
                                passwordRequirementsError.isNotEmpty()
                    )

                    if (passwordRequirementsError.isNotEmpty()) {
                        Text(
                            text = passwordRequirementsError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passwordConfirm,
                        onValueChange = {
                            passwordConfirm = it
                            passwordMatchError = ""
                        },
                        label = { Text("Confirm New Password") },
                        visualTransformation =
                            if (showConfirmPassword) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { showConfirmPassword = !showConfirmPassword }
                            ) {
                                Icon(
                                    imageVector =
                                        if (showConfirmPassword)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = passwordMatchError.isNotEmpty()
                    )

                    if (passwordMatchError.isNotEmpty()) {
                        Text(
                            text = passwordMatchError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = PrimaryColor
                    ),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = {
                        if (!validateForm()) {
                            if (passwordRequirementsError.isNotEmpty()) {
                                Toast.makeText(
                                    context,
                                    passwordRequirementsError,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            return@Button
                        }

                        isLoading = true
                        scope.launch {
                            try {
                                if (passwordNew.isNotEmpty()) {
                                    val isCurrentPasswordCorrect =
                                        viewModel.verifyCurrentPassword(passwordCurrent)
                                    if (!isCurrentPasswordCorrect) {
                                        currentPasswordError =
                                            "Current password is incorrect"
                                        isLoading = false
                                        Toast.makeText(
                                            context,
                                            "Current password is incorrect",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@launch
                                    }
                                }

                                viewModel.updateProfile(
                                    name = name,
                                    phone = phone,
                                    currentPassword =
                                        passwordCurrent.takeIf { it.isNotEmpty() },
                                    newPassword =
                                        passwordNew.takeIf { it.isNotEmpty() }
                                ) { success, error ->
                                    isLoading = false

                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "Profile updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.popBackStack()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            error ?: "Error updating profile",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    e.message ?: "Error updating profile",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryColor
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Save Changes",
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
