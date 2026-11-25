package com.gabriel.powergabs.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gabriel.powergabs.R
import com.gabriel.powergabs.core.time.TimeProvider
import com.gabriel.powergabs.domain.rule.ReminderRules
import java.util.concurrent.TimeUnit

/** Canal do lembrete. Canais só existem a partir do Android 8 (API 26); antes disso, nada a fazer. */
object NotificationChannels {

    const val DAILY_REMINDER = "daily_reminder"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            DAILY_REMINDER,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.notification_channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

/**
 * Agenda o lembrete diário (RF17, RN16).
 *
 * O horário é calculado pela regra do domain ([ReminderRules]), testada com relógio fixo.
 * `KEEP` mantém o agendamento existente: abrir o app várias vezes não cria lembretes duplicados.
 */
class ReminderScheduler(
    private val workManager: WorkManager,
    private val timeProvider: TimeProvider,
) {

    fun schedule() {
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, buildRequest())
    }

    fun buildRequest(): PeriodicWorkRequest {
        val delay = ReminderRules.delayUntilNextReminder(timeProvider.now(), timeProvider.zone())
        return PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
    }

    companion object {
        const val WORK_NAME = "daily_training_reminder"
    }
}

/**
 * Worker do lembrete: só monta e mostra a notificação, sem regra de negócio.
 * Sem permissão (Android 13+), termina com sucesso sem notificar: o usuário escolheu não receber.
 */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        if (!canNotify(context)) return Result.success()

        // Abre o app ao tocar. FLAG_IMMUTABLE é obrigatório a partir do Android 12.
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.DAILY_REMINDER)
            .setSmallIcon(R.drawable.ic_logo_symbol)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permissão revogada entre a checagem e o envio: não é falha do trabalho.
        }
        return Result.success()
    }

    private fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private companion object {
        const val NOTIFICATION_ID = 1001
    }
}
