package com.aemerse.muserse.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.aemerse.iap.DataWrappers
import com.aemerse.iap.IapConnector
import com.aemerse.iap.PurchaseServiceListener
import com.aemerse.muserse.ApplicationClass
import com.aemerse.muserse.R
import com.aemerse.muserse.model.Constants
import com.aemerse.muserse.uiElementHelper.ColorHelper
import com.aemerse.muserse.utils.UtilityFun
import com.afollestad.materialdialogs.MaterialDialog
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class ActivityAboutUs : AppCompatActivity() {

    private lateinit var iapConnector: IapConnector
    private lateinit var beer: String
    private lateinit var beerBox: String
    private lateinit var coffee: String

    override fun onCreate(savedInstanceState: Bundle?) {
        //if player service not running, kill the app
        if (ApplicationClass.getService() == null) {
            UtilityFun.restartApp()
        }
        ColorHelper.setStatusBarGradiant(this)
        when (ApplicationClass.getPref().getInt(getString(R.string.pref_theme), Constants.PRIMARY_COLOR.LIGHT)) {
            Constants.PRIMARY_COLOR.DARK -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.GLOSSY -> setTheme(R.style.AppThemeDark)
            Constants.PRIMARY_COLOR.LIGHT -> setTheme(R.style.AppThemeLight)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        //findViewById(R.id.root_view_about_us).setBackgroundDrawable(ColorHelper.GetGradientDrawableDark());
        val pInfo: PackageInfo
        try {
            pInfo = packageManager.getPackageInfo(packageName, 0)
            val version: String = pInfo.versionName
            (findViewById<View>(R.id.version) as TextView).text = version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar_)
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        if (supportActionBar != null) {
            //getSupportActionBar().setBackgroundDrawable(ColorHelper.GetGradientDrawableToolbar());
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ColorHelper.GetStatusBarColor());
        }*/
        title = getString(R.string.title_about_us)
        coffee = getString(R.string.donate_coffee)
        beerBox = getString(R.string.donate_beer_box)
        beerBox = getString(R.string.donate_beer)

        val iapConnector = IapConnector(
            context = this,
            consumableKeys = listOf(beer, beerBox, coffee)
        )

        iapConnector.addPurchaseListener(object : PurchaseServiceListener {
            override fun onPricesUpdated(iapKeyPrices: Map<String, DataWrappers.SkuDetails>) {
                // list of available products will be received here, so you can update UI with prices if needed
            }

            override fun onProductPurchased(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered whenever purchase succeeded
                when (purchaseInfo.sku) {
                    beer -> {
                        startActivity(Intent(this@ActivityAboutUs,
                            ActivitySettings::class.java))
                        finish()
                        return
                    }
                    beerBox -> {
                        startActivity(Intent(this@ActivityAboutUs,
                            ActivitySettings::class.java))
                        finish()
                        return
                    }
                    coffee -> {
                        startActivity(Intent(this@ActivityAboutUs,
                            ActivitySettings::class.java))
                        finish()
                        return
                    }
                }
            }

            override fun onProductRestored(purchaseInfo: DataWrappers.PurchaseInfo) {
                // will be triggered fetching owned products using IapConnector
            }
        })
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY -> ApplicationClass.getService()!!.play()
            KeyEvent.KEYCODE_MEDIA_NEXT -> ApplicationClass.getService()!!.nextTrack()
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> ApplicationClass.getService()!!.prevTrack()
            KeyEvent.KEYCODE_MEDIA_STOP -> ApplicationClass.getService()!!.stop()
            KeyEvent.KEYCODE_BACK -> onBackPressed()
        }
        return false
    }

    override fun onResume() {
        ApplicationClass.isAppVisible = true
        super.onResume()
    }

    override fun onPause() {
        ApplicationClass.isAppVisible = false
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> finish()
            R.id.action_feedback -> {
                val myDeviceModel = Build.MODEL
                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", getString(R.string.au_email_id), null))
                val address: Array<String> = arrayOf(getString(R.string.au_email_id))
                emailIntent.putExtra(Intent.EXTRA_EMAIL, address)
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for $myDeviceModel")
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello AndroidDevs, \n")
                startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
            }
            R.id.action_support_dev -> selectDonateDialog()
            R.id.action_tou -> showDisclaimerDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun selectDonateDialog() {
       MaterialDialog(this)
            .title(R.string.about_us_support_dev_title)
            .message(R.string.about_us_support_dev_content)
            .positiveButton(R.string.about_us_support_dev_pos){
                iapConnector.purchase(this, coffee)
            }
            .negativeButton(R.string.about_us_support_dev_neg){
                iapConnector.purchase(this, beer)
            }
            .neutralButton(R.string.about_us_support_dev_neu){
                iapConnector.purchase(this, beerBox)
            }
            .show()
    }

    private fun showDisclaimerDialog() {
       MaterialDialog(this)
            .title(text = getString(R.string.lyrics_disclaimer_title))
            .message(text = getString(R.string.lyrics_disclaimer_content))
            .positiveButton(text = getString(R.string.lyrics_disclaimer_title_pos)){
                ApplicationClass.getPref().edit()
                    .putBoolean(getString(R.string.pref_disclaimer_accepted), true).apply()
            }
            .negativeButton(text = getString(R.string.lyrics_disclaimer_title_neg)){
                ApplicationClass.getPref().edit()
                    .putBoolean(getString(R.string.pref_disclaimer_accepted), false).apply()
            }
           .show()
    }
}