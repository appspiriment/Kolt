package io.github.appspiriment.kolt.utils

import android.content.Context
import java.io.InputStream
import java.util.Calendar

/**
 * Reads the entire content of a text file from the application's assets directory as a [String].
 *
 * <p>This function opens the specified file in the assets directory, reads all of its bytes,
 * and returns the content as a [String].
 *
 * @param filename The name of the file to read from the assets directory.
 * @return The content of the file as a [String].
 * @throws java.io.IOException If an I/O error occurs while reading the file.
 * @throws java.lang.NullPointerException if the filename is null
 */
fun Context.readStringFromAssets(filename:String): String {
    val inputStream: InputStream = assets.open(filename)
    val size = inputStream.available()
    val buffer = ByteArray(size)
    inputStream.read(buffer)
    return String(buffer)
}
