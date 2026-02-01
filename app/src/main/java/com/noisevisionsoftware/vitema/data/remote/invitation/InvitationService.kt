package com.noisevisionsoftware.vitema.data.remote.invitation

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

interface InvitationService {
    @POST("/api/invitations/accept")
    suspend fun acceptInvitation(
        @Body request: AcceptInvitationRequest
    ): Response<MessageResponse>

    @DELETE("api/invitations/current-trainer")
    suspend fun disconnectFromTrainer(): Response<MessageResponse>
}