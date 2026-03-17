/*
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.database.stream.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stream_notes")
data class StreamNoteEntity(
    @PrimaryKey
    @ColumnInfo(name = "stream_uid")
    val streamUid: Long,

    @ColumnInfo(name = "note")
    val note: String?
)
