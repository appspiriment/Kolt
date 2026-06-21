package io.github.appspiriment.kolt.utils.extensions

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast

/**
 * Launches the Google Play Store page for the current application.
 *
 * <p>This function creates an intent to open the Google Play Store app and directs it to the
 * details page for the current application. It sets the necessary flags to launch the
 * Play Store in a new task and specifies the package name of the Play Store app.
 *
 * <p>If the Play Store app is not installed or cannot be launched, the function catches
 * any exceptions and does not perform any further actions.
 *
 * @receiver The context used to start the activity.
 */
fun Context.launchPlayStorePage(){
    Intent(
        Intent.ACTION_VIEW,
        Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
    ).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        setPackage("com.android.vending")
    }.let {
        try {
            startActivity(it)
        } catch (_: Exception) { }
    }
}

fun Context.initiateNumberDial(phoneNumber: String, onFail: (Exception)->Unit = {}) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        startActivity(intent)
    } catch (e: Exception) {
        onFail(e)
    }
}

fun Context.initiateSms(phoneNumber: String, message: String = "", onFail: (Exception)->Unit = {}) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber")).apply {
        putExtra("sms_body", message)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        startActivity(intent)
    } catch (e: Exception) {
        onFail(e)
    }
}