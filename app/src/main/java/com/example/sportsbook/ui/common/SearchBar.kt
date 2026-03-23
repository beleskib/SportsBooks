package com.example.sportsbook.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsbook.ui.theme.CoolGray
import com.example.sportsbook.ui.theme.Navy700
import com.example.sportsbook.ui.theme.SportsBookTheme
import com.example.sportsbook.ui.theme.USOpenGold
import com.example.sportsbook.ui.theme.WarmWhite

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search...",
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(placeholder, color = CoolGray)
        },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = CoolGray)
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Navy700,
            unfocusedContainerColor = Navy700,
            focusedTextColor = WarmWhite,
            unfocusedTextColor = WarmWhite,
            cursorColor = USOpenGold,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Preview
@Composable
private fun SearchBarPreview() {
    SportsBookTheme { SearchBar(query = "", onQueryChange = {}, placeholder = "Search venues...") }
}
