package com.subway.app.data.auth

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun currentAccount(): String = prefs.getString(KEY_CURRENT_ACCOUNT, "") ?: ""

    fun login(account: String, password: String): AuthResult {
        val result = AuthValidator.login(account, password, getRegisteredAccounts(), getBuiltInPassword())
        if (result is AuthResult.Success) {
            prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_CURRENT_ACCOUNT, result.account)
                .apply()
        }
        return result
    }

    fun changeCurrentPassword(currentPassword: String, newPassword: String, confirmPassword: String): PasswordChangeResult {
        val account = currentAccount()
        if (account.isBlank()) return PasswordChangeResult.NotLoggedIn
        if (AuthValidator.login(account, currentPassword, getRegisteredAccounts(), getBuiltInPassword()) !is AuthResult.Success) {
            return PasswordChangeResult.CurrentPasswordIncorrect
        }
        if (AuthValidator.isSamePassword(currentPassword, newPassword)) return PasswordChangeResult.SameAsCurrentPassword
        AuthValidator.validateNewPassword(newPassword)?.let {
            return if (it is AuthResult.EmptyPassword) PasswordChangeResult.EmptyNewPassword else PasswordChangeResult.NewPasswordTooShort
        }
        if (newPassword != confirmPassword) return PasswordChangeResult.PasswordMismatch
        if (account == "admin") {
            prefs.edit().putString(KEY_BUILT_IN_PASSWORD, newPassword).apply()
        } else {
            val updated = getRegisteredAccounts().toMutableMap()
            updated[account] = newPassword
            prefs.edit().putString(KEY_REGISTERED_ACCOUNTS, encodeAccounts(updated)).apply()
        }
        return PasswordChangeResult.Success
    }

    fun register(account: String, password: String): AuthResult {
        val accounts = getRegisteredAccounts()
        val result = AuthValidator.register(account, password, accounts)
        if (result is AuthResult.Success) {
            val updated = accounts + (result.account to password)
            prefs.edit().putString(KEY_REGISTERED_ACCOUNTS, encodeAccounts(updated)).apply()
        }
        return result
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_CURRENT_ACCOUNT)
            .apply()
    }

    fun getRegisteredAccounts(): Map<String, String> {
        val raw = prefs.getString(KEY_REGISTERED_ACCOUNTS, "") ?: ""
        if (raw.isBlank()) return emptyMap()
        return raw.split("|")
            .mapNotNull { item ->
                val parts = item.split(":", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else null
            }
            .toMap()
    }

    private fun getBuiltInPassword(): String = prefs.getString(KEY_BUILT_IN_PASSWORD, "123456") ?: "123456"

    private fun encodeAccounts(accounts: Map<String, String>): String {
        return accounts.entries.joinToString("|") { it.key + ":" + it.value }
    }

    companion object {
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_CURRENT_ACCOUNT = "current_account"
        private const val KEY_REGISTERED_ACCOUNTS = "registered_accounts"
        private const val KEY_BUILT_IN_PASSWORD = "built_in_password"
    }
}

sealed class PasswordChangeResult {
    object Success : PasswordChangeResult()
    object NotLoggedIn : PasswordChangeResult()
    object CurrentPasswordIncorrect : PasswordChangeResult()
    object EmptyNewPassword : PasswordChangeResult()
    object NewPasswordTooShort : PasswordChangeResult()
    object PasswordMismatch : PasswordChangeResult()
    object SameAsCurrentPassword : PasswordChangeResult()
}
