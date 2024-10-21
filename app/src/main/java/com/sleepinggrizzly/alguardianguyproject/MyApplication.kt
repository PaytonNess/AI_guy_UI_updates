package com.sleepinggrizzly.alguardianguyproject

import android.app.Application

class MyApplication : Application() {

    companion object {lateinit var sharedViewModelStoreOwner: SharedViewModelStoreOwner
        private set
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize your shared ViewModelStoreOwner here
        sharedViewModelStoreOwner = SharedViewModelStoreOwner()
    }
}