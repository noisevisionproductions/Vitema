package com.noisevisionsoftware.vitema.domain.repository

import android.util.Log
import com.noisevisionsoftware.vitema.data.remote.invitation.AcceptInvitationRequest
import com.noisevisionsoftware.vitema.data.remote.invitation.InvitationService
import javax.inject.Inject

class InvitationRepository @Inject constructor(
    private val invitationService: InvitationService
) {
    suspend fun acceptInvitation(code: String): Result<String> = runCatching {
        val response = invitationService.acceptInvitation(AcceptInvitationRequest(code))

        if (response.isSuccessful) {
            response.body()?.message ?: "Zaproszenie zaakceptowane"
        } else {
            val errorCode = response.code()
            val errorBody = response.errorBody()?.string()
            Log.e("InvitationRepo", "Serwer zwrócił błąd: $errorCode, Treść: $errorBody")

            val errorMsg = when (errorCode) {
                404 -> "Podany kod nie istnieje."
                409 -> "To zaproszenie zostało już użyte."
                410 -> "Kod zaproszenia wygasł."
                403 -> "Błąd autoryzacji (403) - sprawdź token."
                else -> "Błąd serwera ($errorCode). Sprawdź logi backendu."
            }
            throw Exception(errorMsg)
        }
    }

    suspend fun disconnectFromTrainer(): Result<Unit> = runCatching {
        val response = invitationService.disconnectFromTrainer()

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            Log.e("InvitationRepo", "Błąd rozłączania: ${response.code()}, $errorBody")
            throw Exception("Nie udało się zakończyć współpracy. Spróbuj ponownie.")
        }
    }
}