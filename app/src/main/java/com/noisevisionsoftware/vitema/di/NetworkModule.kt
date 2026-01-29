package com.noisevisionsoftware.vitema.di

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.noisevisionsoftware.vitema.data.remote.RecipeService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor { chain ->
            val originalRequest = chain.request()

            val token = try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val tokenResult = currentUser?.getIdToken(false)?.result
                tokenResult?.token ?: ""
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
        return Retrofit.Builder().baseUrl("http://vitema.pl/").client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    @Provides
    @Singleton
    fun provideRecipeService(retrofit: Retrofit): RecipeService {
        return retrofit.create(RecipeService::class.java)
    }
}