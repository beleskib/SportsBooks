package com.example.sportsbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.sportsbook.ui.navigation.SportsBookNavHost
import com.example.sportsbook.ui.theme.SportsBookTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Deep link data extracted from a push notification tap.
 */
data class NotificationDeepLink(
    val type: String,
    val bookingId: Long? = null,
    val matchId: Long? = null,
    val partyId: Long? = null,
    val lobbyId: Long? = null,
    val communityId: Long? = null,
    val paymentId: Long? = null,
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val _pendingDeepLink = MutableStateFlow<NotificationDeepLink?>(null)
    val pendingDeepLink: StateFlow<NotificationDeepLink?> = _pendingDeepLink.asStateFlow()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — either way we proceed */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        handleNotificationIntent(intent)

        setContent {
            SportsBookTheme {
                SportsBookNavHost(pendingDeepLink = pendingDeepLink)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    fun consumeDeepLink() {
        _pendingDeepLink.value = null
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val type = intent?.getStringExtra("type") ?: return
        val bookingId = intent.getStringExtra("bookingId")?.toLongOrNull()
        val matchId = intent.getStringExtra("matchId")?.toLongOrNull()
        val partyId = intent.getStringExtra("partyId")?.toLongOrNull()
        val lobbyId = intent.getStringExtra("lobbyId")?.toLongOrNull()
        val communityId = intent.getStringExtra("communityId")?.toLongOrNull()
        val paymentId = intent.getStringExtra("paymentId")?.toLongOrNull()

        _pendingDeepLink.value = NotificationDeepLink(
            type = type,
            bookingId = bookingId,
            matchId = matchId,
            partyId = partyId,
            lobbyId = lobbyId,
            communityId = communityId,
            paymentId = paymentId,
        )
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }
}
