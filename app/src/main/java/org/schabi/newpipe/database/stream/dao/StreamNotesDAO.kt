/*
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.database.stream.dao

import androidx.room.Dao
import androidx.room.Query
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

@Dao
interface StreamNotesDAO {

    @Query("SELECT note FROM stream_notes WHERE stream_uid = :streamUid")
    fun getNote(streamUid: Long): Maybe<String>

    @Query("INSERT OR REPLACE INTO stream_notes (stream_uid, note) VALUES (:streamUid, :note)")
    fun setNote(streamUid: Long, note: String?): Completable
}
