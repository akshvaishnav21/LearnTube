/*
 * SPDX-FileCopyrightText: 2025 NewPipe contributors <https://newpipe.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.database.playlist

import androidx.room.ColumnInfo

/**
 * Holds per-playlist progress counts returned by [PlaylistStreamDAO.getPlaylistProgressCounts].
 */
data class PlaylistProgressEntry(
    @ColumnInfo(name = "playlistId")
    val playlistId: Long,

    @ColumnInfo(name = "totalCount")
    val totalCount: Int,

    @ColumnInfo(name = "watchedCount")
    val watchedCount: Int
)
