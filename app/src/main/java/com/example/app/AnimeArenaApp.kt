package com.example.app

import android.app.Application
import com.example.audio.AudioEngine
import com.example.storage.MatchRecordDatabase
import com.example.storage.MatchRecordRepository
import com.example.storage.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AnimeArenaApp : Application() {
  lateinit var settingsStore: SettingsDataStore
    private set

  lateinit var audioEngine: AudioEngine
    private set

  lateinit var matchRepository: MatchRecordRepository
    private set

  private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  override fun onCreate() {
    super.onCreate()
    settingsStore = SettingsDataStore(this)
    audioEngine = AudioEngine(this).also { it.initialize() }
    val db = MatchRecordDatabase.getInstance(this)
    matchRepository = MatchRecordRepository(db.matchRecordDao())

    appScope.launch {
      settingsStore.settingsFlow.collectLatest { settings ->
        audioEngine.updateSettings(settings)
      }
    }
  }

  override fun onTerminate() {
    audioEngine.release()
    super.onTerminate()
  }
}
