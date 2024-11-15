package com.example.offlinetransactions

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update


@Database(entities = [Transaction::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Synchronized block to ensure only one instance is created
            return INSTANCE ?: synchronized(this) {
                // Use context.applicationContext to avoid memory leaks
                val instance = Room.databaseBuilder(
                    context.applicationContext,  // Ensure context is non-null here
                    AppDatabase::class.java,
                    "transaction_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }


    data class TransactionSummary(
        val id: Int,
        val amount: Double,
        val status: String
    )

    @Dao
    interface TransactionDao {

        @Insert
        fun insertTransaction(transaction: Transaction): Long

        @Update
        fun updateTransaction(transaction: Transaction): Int

        @Query("SELECT * FROM transactions WHERE phoneNumber = :phoneNumber")
        fun getTransactionsByPhoneNumber(phoneNumber: String): List<Transaction>

        @Query("SELECT id, amount, status FROM transactions WHERE status = :status")
        fun getTransactionSummaries(status: String): List<TransactionSummary>

        @Query("DELETE FROM transactions WHERE id = :transactionId")
        fun deleteTransactionById(transactionId: Int): Int  // Returns number of rows deleted
    }
}
