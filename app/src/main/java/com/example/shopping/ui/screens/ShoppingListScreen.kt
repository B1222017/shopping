package com.example.shopping.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.shopping.model.ShoppingItem
import com.example.shopping.model.PaymentReminder
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    items: List<ShoppingItem>,
    onItemsUpdate: (List<ShoppingItem>) -> Unit,
    detectedStore: String?,
    activeReminder: PaymentReminder?,
    onDetectClick: () -> Unit,
    onDismissReminder: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var selectedDueDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val dateOnlySdf = SimpleDateFormat("MM/dd", Locale.getDefault())

    val activeItems = items.filter { !it.isChecked }
    val (dueItems, normalItems) = activeItems.partition { it.dueDate != null }
    val sortedDueItems = dueItems.sortedBy { it.dueDate }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("智慧購物助理", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(detectedStore ?: "尚未偵測位置", style = MaterialTheme.typography.titleMedium)
                        Text("偵測 5 公里內的商店", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = onDetectClick) { Text("偵測") }
                }
            }

            activeReminder?.let { reminder ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFDC2626))
                        Text("繳費提醒：${reminder.text} ($${reminder.amount})", modifier = Modifier.padding(horizontal = 8.dp).weight(1f), color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismissReminder) { Icon(Icons.Default.Close, null) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("商品名稱") }, modifier = Modifier.weight(1.5f), shape = RoundedCornerShape(12.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(value = itemPrice, onValueChange = { itemPrice = it }, label = { Text("單價") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp))
                        Text(if (selectedDueDate == null) " 設定期限" else " 期限: ${dateOnlySdf.format(Date(selectedDueDate!!))}")
                    }
                    Button(onClick = {
                        if (itemName.isNotBlank()) {
                            onItemsUpdate(items + ShoppingItem(name = itemName, price = itemPrice.toIntOrNull() ?: 0, dueDate = selectedDueDate))
                            itemName = ""; itemPrice = ""; selectedDueDate = null
                        }
                    }) { Text("加入清單") }
                }
            }
        }

        if (sortedDueItems.isNotEmpty()) {
            item { Text("期限購買清單", color = Color(0xFFB45309), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp)) }
            items(sortedDueItems, key = { it.id }) { item -> ShoppingItemRow(item, items, onItemsUpdate, dateOnlySdf) }
        }

        item { Text("一般清單", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp)) }
        items(normalItems, key = { it.id }) { item -> ShoppingItemRow(item, items, onItemsUpdate, dateOnlySdf) }

        val completedItems = items.filter { it.isChecked }
        if (completedItems.isNotEmpty()) {
            item { Text("已完成", color = Color.Gray, modifier = Modifier.padding(top = 16.dp)) }
            items(completedItems, key = { it.id }) { item -> ShoppingItemRow(item, items, onItemsUpdate, dateOnlySdf) }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = { selectedDueDate = datePickerState.selectedDateMillis; showDatePicker = false }) { Text("確定") }
        }) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun ShoppingItemRow(item: ShoppingItem, allItems: List<ShoppingItem>, onItemsUpdate: (List<ShoppingItem>) -> Unit, dateOnlySdf: SimpleDateFormat) {
    ListItem(
        headlineContent = {
            OutlinedTextField(
                value = item.name,
                onValueChange = { newName -> onItemsUpdate(allItems.map { if (it.id == item.id) it.copy(name = newName) else it }) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = if (item.isChecked) LocalTextStyle.current.copy(textDecoration = TextDecoration.LineThrough, color = Color.Gray) else LocalTextStyle.current,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = MaterialTheme.colorScheme.primary),
                singleLine = true
            )
        },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onItemsUpdate(allItems.map { if (it.id == item.id) it.copy(qty = maxOf(1, it.qty - 1)) else it }) }) { Icon(Icons.Default.KeyboardArrowDown, null) }
                Text("${item.qty}")
                IconButton(onClick = { onItemsUpdate(allItems.map { if (it.id == item.id) it.copy(qty = it.qty + 1) else it }) }) { Icon(Icons.Default.KeyboardArrowUp, null) }
                Spacer(modifier = Modifier.width(8.dp))
                Text("$")
                BasicTextField(
                    value = item.price.toString(),
                    onValueChange = { newPrice ->
                        val priceInt = newPrice.filter { it.isDigit() }.toIntOrNull() ?: 0
                        onItemsUpdate(allItems.map { if (it.id == item.id) it.copy(price = priceInt) else it })
                    },
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(60.dp).background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        leadingContent = { Checkbox(checked = item.isChecked, onCheckedChange = { isChecked ->
            onItemsUpdate(allItems.map { if (it.id == item.id) it.copy(isChecked = isChecked, purchasedAt = if (isChecked) System.currentTimeMillis() else null) else it })
        }) },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text("$${item.qty * item.price}", fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = { onItemsUpdate(allItems.filter { it.id != item.id }) }) { Icon(Icons.Default.Delete, null, tint = Color.LightGray) }
            }
        }
    )
}