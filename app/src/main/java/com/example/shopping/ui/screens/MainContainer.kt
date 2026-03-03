package com.example.shopping.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.shopping.model.DietRecord
import com.example.shopping.model.PaymentReminder
import com.example.shopping.model.ShoppingItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(rootNavController: NavController) {
    val context = LocalContext.current
    val shoppingFile = File(context.filesDir, "shopping_list.json")
    val dietFile = File(context.filesDir, "diet_records.json")
    
    val jsonConfig = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    var shoppingItems by remember {
        val items = try {
            if (shoppingFile.exists()) {
                jsonConfig.decodeFromString<List<ShoppingItem>>(shoppingFile.readText())
            } else emptyList()
        } catch (e: Exception) { emptyList<ShoppingItem>() }
        mutableStateOf(items)
    }

    var dietRecords by remember {
        val records = try {
            if (dietFile.exists()) {
                jsonConfig.decodeFromString<List<DietRecord>>(dietFile.readText())
            } else emptyList()
        } catch (e: Exception) { emptyList<DietRecord>() }
        mutableStateOf(records)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var detectedStore by remember { mutableStateOf<String?>(null) }
    var activeReminder by remember { mutableStateOf<PaymentReminder?>(null) }

    LaunchedEffect(shoppingItems) {
        try { shoppingFile.writeText(jsonConfig.encodeToString(shoppingItems)) } catch (e: Exception) {}
    }
    LaunchedEffect(dietRecords) {
        try { dietFile.writeText(jsonConfig.encodeToString(dietRecords)) } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(when(selectedTab) {
                        0 -> "購物清單"
                        1 -> "健康管理"
                        2 -> "AI 助理"
                        else -> "歷史紀錄"
                    })
                },
                actions = {
                    IconButton(onClick = { rootNavController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "設定")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "清單") },
                    label = { Text("清單") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "健康") },
                    label = { Text("健康") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "AI") },
                    label = { Text("AI") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "歷史") },
                    label = { Text("歷史") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { 
                        // 導航功能回到下方
                        val itemsJson = jsonConfig.encodeToString(shoppingItems.filter { !it.isChecked })
                        rootNavController.currentBackStackEntry?.savedStateHandle?.set("shopping_list_json", itemsJson)
                        rootNavController.navigate("teammate_home") 
                    },
                    icon = { Icon(Icons.Default.Route, contentDescription = "導航") },
                    label = { Text("導航") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ShoppingListScreen(
                    items = shoppingItems,
                    onItemsUpdate = { shoppingItems = it },
                    detectedStore = detectedStore,
                    activeReminder = activeReminder,
                    onDetectClick = { detectedStore = "搜尋附近商店..." },
                    onDismissReminder = { activeReminder = null }
                )
                1 -> IngredientsScreen(
                    records = dietRecords,
                    onRecordsUpdate = { dietRecords = it }
                )
                2 -> AIScreen()
                3 -> HistoryScreen(shoppingItems = shoppingItems)
            }
        }
    }
}                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "AI") },
                    label = { Text("AI") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "歷史") },
                    label = { Text("歷史") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { rootNavController.navigate("teammate_home") },
                    icon = { Icon(Icons.Default.Route, contentDescription = "導航") },
                    label = { Text("導航") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ShoppingListScreen(
                    items = shoppingItems,
                    onItemsUpdate = { shoppingItems = it },
                    detectedStore = detectedStore,
                    activeReminder = activeReminder,
                    onDetectClick = {
                        detectedStore = "家樂福 台北店"
                        activeReminder = PaymentReminder("家樂福", "電費", 1200, "2023-11-20")
                    },
                    onDismissReminder = { activeReminder = null }
                )
                1 -> IngredientsScreen()
                2 -> AIScreen()
                3 -> HistoryScreen(shoppingItems = shoppingItems)
            }
        }
    }
}
