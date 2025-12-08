package com.payfunds.wallet.modules.settings.security.twofactorauth

import android.content.Context
import android.content.SharedPreferences


class CrateUserTokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        private const val TOKEN_KEY = "two_factor_auth_token"
    }

    fun createUserSaveToken(token: String?) {
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    fun crateUserGetToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    fun crateUserClearToken() {
        editor.remove(TOKEN_KEY)
        editor.apply()
    }
}