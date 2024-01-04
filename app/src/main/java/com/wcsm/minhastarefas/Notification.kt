package com.wcsm.minhastarefas

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
        Log.i("teste", "dentro da Notification: $title")
        val notification = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_task_24)
            .setContentTitle("Tarefa Pendente")
            .setContentText("A tarefa \"$title\" está próxima ao prazo final.")
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationID, notification)
    }
}