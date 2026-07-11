package com.dskochilov.cosmos.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One remembered media-library search. The query itself is the key, so
 * re-searching a term just bumps it to the top via `updatedAt`.
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(@PrimaryKey val query: String, val updatedAt: Long)
