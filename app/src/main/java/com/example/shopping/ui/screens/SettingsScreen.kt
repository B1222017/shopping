package com.example.shopping.ui.screens

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var cacheSize by remember { mutableStateOf(getCacheSize(context)) }
    var appSize by remember { mutableStateOf(getAppSize(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("儲存空間狀態", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    StorageInfoRow(label = "應用程式資料", size = appSize)
                    StorageInfoRow(label = "快取檔案", size = cacheSize)
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            clearCache(context)
                            cacheSize = getCacheSize(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.DeleteSweep, null)
                        Spacer(Modifier.width(8.dp))
                        Text("清除快取資料")
                    }
                }
            }
        }
    }
}

@Composable
fun StorageInfoRow(label: String, size: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(size, fontWeight = FontWeight.Medium)
    }
}

private fun getCacheSize(context: Context): String {
    val size = getFolderSize(context.cacheDir) + (context.externalCacheDir?.let { getFolderSize(it) } ?: 0L)
    return Formatter.formatFileSize(context, size)
}

private fun getAppSize(context: Context): String {
    val size = getFolderSize(context.filesDir)
    return Formatter.formatFileSize(context, size)
}

private fun getFolderSize(file: File): Long {
    var size = 0L
    if (file.isDirectory) {
        file.listFiles()?.forEach { size += getFolderSize(it) }
    } else {
        size = file.length()
    }
    return size
}

private fun clearCache(context: Context) {
    context.cacheDir.deleteRecursively()
    context.externalCacheDir?.deleteRecursively()
}
