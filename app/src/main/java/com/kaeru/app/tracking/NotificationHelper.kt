package com.kaeru.app.tracking

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kaeru.app.R

class NotificationHelper(private val context: Context) {
    private val CHANNEL_ID = "kaerutrack_updates"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Atualizações de Encomendas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisa quando sua encomenda muda de status"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun showNotification(trackingCode: String, newStatus: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle("Atualização: $trackingCode")
            .setContentText(newStatus)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(trackingCode.hashCode(), notification)
    }
}