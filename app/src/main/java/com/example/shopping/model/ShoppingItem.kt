package com.example.shopping.model

import java.util.*

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var qty: Int = 1,
    var price: Int = 0,
    var isChecked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    var purchasedAt: Long? = null,
    val dueDate: Long? = null
)

data class PaymentReminder(
    val store: String,
    val text: String,
    val amount: Int,
    val dueDate: String
)