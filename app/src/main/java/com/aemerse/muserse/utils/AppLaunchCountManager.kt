package com.aemerse.muserse.utils

import android.content.Context
import android.content.SharedPreferences
import com.aemerse.muserse.ApplicationClass

object AppLaunchCountManager {
    fun app_launched(mContext: Context) {
        val prefs = mContext.getSharedPreferences("apprater", 0)
        val editor = prefs.edit()

        // Increment launch counter
        val launchCount = prefs.getLong("launch_count", 0) + 1
        editor.putLong("launch_count", launchCount)

        // Get date of first launch
        var dateFirstlaunch = prefs.getLong("date_firstlaunch", 0)
        if (dateFirstlaunch == 0L) {
            dateFirstlaunch = System.currentTimeMillis()
            editor.putLong("date_firstlaunch", dateFirstlaunch)
        }
        editor.apply()
    }

    fun nowPlayingLaunched() {
        val prefs = ApplicationClass.getContext().getSharedPreferences("apprater", 0)
        val editor = prefs!!.edit()

        // Increment launch counter
        val launchCount = prefs.getLong("launch_count_now_playing", 0) + 1
        editor!!.putLong("launch_count_now_playing", launchCount)
        editor.apply()
    }

    val nowPlayingLaunchCount: Long
        get() {
            val prefs: SharedPreferences = ApplicationClass.getContext().getSharedPreferences("apprater", 0)
                ?: return -1
            return prefs.getLong("launch_count_now_playing", -1)
        }

    fun instantLyricsLaunched() {
        val prefs: SharedPreferences = ApplicationClass.getContext().getSharedPreferences("apprater", 0)
        val editor = prefs.edit()

        // Increment launch counter
        val launchCount = prefs.getLong("launch_count_instantLyrics", 0) + 1
        editor.putLong("launch_count_instantLyrics", launchCount)
        editor.apply()
    }

    val instantLyricsCount: Long
        get() {
            val prefs: SharedPreferences = ApplicationClass.getContext().getSharedPreferences("apprater", 0)
                ?: return -1
            return prefs.getLong("launch_count_instantLyrics", -1)
        }
}