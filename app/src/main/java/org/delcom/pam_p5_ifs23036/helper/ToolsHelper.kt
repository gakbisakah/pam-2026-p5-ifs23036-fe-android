package org.delcom.pam_p5_ifs23036.helper

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object ToolsHelper {
    fun getTodoImage(todoId: String, updatedAt: String?): String {
        return "${ConstHelper.BASE_URL}todos/$todoId/cover?t=${updatedAt ?: ""}"
    }

    fun getUserImage(userId: String, updatedAt: String?): String {
        return "${ConstHelper.BASE_URL}profile/$userId/photo?t=${updatedAt ?: ""}"
    }

    fun uriToMultipart(context: Context, uri: Uri, partName: String): MultipartBody.Part {
        val file = uriToFile(context, uri)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }

    fun formatDateTime(isoString: String?): String {
        if (isoString.isNullOrEmpty()) return "-"
        return try {
            // Assuming format from backend is ISO 8601 (e.g. 2024-10-24T10:00:00Z)
            val zonedDateTime = ZonedDateTime.parse(isoString)
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("id", "ID"))
            zonedDateTime.format(formatter)
        } catch (e: Exception) {
            isoString.split("T").firstOrNull() ?: isoString
        }
    }
}
