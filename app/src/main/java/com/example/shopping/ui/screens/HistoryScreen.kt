package com.example.shopping.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shopping.model.ShoppingItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(shoppingItems: List<ShoppingItem>) {
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    val sdfFull = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfMonth = SimpleDateFormat("yyyy年MM月", Locale.getDefault())

    val completedItems = shoppingItems.filter { it.isChecked }
    val monthlyTotal = completedItems.sumOf { it.qty * it.price }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("支出分析 - ${sdfMonth.format(Date())}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("本月實付總額", style = MaterialTheme.typography.labelMedium)
                Text("$${monthlyTotal}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }
        }

        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.DateRange, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (selectedDateMillis == null) "依日期篩選" else "日期: ${sdfFull.format(Date(selectedDateMillis!!))}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            val history = completedItems.filter { item ->
                selectedDateMillis == null || sdfFull.format(Date(item.purchasedAt ?: 0)) == sdfFull.format(Date(selectedDateMillis!!))
            }.reversed()

            items(history) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    ListItem(
                        headlineContent = { Text(item.name) },
                        supportingContent = { Text("數量: ${item.qty} x 單價: ${item.price}") },
                        trailingContent = { Text("$${item.qty * item.price}", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A)) }
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = { selectedDateMillis = datePickerState.selectedDateMillis; showDatePicker = false }) { Text("確定") }
        }) { DatePicker(state = datePickerState) }
    }
}