package com.example.demoapp.Activities

import android.content.SearchRecentSuggestionsProvider

class MySuggestionProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.example.demoapp.Activities.MySuggestionProvider"
        const val MODE: Int = DATABASE_MODE_QUERIES
    }
}