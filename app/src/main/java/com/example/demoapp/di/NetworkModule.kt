package com.example.demoapp.di

import com.example.demoapp.API.UserAPI
import com.example.demoapp.Constants.BASE_URL
import com.example.demoapp.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideApiService(): UserAPI {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(UserAPI::class.java)
    }

    @Provides
    fun provideYourRepository(apiService: UserAPI): UserRepository {
        return UserRepository(apiService)
    }
}