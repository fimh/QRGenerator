package androidmads.example

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Save image to gallery.
 * Be sure to you have WRITE_EXTERNAL_STORAGE firstly.
 * From https://stackoverflow.com/a/57265702
 */
object Image2GalleryUtil {

    const val IMAGE_PNG = 0
    const val IMAGE_JPEG = 1
    const val IMAGE_WEBP = 2

    @JvmStatic
    fun hasExternalWritePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun requestWritePermission(activity: Activity) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
    }

    /**
     * @param folderName can be your app's name
     */
    @JvmStatic
    fun saveImage(bitmap: Bitmap, context: Context, folderName: String, imageFormat: Int = IMAGE_JPEG) {
        if (Build.VERSION.SDK_INT >= 29) {
            val values = contentValues(imageFormat)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$folderName")
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            // RELATIVE_PATH and IS_PENDING are introduced in API 29.

            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri), imageFormat)
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + folderName)
            // getExternalStorageDirectory is deprecated in API 29

            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = System.currentTimeMillis().toString() + imageExtension(imageFormat)
            val file = File(directory, fileName)
            saveImageToStream(bitmap, FileOutputStream(file), imageFormat)
            if (file.absolutePath != null) {
                val values = contentValues(imageFormat)
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                // .DATA is deprecated in API 29
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        }
    }

    private fun contentValues(imageFormat: Int): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, imageMimeType(imageFormat))
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        if (Build.VERSION.SDK_INT >= 29) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?, imageFormat: Int = IMAGE_JPEG) {
        if (outputStream != null) {
            try {
                bitmap.compress(imageCompressFormat(imageFormat), 100, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun imageExtension(imageFormat: Int): String {
        return when (imageFormat) {
            IMAGE_PNG -> ".png"
            IMAGE_JPEG -> ".jpg"
            else -> ".webp"
        }
    }

    private fun imageCompressFormat(imageFormat: Int): Bitmap.CompressFormat {
        return when (imageFormat) {
            IMAGE_PNG -> Bitmap.CompressFormat.PNG
            IMAGE_WEBP -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.JPEG
        }
    }

    private fun imageMimeType(imageFormat: Int): String {
        return when (imageFormat) {
            IMAGE_JPEG -> "image/jpeg"
            IMAGE_PNG -> "image/png"
            else -> "image/webp"
        }
    }

}