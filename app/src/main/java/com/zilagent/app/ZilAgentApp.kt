package com.zilagent.app

import android.app.Application

import com.zilagent.app.data.AppDatabase
import com.zilagent.app.data.entity.Quote
import com.zilagent.app.util.QuoteConstants
import com.zilagent.app.util.SubjectConstants
import com.zilagent.app.widget.WidgetStore
import com.zilagent.app.worker.DailyRefreshWorker
import com.zilagent.app.worker.WidgetHeartbeatWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZilAgentApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Schedule WorkManager task for daily alarm refresh
        DailyRefreshWorker.schedule(this)
        WidgetHeartbeatWorker.schedule(this)

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@ZilAgentApp)
            val dao = db.quoteDao()
            val language = WidgetStore.getAppLanguage(this@ZilAgentApp)
            dao.deleteAllSystemQuotes()
            val systemQuotes = QuoteConstants.systemQuotes(language).map { Quote(content = it, isSystem = true) }
            dao.insertQuotes(systemQuotes)

            // Sync System Subjects
            val syllabusDao = db.syllabusDao()
            if (syllabusDao.getSystemSubjectCount() == 0) {
                val defaults = if (language == "en") SubjectConstants.MIDDLE_SCHOOL_SUBJECTS_EN else SubjectConstants.MIDDLE_SCHOOL_SUBJECTS_TR
                defaults.forEach {
                    syllabusDao.insertSubject(com.zilagent.app.data.entity.SchoolSubject(name = it, isSystem = true))
                }
            }
        }
    }
}
