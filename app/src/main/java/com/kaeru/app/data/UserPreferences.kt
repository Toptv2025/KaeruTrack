package com.kaeru.app.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("kaeru_prefs", Context.MODE_PRIVATE)
    private val _userName = MutableStateFlow(prefs.getString("name", "Usuário") ?: "Usuário")
    val userName = _userName.asStateFlow()
    private val _userBio = MutableStateFlow(prefs.getString("bio", "Bio") ?: "Bio")
    val userBio = _userBio.asStateFlow()
    private val _userAvatar = MutableStateFlow(prefs.getString("avatar_uri", null))
    val userAvatar = _userAvatar.asStateFlow()
    fun saveName(name: String) {
        prefs.edit { putString("name", name) }
        _userName.value = name
    }
    fun saveBio(bio: String) {
        prefs.edit { putString("bio", bio) }
        _userBio.value = bio
    }
    fun saveAvatar(uri: String) {
        prefs.edit { putString("avatar_uri", uri) }
        _userAvatar.value = uri
    }
    private val LANGUAGE_KEY = "app_language"
    fun saveLanguage(languageCode: String) {
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply()
    }
    fun getLanguage(): String {
        return prefs.getString(LANGUAGE_KEY, "system") ?: "system"
    }
}