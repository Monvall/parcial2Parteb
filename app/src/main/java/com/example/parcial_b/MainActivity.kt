package com.example.parcial_b
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var locationButton: Button
    private lateinit var mediaButton: Button
    private lateinit var notificationButton: Button
    private lateinit var sendNotificationButton: Button
    private lateinit var goToGalleryButton: Button
    private lateinit var goToMapButton: Button
    private lateinit var storageStatusText: TextView
    private lateinit var locationStatusText: TextView
    private lateinit var notificationStatusText: TextView

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "example_channel"
        private const val REQUEST_CODE_NOTIFICATIONS = 3
        private const val REQUEST_CODE_LOCATION = 1
        private const val REQUEST_CODE_MANAGE_STORAGE = 2296
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar botones
        locationButton = findViewById(R.id.btn_location)
        mediaButton = findViewById(R.id.btn_media)
        notificationButton = findViewById(R.id.btn_notifications)
        sendNotificationButton = findViewById(R.id.btn_send_notification)
        goToGalleryButton = findViewById(R.id.btn_go_to_gallery)
        goToMapButton = findViewById(R.id.btn_go_to_map)
        storageStatusText = findViewById(R.id.storage_status_text)
        locationStatusText = findViewById(R.id.location_status_text)
        notificationStatusText = findViewById(R.id.notification_status_text)

        // Inicialmente ocultar botones "Ir a Galería" y "Ir al Mapa"
        goToGalleryButton.isEnabled = false
        goToGalleryButton.visibility = View.GONE
        goToMapButton.isEnabled = false
        goToMapButton.visibility = View.GONE
        sendNotificationButton.isEnabled = false
        sendNotificationButton.visibility = View.GONE

        // Botón para solicitar permisos de ubicación (Google Maps)
        locationButton.setOnClickListener {
            requestLocationPermission()
        }

        // Botón para solicitar permisos de multimedia (All Files Access en Android 11+)
        mediaButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    storageStatusText.text = "Permiso de multimedia concedido"
                    enableGoToGalleryButton()
                } else {
                    requestManageExternalStoragePermission()
                }
            } else {
                // Solicitar permiso de almacenamiento externo en versiones anteriores
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE_MANAGE_STORAGE
                )
            }
        }

        // Botón para solicitar permisos de notificaciones
        notificationButton.setOnClickListener {
            requestNotificationPermission()
        }

        // Botón para ir a la galería
        goToGalleryButton.setOnClickListener {
            openGallery()
        }

        // Botón para ir al mapa
        goToMapButton.setOnClickListener {
            openMap()
        }

        // Botón para enviar notificaciones
        sendNotificationButton.setOnClickListener {
            if (hasNotificationPermission()) {
                sendNotification()
            } else {
                notificationStatusText.text = getString(R.string.notification_permission_denied)
            }
        }

        createNotificationChannel() // Crear canal de notificaciones si es necesario
    }

    // Abrir galería
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivity(intent)
    }

    // Abrir el mapa
    private fun openMap() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    // Solicitar permisos de ubicación
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        } else {
            locationStatusText.text = "Permiso de ubicación concedido"
            enableGoToMapButton()
        }
    }

    // Solicitar permisos para administrar todos los archivos en Android 11+
    private fun requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, REQUEST_CODE_MANAGE_STORAGE)
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_MANAGE_STORAGE)
        }
    }

    // Habilitar el botón "Ir a Galería"
    private fun enableGoToGalleryButton() {
        mediaButton.isEnabled = false
        goToGalleryButton.isEnabled = true
        goToGalleryButton.visibility = View.VISIBLE
    }

    // Habilitar el botón "Ir al Mapa"
    private fun enableGoToMapButton() {
        locationButton.isEnabled = false
        goToMapButton.isEnabled = true
        goToMapButton.visibility = View.VISIBLE
    }

    // Verificar si se tiene el permiso de notificaciones
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Solicitar permisos de notificaciones
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATIONS)
        } else {
            enableSendNotificationButton()
        }
    }

    // Habilitar el botón para enviar notificaciones
    private fun enableSendNotificationButton() {
        notificationStatusText.text = getString(R.string.notification_permission_granted)
        notificationButton.isEnabled = false
        sendNotificationButton.isEnabled = true
        sendNotificationButton.visibility = View.VISIBLE
    }

    // Crear canal de notificaciones
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Enviar notificación
    private fun sendNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Verificar si las notificaciones están habilitadas

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())

        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(1, builder.build())
        } else {
            notificationStatusText.text = "Las notificaciones están deshabilitadas para esta aplicación."
        }

    }

    // Manejar el resultado de las solicitudes de permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationStatusText.text = "Permiso de ubicación concedido"
                    enableGoToMapButton()
                } else {
                    locationStatusText.text = "Permiso de ubicación no concedido"
                }
            }
            REQUEST_CODE_MANAGE_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    storageStatusText.text = "Permiso de multimedia concedido"
                    enableGoToGalleryButton()
                } else {
                    storageStatusText.text = "Permiso de multimedia no concedido"
                }
            }
            REQUEST_CODE_NOTIFICATIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableSendNotificationButton()
                } else {
                    notificationStatusText.text = getString(R.string.notification_permission_denied)
                }
            }
        }
    }
}

