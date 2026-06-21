
package io.github.appspiriment.kolt.composeutils.utils


import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText
import java.net.URLEncoder

/**
 * A Composable hook that provides a state object to trigger a direct phone call.
 * It handles the required `CALL_PHONE` permission request flow automatically.
 *
 * @param rationaleMessage A custom message to show the user explaining why the permission is needed.
 * @param onFail A callback for any exceptions during the call intent, e.g., security exceptions.
 * @return A `MutableState<String?>`. Set its `value` to a phone number string to initiate the call.
 */
@Composable
fun rememberCallPhoneState(
    rationaleMessage: String = "To make calls, please grant the phone permission.",
    onFail: (Exception) -> Unit = {}
): MutableState<String?> {
    val context = LocalContext.current
    var numberToCall by remember { mutableStateOf<String?>(null) }

    // The actual action to perform once permission is granted.
    val callAction = {
        numberToCall?.let { number ->
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$number")
                // No flags at all is best here
            }
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                onFail(e)
            } catch (e: SecurityException) {
                onFail(e)
            }
        }
    }

    // Use the high-level permission request hook.
    val permissionLauncher = rememberPermissionRequest(
        permissions = listOf(
            AppPermission(
                permission = Manifest.permission.CALL_PHONE,
                name = "Phone Calls"
            )
        ),
        onActionGranted = {
            callAction()
        },
        askFirst = true, // It's good practice to ask before the system prompt for this permission.
        rationaleMessage = { rationaleMessage.toUiText() }
    )

    // The trigger state returned to the user.
    val trigger = remember { mutableStateOf<String?>(null) }

    // Effect to link the user's trigger to our internal state and launcher.
    LaunchedEffect(trigger.value) {
        val number = trigger.value ?: return@LaunchedEffect
        numberToCall = number      // Set the number for the action.
        permissionLauncher()      // Launch the permission flow.
        trigger.value = null      // Reset the trigger immediately.
    }

    return trigger
}

/**
 * A Composable hook that provides a state object to open the phone dialer with a pre-filled number.
 * This action does not require any permissions.
 *
 * @param onFail A callback for any exceptions, e.g., if no dialer app is available.
 * @return A `MutableState<String?>`. Set its `value` to a phone number to open the dialer.
 */
@Composable
fun rememberDialPhoneState(
    onFail: (Exception) -> Unit = {}
): MutableState<String?> {
    val context = LocalContext.current
    val dialPhoneNum: MutableState<String?> = remember { mutableStateOf(null) }

    LaunchedEffect(dialPhoneNum.value) {
        dialPhoneNum.value?.let { number ->
            dialPhoneNum.value = null
            val intent = Intent(Intent.ACTION_DIAL, "tel:$number".toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                onFail(e)
            }
        }
    }
    return dialPhoneNum
}

/**
 * A Composable hook that provides a state object to open the default SMS app with a pre-filled number and message.
 * This action does not require any permissions.
 *
 * @param onFail A callback for any exceptions, e.g., if no SMS app is available.
 * @return A `MutableState<TextMessage?>`. Set its `value` to a [TextMessage] object to launch the SMS app.
 */
@Composable
fun rememberSendSMSState(
    onFail: (Exception) -> Unit = {}
): MutableState<TextMessage?> {
    val context = LocalContext.current
    val sendSmsToPhoneNum: MutableState<TextMessage?> = remember { mutableStateOf(null) }

    LaunchedEffect(sendSmsToPhoneNum.value) {
        sendSmsToPhoneNum.value?.let { textMessage ->
            sendSmsToPhoneNum.value = null
            val intent = Intent(Intent.ACTION_SENDTO, "smsto:${textMessage.phoneNumber}".toUri()).apply {
                textMessage.message?.let{ msg -> putExtra("sms_body", msg) }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                onFail(e)
            }
        }
    }
    return sendSmsToPhoneNum
}

/**
 * A Composable hook that provides a state object to send a WhatsApp message.
 * It can be configured to use either the personal or business version of WhatsApp.
 *
 * @param forceWhatsappBiz If `true`, it targets the WhatsApp Business app.
 * @param onFail A callback for any exceptions, e.g., if the target WhatsApp app is not installed.
 * @return A `MutableState<TextMessage?>`. Set its `value` to a [TextMessage] object to launch WhatsApp.
 */
@Composable
fun rememberSendWhatsappState(
    showChooser: Boolean = true,
    forceWhatsappBiz: Boolean = false,
    onFail: (Exception) -> Unit = {}
): MutableState<TextMessage?> {
    val context = LocalContext.current
    val sendWhatsappToPhoneNum: MutableState<TextMessage?> = remember { mutableStateOf(null) }

    LaunchedEffect(sendWhatsappToPhoneNum.value) {
        sendWhatsappToPhoneNum.value?.let { textMessage ->
            sendWhatsappToPhoneNum.value = null

            val url = buildString {
                append("https://api.whatsapp.com/send?phone=${textMessage.phoneNumber}")
                textMessage.message?.let { msg ->
                    val encodedMessage = URLEncoder.encode(msg, "UTF-8")
                    append("&text=$encodedMessage")
                }
            }

            val sendIntent = Intent(Intent.ACTION_VIEW).apply {
                data = url.toUri()
                if(!showChooser){
                    setPackage(if (forceWhatsappBiz) "com.whatsapp.w4b" else "com.whatsapp")
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                if(showChooser){
                    context.startActivity(Intent.createChooser(sendIntent, "Send through..."))
                } else {
                    context.startActivity(sendIntent)
                }
            } catch (e: Exception) {
                onFail(e)
            }
        }
    }
    return sendWhatsappToPhoneNum
}

/**
 * A Composable hook that provides a state object to launch the default email client.
 *
 * @param onFail A callback for any exceptions, e.g., if no email client is installed.
 * @return A `MutableState<EmailMessage?>`. Set its `value` to an [EmailMessage] object to launch the email app.
 */
@Composable
fun rememberSendEmailState(
    onFail: (Exception) -> Unit = {}
): MutableState<EmailMessage?> {
    val context = LocalContext.current
    val emailState: MutableState<EmailMessage?> = remember { mutableStateOf(null) }

    LaunchedEffect(emailState.value) {
        emailState.value?.let { emailMessage ->
            emailState.value = null
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri() // Only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, emailMessage.to.toTypedArray())
                emailMessage.cc?.let { putExtra(Intent.EXTRA_CC, it.toTypedArray()) }
                emailMessage.bcc?.let { putExtra(Intent.EXTRA_BCC, it.toTypedArray()) }
                emailMessage.subject?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
                emailMessage.body?.let { putExtra(Intent.EXTRA_TEXT, it) }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                onFail(e)
            }
        }
    }
    return emailState
}

/**
 * Data for a text-based message (SMS or WhatsApp).
 * @param phoneNumber The recipient's phone number.
 * @param message The optional message content.
 */
data class TextMessage(
    val phoneNumber: String,
    val message: String? = null
)

/**
 * Data for an email message.
 * @param to A list of primary recipients' email addresses.
 * @param cc A list of CC recipients' email addresses.
 * @param bcc A list of BCC recipients' email addresses.
 * @param subject The subject of the email.
 * @param body The body content of the email.
 */
data class EmailMessage(
    val to: List<String>,
    val cc: List<String>? = null,
    val bcc: List<String>? = null,
    val subject: String? = null,
    val body: String? = null
)
