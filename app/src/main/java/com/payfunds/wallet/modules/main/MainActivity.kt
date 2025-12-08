package com.payfunds.wallet.modules.main

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.messaging.FirebaseMessaging
import com.walletconnect.web3.wallet.client.Wallet
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.BaseActivity
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.core.slideFromBottom
import com.payfunds.wallet.modules.intro.IntroActivity
import com.payfunds.wallet.modules.keystore.KeyStoreActivity
import com.payfunds.wallet.modules.lockscreen.LockScreenActivity
import com.payfunds.wallet.modules.notification.NotificationModule
import com.payfunds.wallet.modules.notification.NotificationViewModel
import io.payfunds.core.hideKeyboard
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {

    private val viewModel by viewModels<MainActivityViewModel> {
        MainActivityViewModel.Factory()
    }

    @SuppressLint("HardwareIds")
    private fun getDevicesId(): String {
        return Secure.getString(contentResolver, Secure.ANDROID_ID) ?: "unknown"
    }

    private val viewModelFCM by viewModels<NotificationViewModel> {
        NotificationModule.Factory()
    }
    private var walletAddress: String = ""


    override fun onResume() {
        super.onResume()
        validate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DashboardObject.notificationWalletAddress =  intent.getStringExtra("walletAddress")

        requestPermissions()

        DashboardObject.deviceId = getDevicesId()

        lifecycleScope.launch {
            val account = App.accountManager.activeAccount
            account?.let {
                val address = getWalletAddress(it)
                walletAddress = address ?: ""
            }

            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                if (!token.isNullOrEmpty() && walletAddress.isNotEmpty()) {

                    DashboardObject.fcmToken = token

                    viewModelFCM.setFCMRegister(
                        deviceId = getDevicesId(),
                        fcmToken = token,
                        os = "android",
                        walletAddress = walletAddress
                    )
                }
            }.addOnFailureListener { e ->
                Log.d("FCM", "Get Fail FCM Token ${e.message}")
            }
        }

        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
        )


        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHost.navController

        navController.setGraph(R.navigation.main_graph, intent.extras)
        navController.addOnDestinationChangedListener { _, _, _ ->
            currentFocus?.hideKeyboard(this)
        }

        viewModel.navigateToMainLiveData.observe(this) {
            if (it) {
                navController.popBackStack(navController.graph.startDestinationId, false)
                viewModel.onNavigatedToMain()
            }
        }

        viewModel.wcEvent.observe(this) { wcEvent ->
            if (wcEvent != null) {
                when (wcEvent) {
                    is Wallet.Model.SessionRequest -> {
                        navController.slideFromBottom(R.id.wcRequestFragment)
                    }
                    is Wallet.Model.SessionProposal -> {
                        navController.slideFromBottom(R.id.wcSessionFragment)
                    }
                    else -> {}
                }
                viewModel.onWcEventHandled()
            }
        }

        viewModel.tcSendRequest.observe(this) { request ->
            if (request != null) {
                navController.slideFromBottom(R.id.tcSendRequestFragment)
            }
        }

        viewModel.tcDappRequest.observe(this) { request ->
            if (request != null) {
                navController.slideFromBottom(R.id.tcNewFragment, request)
                viewModel.onTcDappRequestHandled()
            }
        }
    }

    private fun validate() = try {
        viewModel.validate()
    } catch (e: MainScreenValidationError.NoSystemLock) {
        KeyStoreActivity.startForNoSystemLock(this)
        finish()
    } catch (e: MainScreenValidationError.KeyInvalidated) {
        KeyStoreActivity.startForInvalidKey(this)
        finish()
    } catch (e: MainScreenValidationError.UserAuthentication) {
        KeyStoreActivity.startForUserAuthentication(this)
        finish()
    } catch (e: MainScreenValidationError.Welcome) {
        IntroActivity.start(this)
        finish()
    } catch (e: MainScreenValidationError.Unlock) {
        LockScreenActivity.start(this)
    } catch (e: MainScreenValidationError.KeystoreRuntimeException) {
        Toast.makeText(App.instance, "Issue with Keystore", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(POST_NOTIFICATIONS)
            }
        }
        if (permissionsToRequest.isNotEmpty()) {
            requestMultiplePermissions.launch(permissionsToRequest.toTypedArray())
        } else {
            Log.d("Permissions", "All required permissions granted!")
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach { (permission, isGranted) ->
                when (permission) {
                    POST_NOTIFICATIONS -> {
                        if (isGranted) {
                            Log.d("Permissions", "Notification permission granted!")
                        } else {
                            Log.d("Permissions", "Notification permission denied!")
                        }
                    }
                }
            }
        }

}
