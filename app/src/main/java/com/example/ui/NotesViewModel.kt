package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DecryptedNote
import com.example.data.NoteDatabase
import com.example.data.NoteRepository
import com.example.security.SecurePreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ScreenState {
    object Splash : ScreenState()
    object OnboardingIntro : ScreenState()
    object CreatePin : ScreenState()
    data class ConfirmPin(val proposedPin: String) : ScreenState()
    object LockScreen : ScreenState()
    object NotesList : ScreenState()
    data class EditNote(val noteId: Int?) : ScreenState()
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val db = NoteDatabase.getDatabase(application)
    private val repository = NoteRepository(db.noteDao)
    private val prefs = SecurePreferences(application)

    // UI state streams
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Splash)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _isLocked = MutableStateFlow(true)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _hasSetPin = MutableStateFlow(false)
    val hasSetPin: StateFlow<Boolean> = _hasSetPin.asStateFlow()

    // Preferences
    private val _biometricsEnabled = MutableStateFlow(true)
    val biometricsEnabled: StateFlow<Boolean> = _biometricsEnabled.asStateFlow()

    private val _selfDestructEnabled = MutableStateFlow(false)
    val selfDestructEnabled: StateFlow<Boolean> = _selfDestructEnabled.asStateFlow()

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _isDarkMode = MutableStateFlow<Boolean?>(null)
    val isDarkMode: StateFlow<Boolean?> = _isDarkMode.asStateFlow()

    private val _themeSelection = MutableStateFlow("sage")
    val themeSelection: StateFlow<String> = _themeSelection.asStateFlow()

    // Notes data
    val decryptedNotes: StateFlow<List<DecryptedNote>> = repository.allNotesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered Notes
    val filteredNotes = combine(decryptedNotes, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.title.contains(query, ignoreCase = true) || 
                it.content.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Note being viewed / edited
    private val _activeNote = MutableStateFlow<DecryptedNote?>(null)
    val activeNote: StateFlow<DecryptedNote?> = _activeNote.asStateFlow()

    // Lock screen authentication status message
    private val _loginErrorMessage = MutableStateFlow<String?>(null)
    val loginErrorMessage: StateFlow<String?> = _loginErrorMessage.asStateFlow()

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        val pinExists = prefs.hasSetPin()
        _hasSetPin.value = pinExists
        _biometricsEnabled.value = prefs.isBiometricsEnabled
        _selfDestructEnabled.value = prefs.isSelfDestructEnabled
        _failedAttempts.value = prefs.failedAttempts
        _isDarkMode.value = prefs.getDarkModePreference()
        _themeSelection.value = prefs.getThemePreference()

        if (!pinExists) {
            _screenState.value = ScreenState.OnboardingIntro
            _isLocked.value = false
        } else {
            _screenState.value = ScreenState.LockScreen
            _isLocked.value = true
        }
    }

    fun navigateTo(state: ScreenState) {
        _screenState.value = state
    }

    fun toggleDarkMode(overrideDark: Boolean?) {
        _isDarkMode.value = overrideDark
        prefs.setDarkModePreference(overrideDark)
    }

    // Pin setups
    fun setProposedPin(pin: String) {
        _screenState.value = ScreenState.ConfirmPin(pin)
    }

    fun confirmAndSavePin(proposed: String, confirmed: String): Boolean {
        if (proposed == confirmed) {
            val saved = prefs.savePin(confirmed)
            if (saved) {
                _hasSetPin.value = true
                _screenState.value = ScreenState.NotesList
                _isLocked.value = false
                return true
            }
        }
        return false
    }

    // Pin Verification
    fun attemptUnlock(pin: String): Boolean {
        val success = prefs.verifyPin(pin)
        _failedAttempts.value = prefs.failedAttempts
        
        if (success) {
            _isLocked.value = false
            _loginErrorMessage.value = null
            _screenState.value = ScreenState.NotesList
        } else {
            if (prefs.shouldSelfDestruct()) {
                performSelfDestruct()
            } else {
                val rem = 5 - prefs.failedAttempts
                if (rem <= 0) {
                    performSelfDestruct()
                } else {
                    _loginErrorMessage.value = "Incorrect PIN. $rem attempts remaining!"
                }
            }
        }
        return success
    }

    // Biometric Unlock Success
    fun unlockWithBiometrics() {
        prefs.resetFailedAttempts()
        _failedAttempts.value = 0
        _isLocked.value = false
        _loginErrorMessage.value = null
        _screenState.value = ScreenState.NotesList
    }

    fun lockApp() {
        _isLocked.value = true
        _screenState.value = ScreenState.LockScreen
    }

    // Note actions
    fun startNewNote() {
        _activeNote.value = DecryptedNote()
        _screenState.value = ScreenState.EditNote(null)
    }

    fun startEditNote(note: DecryptedNote) {
        _activeNote.value = note
        _screenState.value = ScreenState.EditNote(note.id)
    }

    fun updateActiveNote(
        title: String? = null,
        content: String? = null,
        colorHex: String? = null,
        isPinned: Boolean? = null,
        isSensitive: Boolean? = null
    ) {
        val current = _activeNote.value ?: return
        _activeNote.value = current.copy(
            title = title ?: current.title,
            content = content ?: current.content,
            colorHex = colorHex ?: current.colorHex,
            isPinned = isPinned ?: current.isPinned,
            isSensitive = isSensitive ?: current.isSensitive,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun saveActiveNote() {
        val noteToSave = _activeNote.value ?: return
        if (noteToSave.title.isBlank() && noteToSave.content.isBlank()) {
            _screenState.value = ScreenState.NotesList
            return
        }
         viewModelScope.launch {
            repository.saveNote(noteToSave)
            _screenState.value = ScreenState.NotesList
            _activeNote.value = null
        }
    }

    fun autoSaveActiveNoteSilently(onSavedComplete: () -> Unit = {}) {
        val noteToSave = _activeNote.value ?: return
        if (noteToSave.title.isBlank() && noteToSave.content.isBlank()) {
            return
        }
        viewModelScope.launch {
            val savedId = repository.saveNote(noteToSave)
            if (noteToSave.id == 0 && savedId > 0) {
                _activeNote.value = _activeNote.value?.copy(id = savedId.toInt())
            }
            onSavedComplete()
        }
    }

    fun deleteActiveNote() {
        val noteToDelete = _activeNote.value ?: return
        viewModelScope.launch {
            if (noteToDelete.id != 0) {
                repository.deleteNote(noteToDelete)
            }
            _screenState.value = ScreenState.NotesList
            _activeNote.value = null
        }
    }

    fun deleteNoteDirectly(note: DecryptedNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Settings Configuration
    fun setBiometricsEnabled(enabled: Boolean) {
        prefs.setBiometricsEnabled(enabled)
        _biometricsEnabled.value = enabled
    }

    fun setSelfDestructEnabled(enabled: Boolean) {
        prefs.setSelfDestructEnabled(enabled)
        _selfDestructEnabled.value = enabled
    }

    fun setThemeSelection(theme: String) {
        prefs.setThemePreference(theme)
        _themeSelection.value = theme
    }

    // Secure local self-destruction execution
    private fun performSelfDestruct() {
        viewModelScope.launch {
            repository.clearAllNotes()
            prefs.clearAllSecureData()
            _failedAttempts.value = 0
            _hasSetPin.value = false
            _isLocked.value = true
            _screenState.value = ScreenState.OnboardingIntro
            _loginErrorMessage.value = "Device data immediately destroyed due to consecutive invalid entries!"
        }
    }

    fun handleSignOutWipe() {
        viewModelScope.launch {
            repository.clearAllNotes()
            prefs.clearAllSecureData()
            checkInitialState()
        }
    }
}
