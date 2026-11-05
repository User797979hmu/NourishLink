package com.nourishlink.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.nourishlink.app.data.SessionManager
import com.nourishlink.app.data.local.AppDatabase
import com.nourishlink.app.data.local.DatabaseSeeder
import com.nourishlink.app.data.repository.AuthRepository
import com.nourishlink.app.data.repository.ClaimRepository
import com.nourishlink.app.data.repository.ListingRepository
import com.nourishlink.app.data.repository.MessageRepository
import com.nourishlink.app.data.repository.NotificationRepository
import com.nourishlink.app.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application-wide singletons: the on-device Room database, the repositories on top of it, and the
 * encrypted session store. NourishLink is a single self-contained app — there is no server.
 * Kept simple (manual DI) on purpose so the whole data flow is easy to follow and demo.
 */
class NourishLinkApp : Application() {

    lateinit var sessionManager: SessionManager
        private set
    lateinit var database: AppDatabase
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var listingRepository: ListingRepository
        private set
    lateinit var claimRepository: ClaimRepository
        private set
    lateinit var messageRepository: MessageRepository
        private set
    lateinit var notificationRepository: NotificationRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)

        // Apply the user's saved language (English / isiZulu / Xitsonga) before anything else runs.
        LocaleHelper.applyPersistedLocale(this)

        sessionManager = SessionManager(this)
        database = AppDatabase.getInstance(this)

        val seeder = DatabaseSeeder(database)
        authRepository = AuthRepository(database.userDao(), sessionManager, seeder)
        listingRepository = ListingRepository(database.listingDao())
        claimRepository = ClaimRepository(database, sessionManager)
        messageRepository = MessageRepository(database, sessionManager)
        notificationRepository = NotificationRepository(database.notificationDao(), sessionManager)

        // First launch: load the demo accounts + listings in the background.
        // Login/registration wait for this to finish, so there's no race.
        appScope.launch { seeder.ensureSeeded() }

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        fun from(app: Application): NourishLinkApp = app as NourishLinkApp
    }
}
