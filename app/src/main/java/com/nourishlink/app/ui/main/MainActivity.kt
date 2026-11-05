package com.nourishlink.app.ui.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.nourishlink.app.NourishLinkApp
import com.nourishlink.app.R
import com.nourishlink.app.databinding.ActivityMainBinding
import com.nourishlink.app.notifications.NotificationHelper
import com.nourishlink.app.ui.claims.ClaimsFragment
import com.nourishlink.app.ui.feed.FeedFragment
import com.nourishlink.app.ui.impact.ImpactFragment
import com.nourishlink.app.ui.notifications.NotificationsFragment
import com.nourishlink.app.ui.settings.SettingsFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Hosts the main navigation tabs (Feed, My Claims, Notifications, Impact, Profile) and, every
 * 20s while the app is open, pops a system notification for any new in-app notification.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val app by lazy { NourishLinkApp.from(application) }

    /** Highest notification id already seen; null until the first check sets the baseline. */
    private var lastSeenNotificationId: Int? = null

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!app.sessionManager.isLoggedIn) {
            finish()
            return
        }

        maybeRequestNotificationPermission()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FeedFragment())
                .commit()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_feed -> FeedFragment()
                R.id.nav_claims -> ClaimsFragment()
                R.id.nav_notifications -> NotificationsFragment()
                R.id.nav_impact -> ImpactFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> FeedFragment()
            }
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
            true
        }

        startNotificationPolling()
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun startNotificationPolling() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    pollNotifications()
                    delay(20_000)
                }
            }
        }
    }

    private suspend fun pollNotifications() {
        if (app.sessionManager.currentUser?.notificationsEnabled == false) return
        try {
            val repo = app.notificationRepository
            val lastSeen = lastSeenNotificationId
            if (lastSeen == null) {
                // First check after opening the app: don't replay old notifications, just remember where we are.
                lastSeenNotificationId = repo.latestId()
                return
            }
            val fresh = repo.getNewerThan(lastSeen)
            fresh.forEach {
                NotificationHelper.show(this@MainActivity, it.title, it.body, it.listingId)
            }
            if (fresh.isNotEmpty()) {
                lastSeenNotificationId = fresh.maxOf { it.id }
            }
        } catch (e: Exception) {
            // Skip this cycle; we'll try again in 20s.
        }
    }
}
