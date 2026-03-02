package com.example.shopping.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.shopping.model.PaymentReminder
import com.example.shopping.model.ShoppingItem

@Composable
fun MainContainer(rootNavController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var shoppingItems by remember { mutableStateOf(listOf<ShoppingItem>()) }
    var detectedStore by remember { mutableStateOf<String?>(null) }
    var activeReminder by remember { mutableStateOf<PaymentReminder?>(null) }

    Scaffold(
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
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "成分") },
                    label = { Text("成分") }
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