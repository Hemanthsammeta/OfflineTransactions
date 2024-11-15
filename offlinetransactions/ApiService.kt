package com.example.offlinetransactions

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

data class TransactionRequest(val amount: Double, val phoneNumber: String, val timestamp: Long)
data class TransactionResponse(val success: Boolean)

interface ApiService {
    @POST("/api/transactions")
    suspend fun sendTransaction(@Body transaction: TransactionRequest): Response<TransactionResponse>
}
