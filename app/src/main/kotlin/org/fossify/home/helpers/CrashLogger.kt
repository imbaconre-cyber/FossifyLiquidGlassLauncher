package org.fossify.home.helpers

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

/** Writes uncaught exceptions to Downloads so crashes can be diagnosed without adb. */
object CrashLogger {
    private const val FILE_NAME = "fossify_launcher_crash.txt"

    fun install(context: Context) {
        val current = Thread.getDefaultUncaughtExceptionHandler()
        if (current is CrashHandler) return
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context.applicationContext, current))
    }

    private class CrashHandler(
        private val context: Context,
        private val previous: Thread.UncaughtExceptionHandler?
    ) : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val report = buildString {
                append("Thread: ").append(thread.name).append('\n')
                append("Time: ").append(System.currentTimeMillis()).append('\n')
                append("Device: Android ").append(Build.VERSION.RELEASE)
                append(" (API ").append(Build.VERSION.SDK_INT).append(")\n\n")
                append(sw.toString())
            }
            writeReport(context, report)
            previous?.uncaughtException(thread, throwable) ?: run {
                android.os.Process.killProcess(android.os.Process.myPid())
                exitProcess(1)
            }
        }
    }

    private fun writeReport(context: Context, report: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, FILE_NAME)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    values
                ) ?: return
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(report.toByteArray())
                }
            } else {
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    FILE_NAME
                ).writeText(report)
            }
        } catch (_: Throwable) {
        }
    }
}
