/*
 * SPDX-FileCopyrightText: 2025 NewPipe contributors <https://newpipe.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package org.schabi.newpipe.local.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.schabi.newpipe.NewPipeDatabase
import org.schabi.newpipe.R
import org.schabi.newpipe.util.Localization

/**
 * Displays learning streak and study time statistics derived from stream_history.
 */
class LearningStatsFragment : Fragment() {

    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_learning_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadStats(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear()
    }

    private fun loadStats(view: View) {
        val db = NewPipeDatabase.getInstance(requireContext())
        val historyDao = db.streamHistoryDAO()

        // Load streak and total watch time
        disposables.add(
            historyDao.getDistinctWatchDates()
                .first(mutableListOf())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { dates -> loadWatchTimeAndBind(view, dates) },
                    { /* silently ignore errors */ }
                )
        )

        // Load this-week hours
        disposables.add(
            historyDao.getThisWeekWatchTimeSeconds()
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { weekSeconds ->
                        view.findViewById<TextView>(R.id.learningStatsWeekTimeValue)
                            ?.text = getString(
                            R.string.learning_stats_hours_this_week,
                            weekSeconds / 3600.0
                        )
                    },
                    { /* silently ignore */ }
                )
        )
    }

    private fun loadWatchTimeAndBind(view: View, dates: List<String>) {
        val db = NewPipeDatabase.getInstance(requireContext())
        val historyDao = db.streamHistoryDAO()
        val streak = computeStreak(dates)
        disposables.add(
            io.reactivex.rxjava3.core.Single.zip(
                historyDao.getTotalWatchTimeSeconds().first(0L),
                historyDao.getWatchedStreamCount().first(0L),
                { totalSeconds: Long, videoCount: Long -> Pair(totalSeconds, videoCount) }
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { (totalSeconds, videoCount) -> bindViews(view, streak, totalSeconds, videoCount) },
                    { /* silently ignore */ }
                )
        )
    }

    private fun computeStreak(sortedDates: List<String>): Int {
        if (sortedDates.isEmpty()) return 0

        // Work backwards from today looking for consecutive days
        val today = java.time.LocalDate.now()
        val yesterday = today.minusDays(1)
        val parsedDates = sortedDates.mapNotNull { dateStr ->
            try {
                java.time.LocalDate.parse(dateStr)
            } catch (e: Exception) {
                null
            }
        }.distinct().sortedDescending()

        // Streak must start on today or yesterday
        val latestDate = parsedDates.firstOrNull() ?: return 0
        if (latestDate != today && latestDate != yesterday) return 0

        var streak = 1
        var expected = latestDate.minusDays(1)
        for (i in 1 until parsedDates.size) {
            if (parsedDates[i] == expected) {
                streak++
                expected = expected.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    private fun bindViews(
        view: View,
        streak: Int,
        totalSeconds: Long,
        videoCount: Long
    ) {
        view.findViewById<TextView>(R.id.learningStreakValue)?.text =
            getString(R.string.learning_streak_days, streak)

        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val watchTimeStr = if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
        view.findViewById<TextView>(R.id.learningWatchTimeValue)?.text = watchTimeStr
        view.findViewById<TextView>(R.id.learningVideosWatchedValue)?.text =
            videoCount.toString()
    }
}
