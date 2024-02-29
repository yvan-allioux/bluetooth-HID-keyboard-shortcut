Documentation technique
=======================
yvan allioux

Composable BluetoothUiConnection
--------------------------------

### Description

La fonction BluetoothUiConnection est une fonction composable qui gère l'interface utilisateur de connexion Bluetooth. Elle prend un objet BluetoothController comme argument et fournit des éléments d'interface utilisateur pour initialiser, découvrir, appairer, connecter et déconnecter des appareils Bluetooth.

#### Fonctionnalités

-   Initialise l'appareil Bluetooth avec le profil HID.

-   Découvre et appaire de nouveaux appareils.

-   Se connecte à l'hôte Bluetooth.

-   Affiche l'état actuel de la connexion Bluetooth.

-   Se déconnecte de l'hôte Bluetooth.

#### Éléments d'interface utilisateur

-   Bouton d'initialisation : Initialise l'appareil Bluetooth avec le profil HID.

-   Bouton de découverte et d'appairage : Démarre l'activité pour découvrir et appairer de nouveaux appareils.

-   Bouton de connexion : Se connecte à l'hôte Bluetooth.

-   Texte : Affiche l'état actuel de la connexion Bluetooth.

-   Icône : Indique l'état de la connexion Bluetooth.

-   Bouton de déconnexion : Se déconnecte de l'hôte Bluetooth.

Composable BluetoothDesk
------------------------

### Description

La fonction BluetoothDesk est une fonction composable qui gère l'interface utilisateur du bureau Bluetooth. Elle prend un objet BluetoothController comme argument et fournit des éléments d'interface utilisateur pour sélectionner des profils, changer des étiquettes de bouton et envoyer des commandes de clavier.

#### Fonctionnalités

-   Sélectionne et change le profil actuel.

-   Charge et sauvegarde les profils et les étiquettes de bouton sélectionnés.

-   Envoie des commandes de clavier en fonction du profil sélectionné et des actions de bouton.

-   Met à jour le texte et les actions des boutons.

#### Éléments d'interface utilisateur

-   Sélection de profil : Affiche le profil actuel et permet de le changer.

-   Étiquettes de bouton : Affiche et permet de mettre à jour les étiquettes de bouton.

-   Actions de bouton : Définit et envoie des commandes de clavier en fonction du profil sélectionné et des actions de bouton.

Fonctions utilitaires
---------------------

### saveSelectedProfile

Sauvegarde le profil sélectionné dans SharedPreferences.

#### Paramètres

-   context : Le contexte de l'application.

-   profile : Le profil sélectionné à sauvegarder.

### loadSelectedProfile

Charge le profil sélectionné depuis SharedPreferences.

#### Paramètres

-   context : Le contexte de l'application.

#### Valeur de retour

Le profil sélectionné.

### saveButtonLabels

Sauvegarde les étiquettes de bouton dans SharedPreferences.

#### Paramètres

-   context : Le contexte de l'application.

-   buttonLabels : Une carte contenant les numéros de bouton et leurs étiquettes correspondantes.

### loadButtonLabels

Charge les étiquettes de bouton depuis SharedPreferences.

#### Paramètres

-   context : Le contexte de l'application.

#### Valeur de retour

Une carte contenant les numéros de bouton et leurs étiquettes correspondantes.

Fonctions d'envoi de clavier
----------------------------

### press

Envoie des raccourcis clavier à l'aide de l'objet KeyboardSender.

#### Paramètres

-   shortcut ou keyCode : Le raccourci ou le code de touche à envoyer.

-   releaseModifiers : Indique si les modificateurs doivent être relâchés après l'envoi du raccourci (par défaut : vrai).

Actions de bouton
-----------------

### alphanum

Envoie le code de touche 'A' en tant que raccourci clavier.

### numericSequence

Envoie le code de touche '1' en tant que raccourci clavier.

Définitions de bouton
---------------------

La variable buttonActions définit les actions pour chaque bouton en fonction du profil sélectionné. Les actions sont définies comme des listes de fonctions qui envoient des raccourcis clavier spécifiques lorsqu'elles sont invoquées.

Codes de touche pris en charge
------------------------------

La variable supportedKeyCodes contient une liste de codes de touche pris en charge qui peuvent être utilisés avec les fonctions d'envoi de clavier.

Fonction de nom de touche
-------------------------

### getKeyName

Retourne le nom d'un code de touche.

#### Paramètres

-   keyCode : Le code de touche pour obtenir le nom.

#### Valeur de retour

Le nom du code de touche.

Fonction de mise à jour de l'action de bouton
---------------------------------------------

### updateButtonAction

Met à jour l'action d'un bouton en fonction du code de touche sélectionné.

#### Paramètres

-   buttonNumber : Le numéro du bouton à mettre à jour.

-   keyCode : Le nouveau code de touche à affecter au bouton.
