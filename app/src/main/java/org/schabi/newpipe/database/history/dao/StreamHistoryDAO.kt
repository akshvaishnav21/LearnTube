/*
 * SPDX-FileCopyrightText: 2018-2022 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.database.history.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import io.reactivex.rxjava3.core.Flowable
import org.schabi.newpipe.database.BasicDAO
import org.schabi.newpipe.database.history.model.StreamHistoryEntity
import org.schabi.newpipe.database.history.model.StreamHistoryEntry
import org.schabi.newpipe.database.stream.StreamStatisticsEntry

@Dao
abstract class StreamHistoryDAO : BasicDAO<StreamHistoryEntity> {

    @Query("SELECT * FROM stream_history")
    abstract override fun getAll(): Flowable<List<StreamHistoryEntity>>

    @Query("DELETE FROM stream_history")
    abstract override fun deleteAll(): Int

    override fun listByService(serviceId: Int): Flowable<List<StreamHistoryEntity>> {
        throw UnsupportedOperationException()
    }

    @get:Query("SELECT * FROM streams INNER JOIN stream_history ON uid = stream_id ORDER BY access_date DESC")
    abstract val history: Flowable<MutableList<StreamHistoryEntry>>

    @get:Query("SELECT * FROM streams INNER JOIN stream_history ON uid = stream_id ORDER BY uid ASC")
    abstract val historySortedById: Flowable<MutableList<StreamHistoryEntry>>

    @Query("SELECT * FROM stream_history WHERE stream_id = :streamId ORDER BY access_date DESC LIMIT 1")
    abstract fun getLatestEntry(streamId: Long): StreamHistoryEntity?

    @Query("DELETE FROM stream_history WHERE stream_id = :streamId")
    abstract fun deleteStreamHistory(streamId: Long): Int

    // Select the latest entry and watch count for each stream id on history table
    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
        SELECT * FROM streams

        INNER JOIN (
            SELECT stream_id, MAX(access_date) AS latestAccess, SUM(repeat_count) AS watchCount
            FROM stream_history
            GROUP BY stream_id
        )
        ON uid = stream_id

        LEFT JOIN (SELECT stream_id AS stream_id_alias, progress_time FROM stream_state )
        ON uid = stream_id_alias
        """
    )
    abstract fun getStatistics(): Flowable<MutableList<StreamStatisticsEntry>>

    /**
     * Returns the sum of duration of all watched streams (in seconds).
     * Uses streams that have at least one history entry.
     */
    @Query(
        """
        SELECT COALESCE(SUM(streams.duration), 0) FROM streams
        INNER JOIN (SELECT DISTINCT stream_id FROM stream_history) h ON uid = h.stream_id
        """
    )
    abstract fun getTotalWatchTimeSeconds(): Flowable<Long>

    /**
     * Returns the distinct UTC dates (as epoch-days) on which any stream was accessed.
     * We compute epoch_day = access_date_unix_seconds / 86400 in SQLite.
     * Room stores OffsetDateTime as an ISO string; we use date(access_date) to extract the date.
     */
    @Query(
        """
        SELECT DISTINCT date(access_date / 1000, 'unixepoch', 'localtime') AS watch_date
        FROM stream_history
        ORDER BY watch_date ASC
        """
    )
    abstract fun getDistinctWatchDates(): Flowable<MutableList<String>>

    /** Returns the count of distinct watched streams. */
    @Query("SELECT COUNT(DISTINCT stream_id) FROM stream_history")
    abstract fun getWatchedStreamCount(): Flowable<Long>

    /**
     * Total duration of streams watched this week (last 7 days), in seconds.
     */
    @Query(
        """
        SELECT COALESCE(SUM(streams.duration), 0) FROM streams
        INNER JOIN (
            SELECT DISTINCT stream_id FROM stream_history
            WHERE date(access_date / 1000, 'unixepoch', 'localtime') >= date('now', '-6 days')
        ) h ON uid = h.stream_id
        """
    )
    abstract fun getThisWeekWatchTimeSeconds(): Flowable<Long>
}
