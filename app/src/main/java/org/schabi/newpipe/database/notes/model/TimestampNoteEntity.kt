/*
 * SPDX-FileCopyrightText: 2025 NewPipe contributors <https://newpipe.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.database.notes.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import org.schabi.newpipe.database.stream.model.StreamEntity

@Entity(
    tableName = TimestampNoteEntity.TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = StreamEntity::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf(TimestampNoteEntity.STREAM_UID),
            onDelete = CASCADE,
            onUpdate = CASCADE
        )
    ],
    indices = [Index(value = [TimestampNoteEntity.STREAM_UID])]
)
data class TimestampNoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = UID)
    val uid: Long = 0,

    @ColumnInfo(name = STREAM_UID)
    val streamUid: Long,

    @ColumnInfo(name = TIMESTAMP_MS, defaultValue = "0")
    val timestampMs: Long,

    @ColumnInfo(name = NOTE)
    val note: String,

    @ColumnInfo(name = CREATED_AT)
    val createdAt: Long
) {
    companion object {
        const val TABLE_NAME = "stream_timestamp_notes"
        const val UID = "uid"
        const val STREAM_UID = "stream_uid"
        const val TIMESTAMP_MS = "timestamp_ms"
        const val NOTE = "note"
        const val CREATED_AT = "created_at"
    }
}
