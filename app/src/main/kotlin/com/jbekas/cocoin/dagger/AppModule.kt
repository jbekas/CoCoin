package com.jbekas.cocoin.dagger

import android.content.Context
import com.jbekas.cocoin.service.ToastService
import com.jbekas.cocoin.util.CoCoinUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    @Named("String1")
    fun provideTestString1() = "This is a string we will inject"

    @Singleton
    @Provides
    fun provideToastService(@ApplicationContext appContext: Context): ToastService {
        return ToastService(appContext)
    }

    @Singleton
    @Provides
    fun provideCoCoinUtil(@ApplicationContext appContext: Context): CoCoinUtil {
        return CoCoinUtil(appContext)
    }
}