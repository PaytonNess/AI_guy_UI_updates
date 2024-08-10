package com.example.alguardianguyproject

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class SharedViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()

    // Optional: Add a method to clear the ViewModelStore when needed
    fun clearViewModelStore() {
        viewModelStore.clear()
    }
}