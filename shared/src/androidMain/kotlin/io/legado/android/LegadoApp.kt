package io.legado.android

import android.app.Application
import io.legado.shared.data.androidAppContext
import io.legado.shared.utils.CookieManager

/**
 * Android application class.
 * Initializes shared module dependencies.
 */
class LegadoApp : Application() {

    lateinit var cookieManager: CookieManager
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize shared module context
        androidAppContext = this
    }
}
