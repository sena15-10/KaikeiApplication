package com.example.kaikeiapplication.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SalesItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val quantity: Int,
    val price: Int,
    val date: String
)