package com.subway.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subway.app.data.auth.AuthManager
import com.subway.app.data.auth.AuthResult

@Composable
fun LoginScreen(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var registerMode by remember { mutableStateOf(false) }
    var showForgot by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background)))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, shadowElevation = 8.dp) {
                Icon(Icons.Filled.Train, null, Modifier.size(72.dp).padding(16.dp), tint = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("武汉地铁路路通", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("本地账号登录后进入地铁查询", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp)),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = account,
                        onValueChange = { account = it; message = null },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Filled.Person, null) },
                        label = { Text("账号") },
                        placeholder = { Text("admin") }
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; message = null },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Filled.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        label = { Text("密码") },
                        placeholder = { Text("123456") }
                    )
                    if (message != null) {
                        Text(message ?: "", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            val result = if (registerMode) {
                                authManager.register(account, password)
                            } else {
                                authManager.login(account, password)
                            }
                            when (result) {
                                is AuthResult.Success -> {
                                    if (registerMode) {
                                        message = "注册成功，请登录"
                                        registerMode = false
                                    } else {
                                        onLoginSuccess()
                                    }
                                }
                                AuthResult.EmptyAccount -> message = "请输入账号"
                                AuthResult.EmptyPassword -> message = "请输入密码"
                                AuthResult.InvalidFormat -> message = "账号需为3-20位字母/数字/下划线，密码至少6位"
                                AuthResult.AccountExists -> message = "账号已存在"
                                AuthResult.InvalidCredentials -> message = "账号或密码错误"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (registerMode) "注册账号" else "登录", fontSize = 16.sp)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { registerMode = !registerMode; message = null }) {
                            Text(if (registerMode) "返回登录" else "注册账号")
                        }
                        TextButton(onClick = { showForgot = true }) { Text("忘记密码") }
                    }
                }
            }
        }
    }

    if (showForgot) {
        AlertDialog(
            onDismissRequest = { showForgot = false },
            title = { Text("忘记密码") },
            text = { Text("后续可在这里接入短信、邮箱或后端找回密码接口。当前测试账号：admin / 123456。") },
            confirmButton = { TextButton(onClick = { showForgot = false }) { Text("知道了") } }
        )
    }
}
