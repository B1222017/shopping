package com.example.shopping.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.shopping.model.DietRecord
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IngredientsScreen(
    records: List<DietRecord>,
    onRecordsUpdate: (List<DietRecord>) -> Unit
) {
    val context = LocalContext.current
    var recordDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var foodName by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var unitCalorie by remember { mutableStateOf("0") }
    var portion by remember { mutableStateOf("1.0") }
    var editId by remember { mutableStateOf<String?>(null) }
    
    // 驗證狀態
    var showNameError by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val totalCalories = remember(unitCalorie, portion) {
        val u = unitCalorie.toIntOrNull() ?: 0
        val p = portion.toDoubleOrNull() ?: 0.0
        (u * p).toInt()
    }

    // 過濾出選定日期的紀錄
    val filteredRecords = remember(records, recordDate) {
        records.filter { it.date == recordDate }
    }

    // 攝取趨勢計算 (最近 7 天)
    val weeklyStats = remember(records) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        (0..6).reversed().map { daysAgo ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dateStr = sdf.format(cal.time)
            val dayTotal = records.filter { it.date == dateStr }.sumOf { it.totalCalories }
            dateStr to dayTotal
        }
    }

    // 拍照與 OCR 邏輯
    val tempFile = remember { File(context.cacheDir, "ocr_temp.jpg") }
    val imageUri = remember { 
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile) 
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            isProcessing = true
            processImageForText(context, imageUri) { recognizedText ->
                // 解析文字並填入表格
                val lines = recognizedText.lines().filter { it.isNotBlank() }
                if (lines.isNotEmpty()) {
                    // 1. 品名：嘗試取第一行
                    foodName = lines.first().take(30)
                    
                    // 2. 成分：尋找關鍵字
                    val lowerText = recognizedText.lowercase()
                    val keywords = listOf("成分", "ingredients", "原料", "內容物")
                    var foundIdx = -1
                    for (kw in keywords) {
                        foundIdx = lowerText.indexOf(kw)
                        if (foundIdx != -1) break
                    }
                    
                    if (foundIdx != -1) {
                        ingredients = recognizedText.substring(foundIdx)
                            .substringAfter(":")
                            .substringAfter("：")
                            .take(150).trim()
                    }

                    // 3. 熱量：正則表達式尋找
                    val calorieRegex = """(\d+)\s*(kcal|大卡|熱量|Calories)""".toRegex(RegexOption.IGNORE_CASE)
                    calorieRegex.find(recognizedText)?.groupValues?.get(1)?.let {
                        unitCalorie = it
                    }
                }
                isProcessing = false
                showNameError = false
                // 辨識完後刪除照片
                if (tempFile.exists()) {
                    tempFile.delete()
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // 標題與拍照辨識
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("健康管理", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(
                        onClick = { cameraLauncher.launch(imageUri) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF2563EB), RoundedCornerShape(20.dp))
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "拍照辨識", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // 1. 輸入與修改表單
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (editId == null) "添加飲食紀錄" else "修改飲食紀錄", fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))

                    OutlinedTextField(
                        value = recordDate,
                        onValueChange = { },
                        label = { Text("選擇日期 (Date)") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                val cal = Calendar.getInstance()
                                try {
                                    val current = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(recordDate)
                                    if (current != null) cal.time = current
                                } catch(e: Exception) {}
                                
                                DatePickerDialog(context, { _, y, m, d ->
                                    recordDate = String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d)
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                            }) { Icon(Icons.Default.CalendarMonth, null) }
                        }
                    )

                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { 
                            foodName = it
                            if (it.isNotBlank()) showNameError = false
                        },
                        label = { Text("商品名稱 *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isError = showNameError,
                        supportingText = { if (showNameError) Text("必須輸入商品名稱", color = MaterialTheme.colorScheme.error) }
                    )

                    OutlinedTextField(
                        value = ingredients,
                        onValueChange = { ingredients = it },
                        label = { Text("成分表") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = { },
                        label = { Text("有效期限") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                val cal = Calendar.getInstance()
                                DatePickerDialog(context, { _, y, m, d ->
                                    expiryDate = String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d)
                                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                            }) { Icon(Icons.Default.Event, null) }
                        }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = unitCalorie, onValueChange = { unitCalorie = it.filter { c -> c.isDigit() } }, label = { Text("單份熱量") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = portion, onValueChange = { portion = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("份量") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    }

                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFFF7ED), RoundedCornerShape(12.dp)).padding(16.dp)) {
                        Column {
                            Text("TOTAL CALCULATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFB923C))
                            Text("$totalCalories kcal", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFFEA580C))
                        }
                    }

                    Button(
                        onClick = {
                            if (foodName.isBlank()) { showNameError = true; return@Button }
                            val newRecord = DietRecord(
                                id = editId ?: UUID.randomUUID().toString(),
                                date = recordDate,
                                name = foodName,
                                ingredients = ingredients,
                                expiryDate = expiryDate,
                                unitCalorie = unitCalorie.toIntOrNull() ?: 0,
                                portion = portion.toDoubleOrNull() ?: 1.0,
                                totalCalories = totalCalories
                            )
                            onRecordsUpdate(if (editId == null) listOf(newRecord) + records else records.map { if (it.id == editId) newRecord else it })
                            foodName = ""; ingredients = ""; expiryDate = ""; unitCalorie = "0"; portion = "1.0"; editId = null; showNameError = false
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text(if (editId == null) "儲存紀錄" else "確認修改", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    if (editId != null) {
                        TextButton(onClick = { editId = null; foodName = ""; ingredients = ""; expiryDate = ""; unitCalorie = "0"; portion = "1.0" }, modifier = Modifier.fillMaxWidth()) {
                            Text("取消編輯", color = Color.Gray)
                        }
                    }
                }
            }
        }

        // 2. 當天紀錄
        item {
            Text("$recordDate 的飲食內容", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 18.sp)
        }

        if (filteredRecords.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Text("此日期尚無紀錄", color = Color.LightGray, fontSize = 14.sp)
                }
            }
        } else {
            items(filteredRecords, key = { it.id }) { record ->
                DietRecordCard(
                    record = record,
                    onEdit = {
                        editId = record.id
                        recordDate = record.date
                        foodName = record.name
                        ingredients = record.ingredients
                        expiryDate = record.expiryDate
                        unitCalorie = record.unitCalorie.toString()
                        portion = record.portion.toString()
                        showNameError = false
                    },
                    onDelete = { onRecordsUpdate(records.filter { it.id != record.id }) }
                )
            }
        }

        // 3. 趨勢圖
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))
            Text("攝取趨勢 (最近 7 天)", fontWeight = FontWeight.Bold, color = Color.Gray)
            Card(
                modifier = Modifier.fillMaxWidth().height(180.dp).padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxCal = (weeklyStats.maxOfOrNull { it.second } ?: 2000).coerceAtLeast(1)
                    weeklyStats.forEach { (date, cal) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val barHeight = (cal.toFloat() / maxCal * 100).dp
                            Box(modifier = Modifier
                                .width(18.dp)
                                .height(barHeight)
                                .background(if (date == recordDate) Color(0xFFEA580C) else Color(0xFF3B82F6), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                            Text(date.substring(8), fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

private fun processImageForText(context: Context, uri: Uri, onResult: (String) -> Unit) {
    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onResult(visionText.text)
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "辨識失敗", e)
                onResult("")
            }
    } catch (e: Exception) {
        Log.e("OCR", "開啟圖片失敗", e)
        onResult("")
    }
}

@Composable
fun DietRecordCard(record: DietRecord, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(record.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                Text("${record.totalCalories} kcal", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
            }
            if (record.expiryDate.isNotBlank()) {
                Text("有效期限: ${record.expiryDate}", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))
            }
            if (record.ingredients.isNotBlank()) {
                Surface(
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("成分: ${record.ingredients}", fontSize = 12.sp, color = Color(0xFF6B7280), modifier = Modifier.padding(8.dp))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "編輯", tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "刪除", tint = Color(0xFFFCA5A5), modifier = Modifier.size(20.dp)) }
            }
        }
    }
}
