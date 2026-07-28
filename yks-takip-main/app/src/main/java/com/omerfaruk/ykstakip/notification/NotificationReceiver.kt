package com.omerfaruk.ykstakip.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.omerfaruk.ykstakip.MainActivity
import com.omerfaruk.ykstakip.R
import com.omerfaruk.ykstakip.data.local.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefManager = PreferenceManager(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            val userInfo = prefManager.userInfo.first()
            val settings = prefManager.notificationSettings.first()
            
            if (userInfo == null || !settings.isEnabled) return@launch

            val examYear = userInfo.examYear
            val examDate = when (examYear) {
                "2027" -> LocalDate.of(2027, 6, 19)
                "2028" -> LocalDate.of(2028, 6, 17)
                else -> LocalDate.of(2027, 6, 19)
            }
            val now = LocalDate.now()
            val daysLeftTotal = ChronoUnit.DAYS.between(now, examDate).coerceAtLeast(0)
            
            val contentText = if (settings.displayFormat == "MonthsDays") {
                val period = Period.between(now, examDate)
                val months = period.years * 12 + period.months
                val days = period.days
                "Dikkat! Sınava $months Ay $days Gün Kaldı."
            } else {
                "Dikkat! Sınava $daysLeftTotal Gün Kaldı."
            }

            showNotification(context, contentText)
        }
    }

    private fun showNotification(context: Context, text: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Varsayılan ikon
            .setContentTitle("YKS Takip")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notification)
    }
}
