package io.legado.shared.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

lateinit var androidAppContext: android.content.Context

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = androidAppContext,
            name = "legado.db"
        )
    }
}
