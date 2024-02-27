package fr.enssat.bluetoothhid.yvan_malo

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun BluetoothUiConnection(bluetoothController: BluetoothController) {
    // Contexte actuel de l'application
    val context = LocalContext.current
    // État pour contrôler la visibilité du bouton d'initialisation
    var isButtonInitVisible by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (isButtonInitVisible) {
            // Bouton pour initialiser le périphérique Bluetooth avec le profil HID
            Button(
                onClick = {
                    bluetoothController.init(context.applicationContext)
                    isButtonInitVisible = false // Cache le bouton après l'initialisation
                }
            ) {
                Text(text = "Initialize Bluetooth device with HID profile")
            }
        } else {
            // Vérifie si le Bluetooth est connecté
            val btOn = bluetoothController.status is BluetoothController.Status.Connected
            if (!btOn) {
                // Bouton pour découvrir et appairer de nouveaux appareils
                Button(
                    onClick = { context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)) }
                ) {
                    Text(text = "Discover and Pair new devices")
                }
            }
            // États de connexion Bluetooth
            val waiting = bluetoothController.status is BluetoothController.Status.Waiting
            val disconnected = bluetoothController.status is BluetoothController.Status.Disconnected
            if (waiting or disconnected) {
                // Bouton pour connecter à l'hôte Bluetooth
                Button(
                    onClick = { bluetoothController.connectHost() }
                ) {
                    Text(text = "Bluetooth connect to host")
                }
            }
            // Affiche l'état actuel de la connexion Bluetooth
            Text(text = bluetoothController.status.display)

            // Icône indiquant l'état de connexion Bluetooth
            Icon(
                if (btOn) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                "bluetooth",
                modifier = Modifier.size(100.dp),
                tint = if (btOn) Color.Blue else Color.Black,
            )
            if (btOn) {
                // Bouton pour se déconnecter de l'hôte Bluetooth
                Button(
                    onClick = { bluetoothController.release() }
                ) {
                    Text(text = "Bluetooth disconnect from host")
                }
            }
        }
    }
}

private fun saveSelectedProfile(context: Context, profile: Int) {
    val sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putInt("selected_profile", profile)
    editor.apply()
}

private fun loadSelectedProfile(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    return sharedPreferences.getInt("selected_profile", 1)
}

private fun saveButtonLabels(context: Context, buttonLabels: Map<Int, String>) {
    val sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    for ((buttonNumber, label) in buttonLabels) {
        editor.putString("button_label_$buttonNumber", label)
    }

    editor.apply()
}

private fun loadButtonLabels(context: Context): Map<Int, String> {
    val sharedPreferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    val buttonLabels = mutableMapOf<Int, String>()

    for (buttonNumber in 1..8) {
        val label =
            sharedPreferences.getString("button_label_$buttonNumber", "Action $buttonNumber")
        buttonLabels[buttonNumber] = label ?: "Action $buttonNumber"
    }

    return buttonLabels
}

