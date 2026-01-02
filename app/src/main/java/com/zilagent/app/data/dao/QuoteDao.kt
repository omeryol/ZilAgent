package com.zilagent.app.data.dao

import androidx.room.*
import com.zilagent.app.data.entity.Quote
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes")
    fun getAllQuotes(): Flow<List<Quote>>

    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotesSync(): List<Quote>

    @Query("SELECT COUNT(*) FROM quotes WHERE isSystem = 1")
    suspend fun getSystemQuotesCountSync(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: Quote)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<Quote>)

    @Delete
    suspend fun deleteQuote(quote: Quote)

    @Query("DELETE FROM quotes WHERE isSystem = 0")
    suspend fun deleteAllCustomQuotes()

    @Query("DELETE FROM quotes WHERE isSystem = 1")
    suspend fun deleteAllSystemQuotes()
}
