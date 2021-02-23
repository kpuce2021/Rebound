package kr.ac.kpu.sleepwell

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder

class RestartService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notificationBuilder: Notification.Builder = Notification.Builder(this, "default");
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);


        val notification = notificationBuilder.build();
        startForeground(9, notification);

        val intent = Intent(this, GroundService::class.java)
        startService(intent)

        stopForeground(true)
        stopSelf()

        return START_NOT_STICKY;
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}