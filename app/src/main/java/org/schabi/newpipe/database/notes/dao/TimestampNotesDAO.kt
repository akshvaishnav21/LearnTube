/*
 * SPDX-FileCopyrightText: 2025 NewPipe contributors <https://newpipe.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.database.notes.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import org.schabi.newpipe.database.notes.model.TimestampNoteEntity

@Dao
interface TimestampNotesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: TimestampNoteEntity): Long

    @Delete
    fun delete(note: TimestampNoteEntity): Int

    @Query("DELETE FROM ${TimestampNoteEntity.TABLE_NAME} WHERE ${TimestampNoteEntity.UID} = :uid")
    fun deleteById(uid: Long): Int

    @Query(
        "SELECT * FROM ${TimestampNoteEntity.TABLE_NAME}" +
            " WHERE ${TimestampNoteEntity.STREAM_UID} = :streamUid" +
            " ORDER BY ${TimestampNoteEntity.TIMESTAMP_MS} ASC"
    )
    fun getNotesForStream(streamUid: Long): Flowable<List<TimestampNoteEntity>>

    @Query("DELETE FROM ${TimestampNoteEntity.TABLE_NAME}")
    fun deleteAll(): Int
}
