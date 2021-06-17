package androidmads.library.qrgenearator;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("ALL")
public class QRGSaver {

    public boolean save(Context app, String saveLocation, String imageName, Bitmap bitmap, int imageFormat) {

        boolean success = false;
        String imageDetail = saveLocation + imageName + imgFormat(imageFormat);
        FileOutputStream outStream;
        File file = new File(saveLocation);
        if (!file.exists()) {
            file.mkdir();
        } else {
            Log.d("QRGSaver", "Folder Exists");
        }
        try {
            outStream = new FileOutputStream(imageDetail);
            bitmap.compress((Bitmap.CompressFormat) compressFormat(imageFormat), 100, outStream);
            outStream.flush();
            outStream.close();
            success = true;
        } catch (IOException e) {
            Log.d("QRGSaver", e.toString());
        }

        MediaScannerConnection.scanFile(app, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

        return success;
    }

    public boolean save(Context app, String saveLocation, String imageName, Bitmap bitmap) {
        return save(app, saveLocation, imageName, bitmap, QRGContents.ImageType.IMAGE_PNG);
    }

    private String imgFormat(int imageFormat) {
        return imageFormat == QRGContents.ImageType.IMAGE_PNG ? ".png" : ".jpg";
    }

    private Comparable<? extends Comparable<? extends Comparable<?>>> compressFormat(int imageFormat) {
        return imageFormat == QRGContents.ImageType.IMAGE_PNG ? Bitmap.CompressFormat.PNG :
                (imageFormat == QRGContents.ImageType.IMAGE_WEBP ? Bitmap.CompressFormat.WEBP : Bitmap.CompressFormat.JPEG);
    }

}
