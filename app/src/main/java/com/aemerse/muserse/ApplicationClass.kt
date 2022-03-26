package com.aemerse.muserse

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.aemerse.muserse.uiElementHelper.TypeFaceHelper
import com.aemerse.muserse.model.Constants
import com.aemerse.muserse.service.PlayerService
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        pref = PreferenceManager.getDefaultSharedPreferences(this)
        selectedThemeId = pref!!.getInt(getString(R.string.pref_theme_id), Constants.DEFAULT_THEME_ID)
        val path = TypeFaceHelper.getTypeFacePath()
        ViewPump.init(ViewPump.builder()
            .addInterceptor(CalligraphyInterceptor(
                CalligraphyConfig.Builder()
                    .setDefaultFontPath(path)
                    .setFontAttrId(R.attr.fontPath)
                    .build()))
            .build())
    }

    companion object {
        private var instance: ApplicationClass? = null
        private var pref: SharedPreferences? = null
        private var service: PlayerService? = null

        //music lock status flag
        private var isLocked: Boolean = false

        //check if app is in foreground
        //this is for button actions on bluetooth headset
        var isAppVisible: Boolean = false

        //batch lyrics download service status flag
        var isBatchServiceRunning: Boolean = false

        //user signed in or not status flag
        var hasUserSignedIn: Boolean = false

        //current selected theme id
        private var selectedThemeId: Int = 0

        fun getInstance(): ApplicationClass {
            return instance!!
        }

        fun getContext(): Context {
            return instance!!
        }

        fun getPref(): SharedPreferences {
            return pref!!
        }

        fun setService(s: PlayerService?) {
            service = s
        }

        fun getService(): PlayerService? {
            return service
        }

        fun isLocked(): Boolean {
            return isLocked
        }

        fun setLocked(lock: Boolean) {
            isLocked = lock
        }

        fun getSelectedThemeId(): Int {
            return selectedThemeId
        }

        fun setSelectedThemeId(selectedThemeId: Int) {
            pref!!.edit().putInt(getContext().getString(R.string.pref_theme_id), selectedThemeId).apply()
            Companion.selectedThemeId = selectedThemeId
        }
    }
}