package com.noisevisionsoftware.vitema.di

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.noisevisionsoftware.vitema.data.remote.RecipeService
import com.noisevisionsoftware.vitema.data.remote.invitation.InvitationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder().addInterceptor(logging).addInterceptor { chain ->
                val originalRequest = chain.request()
                val token = try {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        val tokenResult =
                            com.google.android.gms.tasks.Tasks.await(currentUser.getIdToken(true))
                        tokenResult.token ?: ""
                    } else ""
                } catch (e: Exception) {
                    Log.e("NetworkModule", "Error getting user token", e)
                    ""
                }

                val authRequest =
                    originalRequest.newBuilder().addHeader("Authorization", "Bearer $token").build()
                chain.proceed(authRequest)
            }.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl("https://vitema.pl/").client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    @Provides
    @Singleton
    fun provideRecipeService(retrofit: Retrofit): RecipeService {
        return retrofit.create(RecipeService::class.java)
    }

    @Provides
    @Singleton
    fun provideInvitationService(retrofit: Retrofit): InvitationService {
        return retrofit.create(InvitationService::class.java)
    }
}