package com.example.https_communication_sample

import android.os.Bundle
import android.app.AlertDialog
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.https_communication_sample.http.GetMethodSample
import com.example.https_communication_sample.http.ResultWrapper
import com.example.https_communication_sample.ui.theme.HttpscommunicationsampleTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {
    private lateinit var getMethodSample: GetMethodSample

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // GetMethodSampleを初期化
        getMethodSample = GetMethodSample(this)

        setContent {
            HttpscommunicationsampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var isLoading by remember { mutableStateOf(false) }

                    // MainScreenにisLoadingを渡す
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        isLoading = isLoading,
                        onGetMethodClicked = {
                            performGetRequest { isLoading = it }
                        }
                    )
                }
            }
        }
    }

    // GETリクエストを非同期で実行
    private fun performGetRequest(onLoadingChanged: (Boolean) -> Unit) {
        lifecycleScope.launch {
            // 通信開始時にロード中フラグを設定
            onLoadingChanged(true)

            when (val result = getMethodSample.fetchUsers()) {
                is ResultWrapper.Success -> {
                    val users = result.data
                    val message = users.joinToString("\n") { user ->
                        "ID:${user.id}, Name:${user.username}"
                    }
                    showDialog("通信結果", message)
                }
                is ResultWrapper.Error -> {
                    println("Error: ${result.message}")
                    showDialog("エラー", result.message)
                }
            }

            // 通信終了後にロード中フラグを解除
            onLoadingChanged(false)
        }
    }

    // シンプルなメッセージボックス（AlertDialog）を表示
    private fun showDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
        val dialog = builder.create()
        dialog.show()
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, isLoading: Boolean, onGetMethodClicked: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // ローディング画面
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize() // 画面全体に広げる
                    .background(color = Color.Black.copy(alpha = 0.15f)) // 半透明の黒背景を設定
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center) // 進行状況を中央に表示
                )
            }
        }

        // メインのレイアウト
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "HTTPSサンプル",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onGetMethodClicked,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading // ローディング中はボタンを無効化
            ) {
                Text("GETメソッド")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    HttpscommunicationsampleTheme {
        MainScreen(isLoading = false, onGetMethodClicked = {})
    }
}
