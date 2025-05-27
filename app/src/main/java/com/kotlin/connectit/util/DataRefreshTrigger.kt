package com.kotlin.connectit.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class DataRefreshTrigger @Inject constructor() {
    private val _onDataChanged = MutableSharedFlow<Unit>(replay = 0)
    val onDataChanged = _onDataChanged.asSharedFlow()

    suspend fun triggerRefresh() {
        _onDataChanged.emit(Unit)
    }
}
