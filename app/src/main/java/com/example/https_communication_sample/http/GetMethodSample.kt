package com.example.https_communication_sample.http

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GetMethodSample(private val context: Context) {
    private val json = Json { isLenient = true; ignoreUnknownKeys = true }

    // ネットワーク接続を確認する
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // GETリクエストの非同期処理
    suspend fun fetchUsers(): ResultWrapper<List<User>> {
        // インターネット接続を確認
        if (!isInternetAvailable()) {
            return ResultWrapper.Error("No internet connection")
        }

        val url = "https://jsonplaceholder.typicode.com/users"

        return withContext(Dispatchers.IO) {
            try {
                delay(3000) // 3秒遅延
                val (_, _, result) = Fuel.get(url).responseString()

                when (result) {
                    is Result.Success -> {
                        val jsonResponse = result.get()
                        val users = json.decodeFromString<List<User>>(jsonResponse)
                        ResultWrapper.Success(users)
                    }
                    is Result.Failure -> {
                        ResultWrapper.Error(result.error.localizedMessage ?: "Request failed")
                    }
                }
            } catch (e: Exception) {
                ResultWrapper.Error(e.localizedMessage ?: "Exception occurred")
            }
        }
    }
}

@Serializable
data class Geo(
    val lat: String,
    val lng: String
)

@Serializable
data class Address(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String,
    val geo: Geo
)

@Serializable
data class Company(
    val name: String,
    val catchPhrase: String,
    val bs: String
)

@Serializable
data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val address: Address,
    val phone: String,
    val website: String,
    val company: Company
)

sealed class ResultWrapper<out T> {
    data class Success<out T>(val data: T) : ResultWrapper<T>()
    data class Error(val message: String) : ResultWrapper<Nothing>()
}