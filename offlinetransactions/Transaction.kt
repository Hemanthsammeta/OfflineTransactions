package com.example.offlinetransactions

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val phoneNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    var status: String // Possible values: "pending", "successful", "failed"
)
