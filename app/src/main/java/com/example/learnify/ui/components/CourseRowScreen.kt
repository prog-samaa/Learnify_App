package com.example.learnify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.learnify.R
import com.example.learnify.ui.viewModels.CourseViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CourseRowScreen(
    cardWeight: Int = 245,
    cardHeight: Int = 300,
    viewModel: CourseViewModel = viewModel(),
    query: String,
    isTrending: Boolean = false,
    isSearch: Boolean = false,
    navController: NavHostController
) {
    val categoryKey = viewModel.detectCategoryKeyFromQuery(query)

    val courses by when {
        isTrending -> viewModel.trendingCourses(query).observeAsState(emptyList())
        isSearch -> viewModel.searchResults.observeAsState(emptyList())
        else -> viewModel.generalCoursesByCategory(categoryKey).observeAsState(emptyList())
    }

    val isTrendingLoading by viewModel.isTrendingLoading.observeAsState(false)
    val isSearchLoading by viewModel.isSearchLoading.observeAsState(false)
    val isGeneralLoading by viewModel.isGeneralLoading.observeAsState(false)

    val isLoading = when {
        isTrending -> isTrendingLoading
        isSearch -> isSearchLoading
        else -> isGeneralLoading
    }

    val trendingError by viewModel.trendingError.observeAsState(null)
    val searchError by viewModel.searchError.observeAsState(null)
    val generalError by viewModel.generalError.observeAsState(null)

    val error = when {
        isTrending -> trendingError
        isSearch -> searchError
        else -> generalError
    }

    LaunchedEffect(query, isTrending) {
        when {
            isTrending -> viewModel.getTrendingCourses(query)
            isSearch -> viewModel.searchCoursesDirect(query)
            query.isNotBlank() -> viewModel.searchCourses(query)
        }
    }

    when {
        isLoading -> Loading()
        error != null -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.unknownerror_icon),
                contentDescription = "No courses Image",
                modifier = Modifier.size(100.dp)
            )
        }
        courses.isEmpty() -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.nocourses_icon),
                contentDescription = "No courses Image",
                modifier = Modifier.size(100.dp)
            )
        }
        else -> LazyRow(modifier = Modifier.padding(8.dp)) {
            items(courses) { course ->
                if (isTrending) {
                    TrendingCourseCard(
                        course = course,
                        onCourseClick = { selectedCourse ->
                            navController.navigate("courseDetails/${selectedCourse.id}")
                        }
                    )
                } else {
                    CourseCard(
                        course = course,
                        cardWeight = cardWeight,
                        cardHeight = cardHeight,
                        onCourseClick = { selectedCourse ->
                            navController.navigate("courseDetails/${selectedCourse.id}")
                        }
                    )
                }
            }
        }
    }
}
