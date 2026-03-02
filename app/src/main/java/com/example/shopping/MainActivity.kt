package com.example.shopping

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shopping.model.*
import com.example.shopping.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "main_list") {
                        // 1. 購物清單主頁
                        composable("main_list") {
                            MainContainer(navController)
                        }
                        // 2. 夥伴做的導航輸入頁
                        composable("teammate_home") {
                            TeammateHomeScreen(navController)
                        }
                        // 3. AR 導航相機畫面
                        composable("ar_navigation") {
                            NavigationScreen(navController)
                        }
                    }
                }
            }
        }
    }
}