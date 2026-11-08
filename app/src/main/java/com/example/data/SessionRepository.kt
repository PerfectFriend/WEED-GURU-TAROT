package com.example.data

import android.content.Context
import com.example.ui.theme.CyberSkin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionRepository private constructor(context: Context) {
    private val sharedPrefs = context.getSharedPreferences("cyber_session_prefs", Context.MODE_PRIVATE)

    private val _currentSkin = MutableStateFlow(
        CyberSkin.fromId(sharedPrefs.getString("selected_skin", "jamaica") ?: "jamaica")
    )
    val currentSkin: StateFlow<CyberSkin> = _currentSkin.asStateFlow()

    private val _isAmbientSoundEnabled = MutableStateFlow(
        sharedPrefs.getBoolean("ambient_sound", false)
    )
    val isAmbientSoundEnabled: StateFlow<Boolean> = _isAmbientSoundEnabled.asStateFlow()

    private val _isSafeBotanicalMode = MutableStateFlow(false)
    val isSafeBotanicalMode: StateFlow<Boolean> = _isSafeBotanicalMode.asStateFlow()

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    fun updateSkin(skin: CyberSkin) {
        sharedPrefs.edit().putString("selected_skin", skin.id).apply()
        _currentSkin.value = skin
    }

    fun toggleAmbientSound(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("ambient_sound", enabled).apply()
        _isAmbientSoundEnabled.value = enabled
    }

    fun setSafeBotanicalMode(enabled: Boolean) {
        _isSafeBotanicalMode.value = enabled
    }

    fun setActiveTab(tab: Int) {
        _activeTab.value = tab
    }

    companion object {
        @Volatile
        private var INSTANCE: SessionRepository? = null

        fun getInstance(context: Context): SessionRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
