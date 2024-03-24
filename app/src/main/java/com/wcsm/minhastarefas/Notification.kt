package com.wcsm.minhastarefas

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat

const val notificationID = 1
const val channelID = "channel1"

class Notification : BroadcastReceiver() {
    @SuppressLint("NotificationPermission")
    override fun onReceive(context: Context, intent: Intent) {
        var title = ""
        val bundle = intent.extras
        if(bundle != null) {
            title = bundle.getString("taskTitle").toString()
        }

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_taskapp_24)
            .setContentTitle("Tarefa Pendente")
            .setContentText("A tarefa \"$title\" está próxima ao prazo final.")
            .setContentIntent(pendingIntent)
            .setColor(Color.parseColor("#EC6109"))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)

        pendingIntent?.let {
            it.cancel()
            manager.cancel(notificationID)
        }
    }
}