@Composable
fun BluetoothDesk(bluetoothController: BluetoothController) {
    // S'assure qu'un appareil est connecté avant de continuer
    val connected = bluetoothController.status as? BluetoothController.Status.Connected ?: return

    // Contexte actuel de l'application
    val context = LocalContext.current


    // Initialisation de l'envoi de commandes clavier
    val keyboardSender = KeyboardSender(connected.btHidDevice, connected.hostDevice)

    // Gestion du profil sélectionné
    var selectedProfile by remember { mutableStateOf(loadSelectedProfile(context)) }
    val profiles = listOf("Profile 1", "Profile 2")
    var expanded by remember { mutableStateOf(false) }

    // Fonction pour envoyer des raccourcis clavier
    fun press(shortcut: Shortcut, releaseModifiers: Boolean = true) {
        val result =
            keyboardSender.sendKeyboard(shortcut.shortcutKey, shortcut.modifiers, releaseModifiers)
        if (!result) Toast.makeText(context, "Can't find keymap for $shortcut", Toast.LENGTH_LONG)
            .show()
    }
    // Fonction pour envoyer des raccourcis clavier
    fun press(keyCode: Int, releaseModifiers: Boolean = true) {
        val result = keyboardSender.sendKeyboard(keyCode, emptyList(), releaseModifiers)
        if (!result) Toast.makeText(context, "Can't find keymap for $keyCode", Toast.LENGTH_LONG)
            .show()
    }

    // UI pour sélectionner le profil
    Text("Selected Profile: ")
    Text(profiles[selectedProfile - 1])
    Button(onClick = { expanded = true }) {
        Text("Change Profile")
    }


    // Charge les noms des boutons d'action
    val buttonLabels =
        remember { mutableStateMapOf<Int, String>().apply { putAll(loadButtonLabels(context)) } }

    // Fonction pour mettre à jour le texte d'un bouton
    fun updateButtonText(buttonNumber: Int, newText: String) {
        buttonLabels[buttonNumber] = newText
        saveButtonLabels(context, buttonLabels)
    }

    // Menu déroulant pour le choix du profil
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        profiles.forEachIndexed { index, profile ->
            DropdownMenuItem(
                onClick = {
                    selectedProfile = index + 1
                    expanded = false
                    saveSelectedProfile(
                        context,
                        selectedProfile
                    ) // Enregistre le nouveau profil sélectionné
                },
                text = { Text(profile) }
            )
        }
    }


    //definiton des boutons
    var selectedButton by remember { mutableStateOf(1) }
    var showDropdown by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(false) }
    //UI pour changer le texte des boutons
    Box(modifier = Modifier.clickable { showDropdown = !showDropdown }) {
        Text("Change button text  : $selectedButton")
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Dropdown",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(start = 8.dp)
        )

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            (1..8).forEach { buttonIndex ->
                DropdownMenuItem(
                    onClick = {
                        selectedButton = buttonIndex
                        showDropdown = false
                        showPopup = true
                    },
                    text = { Text("Button $buttonIndex") }
                )
            }
        }
    }
    // Popup pour changer le texte des boutons
    if (showPopup) {
        var newText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPopup = false },
            title = { Text("Enter new button text") },
            text = {
                TextField(
                    value = newText,
                    onValueChange = { newText = it },
                    label = { Text("New text") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    updateButtonText(selectedButton, newText)
                    showPopup = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showPopup = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    //fonction pour envoyer des raccourcis clavier
    fun alphanum() {
        press(Shortcut(KeyEvent.KEYCODE_A))
    }

    fun numericSequence() {
        press(Shortcut(KeyEvent.KEYCODE_1))
    }

    // Définition des actions pour chaque bouton
    val buttonActions = mutableMapOf(
        1 to listOf({ alphanum() }, { numericSequence() }), // Button 1 actions for Profile 1 and 2
        2 to listOf(
            { press(Shortcut(KeyEvent.KEYCODE_B)) },
            { press(Shortcut(KeyEvent.KEYCODE_2)) }),
        3 to listOf(
            { press(Shortcut(KeyEvent.KEYCODE_C)) },
            { press(Shortcut(KeyEvent.KEYCODE_3)) }),
        4 to listOf(
            { press(Shortcut(KeyEvent.KEYCODE_D)) },
            { press(Shortcut(KeyEvent.KEYCODE_4)) }),
        5 to listOf(
            { press(Shortcut(KeyEvent.KEYCODE_E)) },
            { press(Shortcut(KeyEvent.KEYCODE_5)) }),
        6 to listOf(
            { press(Shortcut(KeyEvent.KEYCODE_F)) },
            { press(Shortcut(KeyEvent.KEYCODE_6)) }),
        7 to listOf(
            { press(Shortcut(KeyEvent.KEYCODE_G)) },
            { press(Shortcut(KeyEvent.KEYCODE_7)) }),
        8 to listOf(
            { press(Shortcut(KeyEvent.KEYCODE_H)) },
            { press(Shortcut(KeyEvent.KEYCODE_8)) })
    )

    // Liste des keycodes supportés
    val supportedKeyCodes = listOf(
        KeyEvent.KEYCODE_0,
        KeyEvent.KEYCODE_1,
        KeyEvent.KEYCODE_2,
        KeyEvent.KEYCODE_3,
        KeyEvent.KEYCODE_4,
        KeyEvent.KEYCODE_5,
        KeyEvent.KEYCODE_6,
        KeyEvent.KEYCODE_7,
        KeyEvent.KEYCODE_8,
        KeyEvent.KEYCODE_9,
        KeyEvent.KEYCODE_A,
        KeyEvent.KEYCODE_B,
        KeyEvent.KEYCODE_C,
        KeyEvent.KEYCODE_D,
        KeyEvent.KEYCODE_E,
        KeyEvent.KEYCODE_F,
        KeyEvent.KEYCODE_G,
        KeyEvent.KEYCODE_H,
        KeyEvent.KEYCODE_I,
        KeyEvent.KEYCODE_J,
        KeyEvent.KEYCODE_K,
        KeyEvent.KEYCODE_L,
        KeyEvent.KEYCODE_M,
        KeyEvent.KEYCODE_N,
        KeyEvent.KEYCODE_O,
        KeyEvent.KEYCODE_P,
        KeyEvent.KEYCODE_Q,
        KeyEvent.KEYCODE_R,
        KeyEvent.KEYCODE_S,
        KeyEvent.KEYCODE_T,
        KeyEvent.KEYCODE_U,
        KeyEvent.KEYCODE_V,
        KeyEvent.KEYCODE_W,
        KeyEvent.KEYCODE_X,
        KeyEvent.KEYCODE_Y,
        KeyEvent.KEYCODE_Z,
        KeyEvent.KEYCODE_SPACE,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_DEL,
        KeyEvent.KEYCODE_TAB,
        KeyEvent.KEYCODE_ESCAPE,
        KeyEvent.KEYCODE_SHIFT_LEFT,
        KeyEvent.KEYCODE_SHIFT_RIGHT,
        KeyEvent.KEYCODE_CTRL_LEFT,
        KeyEvent.KEYCODE_CTRL_RIGHT,
        KeyEvent.KEYCODE_ALT_LEFT,
        KeyEvent.KEYCODE_ALT_RIGHT,
        KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_HOME,
        KeyEvent.KEYCODE_INSERT,
        KeyEvent.KEYCODE_FORWARD_DEL,
        KeyEvent.KEYCODE_PAGE_UP,
        KeyEvent.KEYCODE_PAGE_DOWN,
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_MUTE,
        KeyEvent.KEYCODE_MEDIA_PLAY,
        KeyEvent.KEYCODE_MEDIA_PAUSE,
        KeyEvent.KEYCODE_MEDIA_STOP,
        KeyEvent.KEYCODE_MEDIA_NEXT,
        KeyEvent.KEYCODE_MEDIA_PREVIOUS,
        KeyEvent.KEYCODE_MEDIA_REWIND,
        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
        KeyEvent.KEYCODE_MENU,
        KeyEvent.KEYCODE_APP_SWITCH,
        KeyEvent.KEYCODE_BUTTON_A,
        KeyEvent.KEYCODE_BUTTON_B,
        KeyEvent.KEYCODE_BUTTON_C,
        KeyEvent.KEYCODE_BUTTON_X,
        KeyEvent.KEYCODE_BUTTON_Y,
        KeyEvent.KEYCODE_BUTTON_Z,
        KeyEvent.KEYCODE_BUTTON_L1,
        KeyEvent.KEYCODE_BUTTON_R1,
        KeyEvent.KEYCODE_BUTTON_L2,
        KeyEvent.KEYCODE_BUTTON_R2,
        KeyEvent.KEYCODE_BUTTON_THUMBL,
        KeyEvent.KEYCODE_BUTTON_THUMBR,
        KeyEvent.KEYCODE_BUTTON_START,
        KeyEvent.KEYCODE_BUTTON_SELECT,
        KeyEvent.KEYCODE_BUTTON_MODE,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_CENTER
    )

    //fonction pour obtenir le nom de la touche
    fun getKeyName(keyCode: Int): String {
        return when (keyCode) {
            KeyEvent.KEYCODE_A -> "A"
            KeyEvent.KEYCODE_B -> "B"
            // Ajoutez les autres keycodes ici aaaabbbb&&&&ééééé&"'(-è_&&&&&&
            else -> "Unknown ($keyCode)"
        }
    }

    //fonction pour mettre à jour l'action du bouton
    fun updateButtonAction(buttonNumber: Int, keyCode: Int) {
        buttonActions[buttonNumber] =
            listOf({ press(Shortcut(keyCode)) }, { press(Shortcut(keyCode)) })
    }
    //definition des variables pour la selection de la touche
    var selectedKey by remember { mutableStateOf(KeyEvent.KEYCODE_UNKNOWN) }
    var showKeyDropdown by remember { mutableStateOf(false) }
    var showKeyDialog by remember { mutableStateOf(false) }
    var newKeyCode by remember { mutableStateOf("") }
    //UI pour la selection de la touche
    Box(modifier = Modifier.clickable { showKeyDropdown = !showKeyDropdown }) {
        Text("Selected key : ${getKeyName(selectedKey)}")
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Dropdown",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(start = 8.dp)
        )

        DropdownMenu(
            expanded = showKeyDropdown,
            onDismissRequest = { showKeyDropdown = false }
        ) {
            supportedKeyCodes.forEach { keyCode ->
                DropdownMenuItem(
                    onClick = {
                        selectedKey = keyCode
                        showKeyDropdown = false
                        showKeyDialog = true
                    },
                    text = { Text(getKeyName(keyCode)) }
                )
            }
        }

    }
    //Popup pour la selection de la touche
    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false },
            title = { Text("Enter custom key code") },
            text = {
                TextField(
                    value = newKeyCode,
                    onValueChange = { newKeyCode = it },
                    label = { Text("Key code") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    selectedKey = newKeyCode.toIntOrNull() ?: KeyEvent.KEYCODE_UNKNOWN
                    showKeyDialog = false
                    updateButtonAction(selectedButton, selectedKey)
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    //UI pour les boutons
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.weight(1f)) // Espacement pour aligner le contenu
        Text("Stream Deck Controls")
        Spacer(modifier = Modifier.size(10.dp))

        // Définition de la grille de boutons pour les actions
        val buttonHeightWeight = 1f
        Column {
            for (row in 1..4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(buttonHeightWeight), horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (column in 1..2) {
                        val buttonIndex = (row - 1) * 2 + column
                        Button(
                            modifier = Modifier
                                .weight(buttonHeightWeight)
                                .padding(end = 4.dp)
                                .fillMaxHeight(),
                            onClick = {
                                buttonActions[buttonIndex]?.get(selectedProfile - 1)?.invoke()
                            }
                        ) {
                            Text(buttonLabels[buttonIndex] ?: "Action $buttonIndex")
                        }
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
            }
        }
    }

}



