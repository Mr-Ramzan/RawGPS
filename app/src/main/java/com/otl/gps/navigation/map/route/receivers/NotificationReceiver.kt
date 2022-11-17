package com.otl.gps.navigation.map.route.receivers

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.otl.gps.navigation.map.route.R
import com.otl.gps.navigation.map.route.model.NotificationSlugs
import com.otl.gps.navigation.map.route.view.activity.splash.Splash
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.random.Random


class NotificationReceiver : BroadcastReceiver() {
    //============================================================================================//

    private val PENDING_INTENT_ID = 786545
    private val CHANNEL_ID = "RawGPS"
    private val NOTIFICATION_ID = 37863624

    override fun onReceive(context: Context?, intent: Intent?) {
        showLocalNotifications(context)
    }


    private lateinit var notificationsSlugs: ArrayList<NotificationSlugs>

    private fun showLocalNotifications(context: Context?) {

        if (context == null) {
            return
        }
        notificationsSlugs = ArrayList<NotificationSlugs>()
        notificationsSlugs.add(
            NotificationSlugs(
                "Don't know where you are?",
                "Pin point your location on the map Now!"
            )
        )

        notificationsSlugs.add(
            NotificationSlugs(
                "Know your speed real time!",
                "Moving, know your speed using speedometer with beautiful interfaces."
            )
        )
        notificationsSlugs.add(
            NotificationSlugs(
                "Enjoying this place? ",
                "Save in your saved places for later."
            )
        )
        notificationsSlugs.add(
            NotificationSlugs(
                "Which way is the north? ",
                "Use Compass to find out.."
            )
        )
        notificationsSlugs.add(
            NotificationSlugs(
                "Get Weekly weather Updates",
                "Plans on traveling get weather updates before hand."
            )
        )
        notificationsSlugs.add(
            NotificationSlugs(
                "Never get Stuck in Traffic! ",
                "Get live Traffic Updates before travelling and never get stuck. "
            )
        )

        var index = Random.nextInt(0, 2)
        var slug = notificationsSlugs[index]
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    CHANNEL_ID,
                    "Editor Notifications Channel",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.white))
                .setSmallIcon(R.drawable.my_location_icon)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(slug.title)
                .setContentText(slug.desc)
                .setStyle(NotificationCompat.BigTextStyle().bigText(slug.desc))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
               notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
            }
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
            initReminderNotification(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun largeIcon(context: Context): Bitmap? {
        val res = context.resources
        return (ResourcesCompat.getDrawable(res, R.drawable.app_logo, null))?.toBitmap()
    }

    private fun contentIntent(context: Context): PendingIntent {
        val startActivityIntent = Intent(context, Splash::class.java)
        return PendingIntent.getActivity(
            context, PENDING_INTENT_ID,
            startActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    private fun initReminderNotification(context: Context?) {

        if (context == null)
        {
            return
        }
        val start: LocalDateTime = LocalDateTime.now()
        // Hour + 1, set Minute and Second to 00
        val end: LocalDateTime = start.plusHours(3).truncatedTo(ChronoUnit.HOURS)
        // Get Duration
        val duration: Duration = Duration.between(start, end)
        val millis: Long = duration.toMillis()
        val delayTimeInMillis = System.currentTimeMillis() + millis
        val intent = Intent(context, NotificationReceiver::class.java)
        alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
        pendingIntent = PendingIntent.getBroadcast(context, 100101, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            delayTimeInMillis,
            delayTimeInMillis,
            pendingIntent
        )


    }


}