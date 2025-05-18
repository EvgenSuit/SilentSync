package com.suit.dndlocation.impl

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppForegroundWatcher: LifecycleEventObserver {
    private val _isInForeground = MutableStateFlow<Boolean>(false)
    val isInForeground = _isInForeground.asStateFlow()

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event
    ) {
        _isInForeground.value = event == Lifecycle.Event.ON_START ||
                source.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }
    
    fun startObserving() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    fun stopObserving() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }
}