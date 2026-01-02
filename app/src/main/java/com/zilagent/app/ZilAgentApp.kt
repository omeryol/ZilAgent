package com.zilagent.app

import android.app.Application

import com.zilagent.app.data.AppDatabase
import com.zilagent.app.data.entity.Quote
import com.zilagent.app.util.QuoteConstants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ZilAgentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        GlobalScope.launch {
            val db = AppDatabase.getDatabase(this@ZilAgentApp)
            val dao = db.quoteDao()
            val currentSystemCount = dao.getSystemQuotesCountSync()
            
            // If the count in DB differs from the one in Code (QuoteConstants), update it.
            if (currentSystemCount != QuoteConstants.HOLIDAY_QUOTES.size) {
                dao.deleteAllSystemQuotes()
                val systemQuotes = QuoteConstants.HOLIDAY_QUOTES.map { Quote(content = it, isSystem = true) }
                dao.insertQuotes(systemQuotes)
            }
        }
    }
}
