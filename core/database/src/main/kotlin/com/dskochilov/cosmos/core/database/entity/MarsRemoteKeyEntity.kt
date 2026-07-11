package com.dskochilov.cosmos.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Continuation state of the Mars photo feed for one query (rover + optional camera).
 * `nextSol == null` means the feed is exhausted.
 */
@Entity(tableName = "mars_remote_keys")
data class MarsRemoteKeyEntity(@PrimaryKey val queryKey: String, val nextSol: Int?, val nextPage: Int)
