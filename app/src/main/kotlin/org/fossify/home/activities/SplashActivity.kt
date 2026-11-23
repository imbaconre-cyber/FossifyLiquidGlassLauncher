package org.fossify.home.activities

import android.content.Intent
import org.fossify.commons.activities.BaseSplashActivity
import org.fossify.home.helpers.CrashLogger

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        CrashLogger.install(applicationContext)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
