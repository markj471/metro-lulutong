package com.subway.app.data.auth

sealed class AuthResult {
    data class Success(val account: String) : AuthResult()
    object EmptyAccount : AuthResult()
    object EmptyPassword : AuthResult()
    object InvalidFormat : AuthResult()
    object AccountExists : AuthResult()
    object InvalidCredentials : AuthResult()
}

object AuthValidator {
    private const val BUILT_IN_ACCOUNT = "admin"
    private const val BUILT_IN_PASSWORD = "123456"
    private val accountRegex = Regex("^[A-Za-z0-9_]{3,20}$")

    fun login(account: String, password: String, registeredAccounts: Map<String, String>, builtInPassword: String = BUILT_IN_PASSWORD): AuthResult {
        val normalizedAccount = account.trim()
        if (normalizedAccount.isEmpty()) return AuthResult.EmptyAccount
        if (password.isEmpty()) return AuthResult.EmptyPassword

        val expectedPassword = when (normalizedAccount) {
            BUILT_IN_ACCOUNT -> builtInPassword
            else -> registeredAccounts[normalizedAccount]
        }

        return if (expectedPassword == password) {
            AuthResult.Success(normalizedAccount)
        } else {
            AuthResult.InvalidCredentials
        }
    }

    fun register(account: String, password: String, registeredAccounts: Map<String, String>): AuthResult {
        val normalizedAccount = account.trim()
        if (normalizedAccount.isEmpty()) return AuthResult.EmptyAccount
        if (password.isEmpty()) return AuthResult.EmptyPassword
        if (!accountRegex.matches(normalizedAccount) || password.length < 6) return AuthResult.InvalidFormat
        if (normalizedAccount == BUILT_IN_ACCOUNT || registeredAccounts.containsKey(normalizedAccount)) {
            return AuthResult.AccountExists
        }
        return AuthResult.Success(normalizedAccount)
    }

    fun validateNewPassword(password: String): AuthResult? {
        return if (password.isEmpty()) AuthResult.EmptyPassword
        else if (password.length < 6) AuthResult.InvalidFormat
        else null
    }

    fun isSamePassword(current: String, newPassword: String): Boolean = current == newPassword
}
