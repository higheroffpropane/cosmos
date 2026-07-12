package com.dskochilov.cosmos.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dskochilov.cosmos.core.database.CosmosDatabase
import com.dskochilov.cosmos.core.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {

    private lateinit var database: CosmosDatabase
    private lateinit var dao: FavoriteDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CosmosDatabase::class.java
        ).build()
        dao = database.favoriteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun favorites_areOrderedNewestFirst() = runTest {
        dao.insert(favorite(id = "apod:2026-01-01", savedAt = 1))
        dao.insert(favorite(id = "mars:abc", savedAt = 3))
        dao.insert(favorite(id = "apod:2026-02-02", savedAt = 2))

        val all = dao.observeAll().first()

        assertEquals(listOf("mars:abc", "apod:2026-02-02", "apod:2026-01-01"), all.map { it.id })
    }

    @Test
    fun observeExists_reflectsInsertAndDelete() = runTest {
        val id = "apod:2026-07-11"
        assertFalse(dao.observeExists(id).first())

        dao.insert(favorite(id = id, savedAt = 1))
        assertTrue(dao.observeExists(id).first())

        dao.deleteById(id)
        assertFalse(dao.observeExists(id).first())
    }

    @Test
    fun insertingSameId_replacesInsteadOfDuplicating() = runTest {
        dao.insert(favorite(id = "apod:2026-07-11", savedAt = 1, title = "Old"))
        dao.insert(favorite(id = "apod:2026-07-11", savedAt = 2, title = "New"))

        val all = dao.observeAll().first()

        assertEquals(1, all.size)
        assertEquals("New", all.single().title)
    }

    @Test
    fun exists_matchesSuspendAndFlowVariants() = runTest {
        dao.insert(favorite(id = "mars:xyz", savedAt = 1))

        assertTrue(dao.exists("mars:xyz"))
        assertFalse(dao.exists("mars:other"))
    }

    private fun favorite(id: String, savedAt: Long, title: String = "Title") = FavoriteEntity(
        id = id,
        type = if (id.startsWith("apod")) FavoriteEntity.TYPE_APOD else FavoriteEntity.TYPE_MARS_PHOTO,
        title = title,
        imageUrl = "https://example.com/i.jpg",
        thumbnailUrl = "https://example.com/t.jpg",
        savedAt = savedAt
    )
}
