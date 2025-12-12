package com.example.learnify.ui.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.learnify.data.local.CourseEntity
import com.example.learnify.ui.theme.ActiveStar
import com.example.learnify.ui.theme.unActiveStar
import com.example.learnify.ui.theme.AppBackgroundColor


@Composable
fun CourseCard(
    cardWeight: Int,
    cardHeight: Int,
    course: CourseEntity,
    onCourseClick: (CourseEntity) -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f)

    Card(
        modifier = Modifier
            .width(cardWeight.dp)
            .height(cardHeight.dp)
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = {
                        onCourseClick(course)
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 15.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackgroundColor),
            horizontalAlignment = Alignment.Start
        ) {
            SubcomposeAsyncImage(
                model = course.imageUrl,
                contentDescription = "Course Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            ) {
                SubcomposeAsyncImageContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleY = 1.39f
                            translationY = -6f
                        })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = course.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                Text(
                    text = "By ${course.channelTitle}",
                    fontSize = 8.sp,
                    color = Color.Gray,
                    maxLines = 1
                )

                Row {
                    val rating = course.rating ?: 4f
                    Log.d("CourseRating", "Course ${course.title} -> $rating")

                    repeat(5) { index ->
                        val tint = if (index < rating.toInt()) ActiveStar else unActiveStar
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}