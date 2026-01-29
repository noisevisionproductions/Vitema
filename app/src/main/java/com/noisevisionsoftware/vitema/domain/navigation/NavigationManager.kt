package com.noisevisionsoftware.vitema.domain.navigation

import com.noisevisionsoftware.vitema.ui.base.AppEvent
import com.noisevisionsoftware.vitema.ui.base.EventBus
import com.noisevisionsoftware.vitema.ui.navigation.NavigationDestination
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