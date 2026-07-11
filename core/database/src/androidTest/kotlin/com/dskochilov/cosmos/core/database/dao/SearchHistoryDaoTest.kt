package com.dskochilov.cosmos.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dskochilov.cosmos.core.database.CosmosDatabase
import com.dskochilov.cosmos.core.database.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchHistoryDaoTest {

    private lateinit var database: CosmosDatabase
    private lateinit var dao: SearchHistoryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CosmosDatabase::class.java
        ).build()
        dao = database.searchHistoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun recentSearches_areOrderedNewestFirst_andLimited() = runTest {
        (1..5).forEach { i ->
            dao.upsert(SearchHistoryEntity(query = "query-$i", updatedAt = i.toLong()))
        }

        val recent = dao.observeRecent(limit = 3).first()

        assertEquals(listOf("query-5", "query-4", "query-3"), recent)
    }

    @Test
    fun repeatingASearch_bumpsItToTheTop_withoutDuplicating() = runTest {
        dao.upsert(SearchHistoryEntity(query = "apollo", updatedAt = 1))
        dao.upsert(SearchHistoryEntity(query = "nebula", updatedAt = 2))
        dao.upsert(SearchHistoryEntity(query = "apollo", updatedAt = 3))

        val recent = dao.observeRecent(limit = 10).first()

        assertEquals(listOf("apollo", "nebula"), recent)
    }

    @Test
    fun clear_removesEverything() = runTest {
        dao.upsert(SearchHistoryEntity(query = "apollo", updatedAt = 1))

        dao.clear()

        assertEquals(emptyList<String>(), dao.observeRecent(limit = 10).first())
    }
}
