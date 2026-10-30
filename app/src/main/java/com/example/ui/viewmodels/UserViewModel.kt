package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LusalosRepository
import com.example.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: LusalosRepository) : ViewModel() {
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser = _currentUser.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    init {
        viewModelScope.launch { repository.observeProfile().collect { _currentUser.value = it } }
        load()
    }
    fun load() { viewModelScope.launch {
        try { repository.loadProfile(); _error.value = null }
        catch (e: Exception) { _error.value = "Could not open local data. Please try again." }
    } }
    fun updateProfile(username: String, avatar: String) { viewModelScope.launch {
        try { repository.updateProfile(username, avatar); _error.value = null }
        catch (e: Exception) { _error.value = "Could not save the name. Please try again." }
    } }
}
