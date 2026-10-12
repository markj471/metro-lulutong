package com.subway.app

import com.subway.app.data.auth.AuthResult
import com.subway.app.data.auth.AuthValidator
import com.subway.app.data.search.SearchHistoryPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthAndSearchUnitTest {

    @Test
    fun loginValidatorAcceptsBuiltInAdminAndRegisteredUser() {
        val accounts = mapOf("wuhan" to "metro123")

        assertEquals(AuthResult.Success("admin"), AuthValidator.login("admin", "123456", accounts))
        assertEquals(AuthResult.Success("wuhan"), AuthValidator.login("wuhan", "metro123", accounts))
    }

    @Test
    fun loginValidatorRejectsBlankAndWrongPassword() {
        assertEquals(AuthResult.EmptyAccount, AuthValidator.login("", "123456", emptyMap()))
        assertEquals(AuthResult.EmptyPassword, AuthValidator.login("admin", "", emptyMap()))
        assertEquals(AuthResult.InvalidCredentials, AuthValidator.login("admin", "bad", emptyMap()))
    }

    @Test
    fun registerValidatorRejectsInvalidInputAndDuplicateAccount() {
        assertEquals(AuthResult.EmptyAccount, AuthValidator.register("", "123456", emptyMap()))
        assertEquals(AuthResult.EmptyPassword, AuthValidator.register("new_user", "", emptyMap()))
        assertEquals(AuthResult.InvalidFormat, AuthValidator.register("ab", "123456", emptyMap()))
        assertEquals(AuthResult.AccountExists, AuthValidator.register("admin", "123456", emptyMap()))
        assertEquals(AuthResult.Success("new_user"), AuthValidator.register("new_user", "123456", emptyMap()))
    }

    @Test
    fun passwordValidationRequiresSixCharacters() {
        assertEquals(AuthResult.EmptyPassword, AuthValidator.validateNewPassword(""))
        assertEquals(AuthResult.InvalidFormat, AuthValidator.validateNewPassword("12345"))
        assertEquals(null, AuthValidator.validateNewPassword("123456"))
        assertEquals(true, AuthValidator.isSamePassword("123456", "123456"))
        assertEquals(false, AuthValidator.isSamePassword("123456", "654321"))
        assertEquals(AuthResult.Success("admin"), AuthValidator.login("admin", "654321", emptyMap(), "654321"))
    }

    @Test
    fun searchHistoryMovesRepeatedKeywordToTopAndLimitsSize() {
        val existing = listOf("光谷广场", "江汉路", "武汉站", "洪山广场", "街道口")

        val updated = SearchHistoryPolicy.add(existing, "江汉路", maxSize = 4)

        assertEquals(listOf("江汉路", "光谷广场", "武汉站", "洪山广场"), updated)
    }
}
