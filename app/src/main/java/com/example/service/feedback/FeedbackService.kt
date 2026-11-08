package com.example.service.feedback

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.BuildConfig
import com.example.R
import com.example.model.DeviceDiagnosticInfo
import com.example.model.FeedbackCategory
import com.example.model.FeedbackSubmission
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FeedbackService(
    private val firestore: FirebaseFirestore
) {

    companion object {
        fun create(context: Context): FeedbackService {
            val dbId = context.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(dbId)
            return FeedbackService(db)
        }

        /**
         * Pobiera podstawowe informacje diagnostyczne urządzenia bez konieczności
         * uzyskiwania jakichkolwiek uprawnień (brak niebezpiecznych uprawnień).
         */
        fun collectDeviceDiagnostics(context: Context, preferredBibleSource: String): DeviceDiagnosticInfo {
            val osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
            val manufacturer = Build.MANUFACTURER.orEmpty()
            val model = Build.MODEL.orEmpty()
            val device = Build.DEVICE.orEmpty()
            val appVersion = BuildConfig.VERSION_NAME
            val appVersionCode = BuildConfig.VERSION_CODE
            val screenDensity = "${context.resources.displayMetrics.densityDpi} dpi"

            val networkType = getNetworkTypeDescription(context)

            return DeviceDiagnosticInfo(
                osVersion = osVersion,
                sdkInt = Build.VERSION.SDK_INT,
                manufacturer = manufacturer,
                model = model,
                device = device,
                appVersion = appVersion,
                appVersionCode = appVersionCode,
                networkType = networkType,
                screenDensity = screenDensity,
                preferredBibleSource = preferredBibleSource
            )
        }

        private fun getNetworkTypeDescription(context: Context): String {
            return try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val activeNetwork = cm?.activeNetwork
                val capabilities = cm?.getNetworkCapabilities(activeNetwork)
                when {
                    capabilities == null -> "Brak połączenia"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Dane komórkowe (LTE/5G)"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    else -> "Połączono (inne)"
                }
            } catch (e: Exception) {
                "Nieokreślone"
            }
        }
    }

    /**
     * Wysyła formularz do bazy Firestore w kolekcji `feedback`.
     */
    suspend fun submitFeedback(
        category: FeedbackCategory,
        message: String,
        contactEmail: String?,
        deviceInfo: DeviceDiagnosticInfo
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmedMessage = message.trim()
        if (trimmedMessage.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("Wiadomość musi zawierać co najmniej 3 znaki."))
        }
        if (trimmedMessage.length > 4000) {
            return@withContext Result.failure(IllegalArgumentException("Wiadomość jest zbyt długa (maks. 4000 znaków)."))
        }

        val trimmedEmail = contactEmail?.trim()?.ifBlank { null }

        val payload = hashMapOf<String, Any>(
            "category" to category.name,
            "message" to trimmedMessage,
            "createdAt" to FieldValue.serverTimestamp(),
            "deviceInfo" to hashMapOf<String, Any>(
                "osVersion" to deviceInfo.osVersion,
                "sdkInt" to deviceInfo.sdkInt,
                "manufacturer" to deviceInfo.manufacturer,
                "model" to deviceInfo.model,
                "device" to deviceInfo.device,
                "appVersion" to deviceInfo.appVersion,
                "appVersionCode" to deviceInfo.appVersionCode,
                "networkType" to deviceInfo.networkType,
                "screenDensity" to deviceInfo.screenDensity,
                "preferredBibleSource" to deviceInfo.preferredBibleSource
            )
        )

        if (trimmedEmail != null) {
            payload["contactEmail"] = trimmedEmail
        }

        try {
            val docRef = firestore.collection("feedback").document()
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
