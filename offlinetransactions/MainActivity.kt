package com.example.offlinetransactions

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var amountField: EditText
    private lateinit var phoneNumberField: EditText
    private lateinit var sendButton: Button
    private lateinit var statuse: TextView
    private val appDatabase: AppDatabase by lazy { AppDatabase.getDatabase(applicationContext) }
    private val transactionDao by lazy { appDatabase.transactionDao() }
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        amountField = findViewById(R.id.amountField)
        phoneNumberField = findViewById(R.id.phoneNumberField)
        sendButton = findViewById(R.id.sendButton)
        statuse = findViewById(R.id.statuse)

        // Check for Bluetooth permission and enable Bluetooth if necessary
        checkBluetoothPermission()

        sendButton.setOnClickListener {
            val amount = amountField.text.toString()
            val phoneNumber = phoneNumberField.text.toString()

            if (amount.isEmpty()) {
                Toast.makeText(this, "Amount cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    processOfflinePayment(amount, phoneNumber)
                } catch (e: Exception) {
                    Log.e("PaymentError", "Error processing payment: ${e.message}", e)
                    Toast.makeText(this, "Error processing payment: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun processOfflinePayment(amount: String, phoneNumber: String) {
        try {
            val transaction = Transaction(phoneNumber = phoneNumber, amount = amount.toDouble(), status = "pending")
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Insert transaction into the Room database and get the inserted transaction ID
                    val transactionId = transactionDao.insertTransaction(transaction)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Offline transaction added with ID $transactionId", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("DatabaseError", "Error inserting transaction: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PaymentError", "Error processing offline payment: ${e.message}", e)
            Toast.makeText(this, "Error processing offline payment: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun syncPendingTransactions() {
        if (isOnline(this)) {
            try {
                val pendingTransactions = transactionDao.getTransactionSummaries("pending")
                for (transactionSummary in pendingTransactions) {
                    val transaction = Transaction(
                        id = transactionSummary.id,
                        amount = transactionSummary.amount,
                        phoneNumber = "", // Assuming this field would be set when syncing from server
                        status = "pending"
                    )
                    val success = syncTransactionWithServer(transaction)
                    transaction.status = if (success) "successful" else "failed"

                    // Update transaction status in the database
                    transactionDao.updateTransaction(transaction)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error syncing transactions: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Log.d("SyncError", "No internet connection available.")
        }
    }

    private suspend fun syncTransactionWithServer(transaction: Transaction): Boolean {
        return try {
            val apiService = RetrofitInstance.apiService
            val response = apiService.sendTransaction(
                TransactionRequest(transaction.amount, transaction.phoneNumber, transaction.timestamp)
            )
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            Log.e("ServerSyncError", "Error syncing transaction: ${e.message}", e)
            Toast.makeText(this, "Server sync error: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
//                syncPendingTransactions()
            } catch (e: Exception) {
                Log.e("SyncError", "Error during resume: ${e.message}", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error during resume: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkBluetoothPermission() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_BLUETOOTH_PERMISSION
                )
            } else {
                enableBluetooth()
            }
        } else {
            enableBluetooth()
        }
    }

    private fun enableBluetooth() {
        try {
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                bluetoothEnableLauncher.launch(enableBtIntent)
            } else {
                Toast.makeText(this, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("BluetoothError", "Bluetooth error: ${e.message}", e)
            Toast.makeText(this, "Bluetooth error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private val bluetoothEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth enabled successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bluetooth is required for this app", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableBluetooth()
            } else {
                Toast.makeText(this, "Bluetooth permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSION = 1
    }
}
