package com.aemerse.muserse.activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.aemerse.muserse.ApplicationClass
import com.aemerse.muserse.R
import com.aemerse.muserse.model.Constants
import com.aemerse.muserse.service.PlayerService
import com.afollestad.materialdialogs.MaterialDialog
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class ActivityPermissionSeek : AppCompatActivity() {
    private val MY_PERMISSIONS_REQUEST: Int = 0
    private var mBound: Boolean = false

    private val playerServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(arg0: ComponentName, service: IBinder) {
            val playerBinder = service as PlayerService.PlayerBinder
            val playerService: PlayerService = playerBinder.getService()
            ApplicationClass.setService(playerService)
            mBound = true
            Log.v(Constants.TAG, "LAUNCH MAIN ACTIVITY")
            startActivity(Intent(this@ActivityPermissionSeek, ActivityRequestNotificationAccess::class.java))
            finish()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //check version and make changes if any

        /*
          This will finish your Launcher Activity before it is displayed by detecting that there is already a task running,
          and app should instead resume to the last visible Activity.
          https://stackoverflow.com/a/21022876/5430666
         */if ((intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish()
            return
        }

        //setContentView(R.layout.splash_activity);
        if (!hasPermissions(this, *PERMISSIONS)) {
            try {
                permissionDetailsDialog()
            } catch (e: Exception) {
                requestPermission()
            }
        } else {
            bindService()
        }
        setNotificationChannelForOreoPlus()

        //if(MyApp.getPref().getBoolean(getString(R.string.pref_first_install),true)) {
        //checkForDeepLink();
        //}
        changeSettingsForVersion()
    }

    private fun permissionDetailsDialog() {
        MaterialDialog(this)
            .title(R.string.permission_details_title)
            .message(R.string.permission_details_content)
            .positiveButton(R.string.permission_details_pos){
                requestPermission()
            }
            .negativeButton(R.string.cancel){
                finish()
            }
            .cancelable(false)
            .show()
    }

    private fun setNotificationChannelForOreoPlus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                /* Create or update. */
                val channel = NotificationChannel("channel_01",
                    "Playback Notification",
                    NotificationManager.IMPORTANCE_LOW)
                channel.setSound(null, null)
                val channel2 = NotificationChannel("channel_02",
                    "Instant Lyrics",
                    NotificationManager.IMPORTANCE_LOW)
                channel.setSound(null, null)
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                    channel)
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                    channel2)
            }
        } catch (ignored: Exception) {
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun requestPermission() {
        // Here, thisActivity is the current activity
        ActivityCompat.requestPermissions(this,
            PERMISSIONS,
            MY_PERMISSIONS_REQUEST)
    }

    private fun changeSettingsForVersion() {

        //if first install
        if (ApplicationClass.getPref().getBoolean(getString(R.string.pref_first_install), true)) {
            ApplicationClass.getPref().edit().putBoolean(getString(R.string.pref_first_install), false).apply()
            ApplicationClass.getPref().edit().putInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.GLOSSY).apply()
            ApplicationClass.getPref().edit().putInt(getString(R.string.pref_theme_color), Constants.PRIMARY_COLOR.BLACK).apply()
            ApplicationClass.getPref().edit().putBoolean(getString(R.string.pref_prefer_system_equ), false).apply()
            ApplicationClass.getPref().edit().putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MANROPE).apply()
            ApplicationClass.getPref().edit().putInt(getString(R.string.pref_theme_id), Constants.DEFAULT_THEME_ID).apply()
            ApplicationClass.getPref().edit().putInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MANROPE).apply()
        }

        //disable lock screen album art
        setDeprecatedPreferencesValues()
    }

    private fun setDeprecatedPreferencesValues() {
        if (ApplicationClass.getPref().getInt(getString(R.string.pref_click_on_notif), -1) != Constants.CLICK_ON_NOTIF.OPEN_DISC_VIEW) {
            ApplicationClass.getPref().edit().putInt(getString(R.string.pref_click_on_notif), Constants.CLICK_ON_NOTIF.OPEN_DISC_VIEW).apply()
        }

        //REMOVED PREFERENCES
        if (ApplicationClass.getPref().getFloat(getString(R.string.pref_disc_size), -1f) != Constants.DISC_SIZE.MEDIUM) {
            ApplicationClass.getPref().edit().putFloat(getString(R.string.pref_disc_size), Constants.DISC_SIZE.MEDIUM).apply()
        }

        /*if(!MyApp.getPref().getBoolean(getString(R.string.pref_album_lib_view)
                ,false)){
            MyApp.getPref().edit().putBoolean(getString(R.string.pref_album_lib_view)
                    ,true).apply();
        }*/
    }

    private fun bindService() {
        //initialize music library instance
        // MusicLibrary.getInstance();
        startService(Intent(this, PlayerService::class.java))
        try {
            val playerServiceIntent = Intent(this, PlayerService::class.java)
            bindService(playerServiceIntent, playerServiceConnection, Context.BIND_AUTO_CREATE)
        } catch (ignored: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (mBound) {
                unbindService(playerServiceConnection)
                mBound = false
            }
        } catch (ignored: Exception) {
        }
    }

    private fun logFont() {
        if (ApplicationClass.getPref().getBoolean(getString(R.string.pref_font_already_logged), false)) {
            return
        }
        try {
            val textFontPref: Int = ApplicationClass.getPref().getInt(getString(R.string.pref_text_font), Constants.TYPEFACE.MONOSPACE)
            var fontString: String? = ""
            when (textFontPref) {
                Constants.TYPEFACE.MONOSPACE -> fontString = "MONOSPACE"
                Constants.TYPEFACE.SOFIA -> fontString = "SOFIA"
                Constants.TYPEFACE.SYSTEM_DEFAULT -> fontString = "SYSTEM_DEFAULT"
                Constants.TYPEFACE.MANROPE -> fontString = "MANROPE"
                Constants.TYPEFACE.ASAP -> fontString = "ASAP"
            }
            ApplicationClass.getPref().edit().putBoolean(getString(R.string.pref_font_already_logged), true).apply()
        } catch (ignored: Exception) {
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                if (grantResults.isEmpty()) {
                    return
                }
                // If request is cancelled, the result arrays are empty.
                when {
                    (grantResults.isNotEmpty()) && (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)
                        //&& grantResults[2] == PackageManager.PERMISSION_GRANTED
                    -> {
                        bindService()
                    }
                    else -> {
                        when (PackageManager.PERMISSION_DENIED) {
                            grantResults[0] -> {
                                //READ PHONE STATE DENIED
                                val intent = Intent(Intent.ACTION_MAIN)
                                intent.addCategory(Intent.CATEGORY_HOME)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                Toast.makeText(this,
                                    getString(R.string.phone_stat_perm_required),
                                    Toast.LENGTH_LONG).show()
                                finish()
                            }
                            grantResults[1] -> {
                                val intent = Intent(Intent.ACTION_MAIN)
                                intent.addCategory(Intent.CATEGORY_HOME)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                Toast.makeText(this,
                                    getString(R.string.storage_perm_required),
                                    Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val PERMISSIONS: Array<String> = arrayOf(Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (context != null)) {
                for (permission: String? in permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission!!) != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }
            }
            return true
        }
    }
}