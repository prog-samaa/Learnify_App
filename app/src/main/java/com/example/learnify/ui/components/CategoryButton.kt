package com.example.learnify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.learnify.R
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.learnify.data.model.CategoryItem


@Composable
fun CategoryButton(
    selected: String?,
    onSelect: (String) -> Unit,

    ) {
    val categories = listOf(

        CategoryItem("Marketing", painterResource(R.drawable.marketing)),
        CategoryItem("Medical", painterResource(R.drawable.medical)),
        CategoryItem("Language", painterResource(R.drawable.language)),
        CategoryItem("Engineering", painterResource(R.drawable.engineering)),
        CategoryItem("Human Development", painterResource(R.drawable.human_development)),
        CategoryItem("Programming", painterResource(R.drawable.programming))



    )
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            val isSelected = selected == category.name

            Card(
                modifier = Modifier
                    .size(width = 160.dp, height = 170.dp)
                    .padding(5.dp)
                    .clickable { onSelect(category.name) },
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {

                Image(
                    painter = category.icon,
                    contentDescription = category.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

            }

        }
    }

}
