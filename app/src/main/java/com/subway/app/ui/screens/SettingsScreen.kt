package com.subway.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.subway.app.data.auth.AuthManager
import com.subway.app.data.auth.PasswordChangeResult
import com.subway.app.data.settings.AppSettingsManager
import com.subway.app.data.settings.ThemeMode

@Composable
fun SettingsScreen(
    authManager: AuthManager,
    themeMode: ThemeMode,
    fontScale: Float,
    onThemeModeChange: (ThemeMode) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { AppSettingsManager(context) }
    var cacheSize by remember { mutableStateOf(settingsManager.cacheSizeText()) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text("设置", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            SettingsGroup {
                SettingsRow(Icons.Filled.AccountCircle, "账号管理", authManager.currentAccount().ifBlank { "未登录" }, onChangePassword)
            }
        }
        item {
            SettingsGroup {
                Text("通用设置", Modifier.padding(horizontal = 16.dp, vertical = 10.dp), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ThemeMode.entries.forEach { mode ->
                    SettingsRow(Icons.Filled.Palette, mode.label, if (themeMode == mode) "当前" else "", { onThemeModeChange(mode) })
                }
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("字体大小", fontSize = 15.sp)
                        Text("%.0f%%".format(fontScale * 100), color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(value = fontScale, onValueChange = onFontScaleChange, valueRange = 0.9f..1.2f, steps = 2)
                }
            }
        }
        item {
            SettingsGroup {
                SettingsRow(Icons.Filled.CleaningServices, "缓存管理", cacheSize) {
                    settingsManager.clearCache()
                    cacheSize = settingsManager.cacheSizeText()
                }
            }
        }
        item {
            SettingsGroup {
                SettingsRow(Icons.Filled.Info, "关于我们", "版本 1.0") { showAbout = true }
            }
        }
        item { Spacer(Modifier.height(28.dp)) }
        item {
            Button(
                onClick = { showLogoutConfirm = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) { Text("退出登录") }
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("确认退出登录") },
            text = { Text("退出后需要重新登录才能进入主界面。") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    authManager.logout()
                    onLogout()
                }) { Text("退出", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("取消") } }
        )
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("武汉地铁路路通") },
            text = { Text("版本 1.0\n\n提供武汉地铁线路查询、站点搜索、换乘规划、票价估算、收藏和历史记录等功能。") },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("确定") } }
        )
    }
}

@Composable
fun ChangePasswordScreen(authManager: AuthManager, onBack: () -> Unit, onPasswordChanged: () -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            Text("修改密码", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Surface(Modifier.fillMaxWidth().padding(top = 24.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.LockReset, null, tint = MaterialTheme.colorScheme.primary)
                Text("修改密码", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                PasswordEditField("当前密码", currentPassword, { currentPassword = it; message = null }, visible) { visible = !visible }
                PasswordEditField("新密码", newPassword, { newPassword = it; message = null }, visible) { visible = !visible }
                PasswordEditField("确认新密码", confirmPassword, { confirmPassword = it; message = null }, visible) { visible = !visible }
                Text("密码至少6位，修改后立即生效。", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (message != null) Text(message!!, color = if (success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, fontSize = 13.sp)
                Button(
                    onClick = {
                        when (authManager.changeCurrentPassword(currentPassword, newPassword, confirmPassword)) {
                            PasswordChangeResult.Success -> {
                                onPasswordChanged()
                            }
                            PasswordChangeResult.NotLoggedIn -> message = "当前没有登录账号"
                            PasswordChangeResult.CurrentPasswordIncorrect -> message = "当前密码错误"
                            PasswordChangeResult.EmptyNewPassword -> message = "请输入新密码"
                            PasswordChangeResult.NewPasswordTooShort -> message = "新密码至少需要6位"
                            PasswordChangeResult.PasswordMismatch -> message = "两次输入的新密码不一致"
                            PasswordChangeResult.SameAsCurrentPassword -> message = "新密码不能与当前密码相同"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("确认修改") }
            }
        }
    }
}

@Composable
private fun PasswordEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        visualTransformation = if (visible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "显示或隐藏密码")
            }
        }
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, fontSize = 15.sp) },
        supportingContent = { if (subtitle.isNotBlank()) Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        modifier = Modifier.clickable { onClick() }
    )
}
