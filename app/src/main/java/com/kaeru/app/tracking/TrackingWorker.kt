package com.kaeru.app.tracking

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TrackingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val notificationHelper = NotificationHelper(applicationContext)

        // 1. VERIFICAÇÃO DO GATILHO FAKE
        val isFakeTest = inputData.getBoolean("is_fake_test", false)
        if (isFakeTest) {
            // Dispara notificação de teste imediatamente
            notificationHelper.showNotification(
                trackingCode = "NL123456789BR",
                newStatus = "Objeto saiu para entrega ao destinatário"
            )
            return Result.success()
        }

        // 2. LÓGICA REAL (A Cada 1 Hora)
        try {
            // Exemplo conceitual de como integrar com o seu banco local:
            // val encomendasSalvas = repository.getAllTrackingCodes()

            // for (codigo in encomendasSalvas) {
            //     val response: TrackingResponse = api.getTracking(codigo)
            //     val eventoMaisRecente: TrackingEvent? = response.events?.firstOrNull()
            //
            //     val statusAntigo = repository.getLatestStatus(codigo)
            //     val statusNovo = eventoMaisRecente?.status
            //
            //     if (statusNovo != null && statusNovo != statusAntigo) {
            //         notificationHelper.showNotification(codigo, statusNovo)
            //         repository.updateStatus(codigo, statusNovo) // Atualiza no banco
            //     }
            // }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry() // Se a internet cair, o Android tenta de novo depois
        }
    }
}