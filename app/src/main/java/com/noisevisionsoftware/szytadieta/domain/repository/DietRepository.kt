package com.noisevisionsoftware.szytadieta.domain.repository

import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.Diet
import com.noisevisionsoftware.szytadieta.domain.service.dietService.DietService
import javax.inject.Inject

class DietRepository @Inject constructor(
    private val dietService: DietService,
    private val authRepository: AuthRepository
) {
    suspend fun getCurrentUserDiet(): Result<Diet?> = runCatching {
        val currentUser = authRepository.getCurrentUser()
            ?: throw AppException.AuthException("Użytkownik nie jest zalogowany")

        dietService.getUserDiets(currentUser.uid)
            .getOrThrow()
            .maxByOrNull { it.uploadedAt }
    }
}