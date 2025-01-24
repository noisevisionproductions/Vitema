package com.noisevisionsoftware.szytadieta.domain.navigation

import com.noisevisionsoftware.szytadieta.ui.base.AppEvent
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigationManager @Inject constructor(
    private val eventBus: EventBus
) {
    suspend fun navigateToScreen(screen: NavigationDestination) {
        eventBus.emit(AppEvent.Navigation(screen))
    }
}