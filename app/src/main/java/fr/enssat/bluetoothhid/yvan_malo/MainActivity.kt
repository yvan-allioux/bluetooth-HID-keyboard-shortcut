package fr.enssat.bluetoothhid.yvan_malo

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.room.Room

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var db: AppDataBase
    private lateinit var bluetoothController: BluetoothController

    private fun ensureBluetoothPermission(activity: ComponentActivity) {
        val requestPermissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted: Boolean ->
                if (isGranted) {Log.d(MainActivity.TAG, "Bluetooth connection granted")
                } else { Log.e(MainActivity.TAG, "Bluetooth connection not granted, Bye!")
                         activity.finish()
                }
        }

        requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_ADMIN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        ensureBluetoothPermission(this)

        db = Room.databaseBuilder(applicationContext, AppDataBase::class.java, "hid_db").build()
        val deckDao = db.deckDao()
        val shortcutDao = db.shortcutDao()
        val shortcutbydeckDao = db.shortcutbydeckDao()
        bluetoothController = BluetoothController()

        setContent {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        BluetoothUiConnection(bluetoothController)
                        BluetoothDesk(bluetoothController, deckDao,shortcutDao,shortcutbydeckDao)
                    }
                }
        }
    }

    override fun onPause() {
        super.onPause()
        bluetoothController.release()
    }
}

typealias KeyModifier = Int