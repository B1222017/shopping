package com.example.shopping.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class IngredientInfo(val name: String, val rating: String, val note: String)

@Composable
fun IngredientsScreen() {
    val ingredients = listOf(
        IngredientInfo("防腐劑 (Parabens)", "注意", "可能引起過敏或干擾荷爾蒙。"),
        IngredientInfo("人工色素 (Red 40)", "警告", "部分研究指出可能影響兒童注意力。"),
        IngredientInfo("棕櫚油", "中立", "飽和脂肪較高，且有環境保護議題。"),
        IngredientInfo("阿斯巴甜 (Aspartame)", "注意", "代糖成分，部分人群不宜食用。"),
        IngredientInfo("反式脂肪", "危險", "增加心血管疾病風險，建議避免。")
    )
    
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("商品成分百科", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("快速查詢商品添加物與健康風險", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("搜尋成分...") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ingredients.filter { it.name.contains(searchQuery, ignoreCase = true) }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                item.rating,
                                color = when(item.rating) {
                                    "危險" -> Color.Red
                                    "警告" -> Color(0xFFE65100)
                                    "注意" -> Color(0xFFFBC02D)
                                    else -> Color.Gray
                                },
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.note, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}