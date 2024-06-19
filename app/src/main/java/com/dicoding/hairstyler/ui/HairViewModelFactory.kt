package com.dicoding.hairstyler.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.hairstyler.data.repository.HairRepositoryImpl
import com.dicoding.hairstyler.data.repository.UserRepositoryImpl
import com.dicoding.hairstyler.di.Injection
import com.dicoding.hairstyler.ui.detail.DetailViewModel
import com.dicoding.hairstyler.ui.home.HomeViewModel
import com.dicoding.hairstyler.ui.savedhairstyle.SavedHairstyleViewModel

class HairViewModelFactory(
    private val repositoryImpl: UserRepositoryImpl,
    private val hairRepositoryImpl: HairRepositoryImpl
) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repositoryImpl, hairRepositoryImpl) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(hairRepositoryImpl) as T
            }
            modelClass.isAssignableFrom(SavedHairstyleViewModel::class.java) -> {
                SavedHairstyleViewModel(hairRepositoryImpl) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HairViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): HairViewModelFactory {
            if (INSTANCE == null) {
                synchronized(HairViewModelFactory::class.java) {
                    INSTANCE = HairViewModelFactory(
                        Injection.provideUserRepository(context),
                        Injection.provideHairRepository(context)
                    )
                }
            }
            return INSTANCE as HairViewModelFactory
        }
    }
}