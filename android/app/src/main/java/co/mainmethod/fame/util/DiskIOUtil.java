package co.mainmethod.fame.util;

import android.os.Environment;

import com.mainmethod.premofm.helper.ResourceUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

/**
 * Convenience functions for disk IO related things
 * Created by evan on 1/31/16.
 */
public class DiskIOUtil {

    public static String writeTempPicture(byte[] data, String filename) {
        String filePath = null;
        FileOutputStream outputStream = null;
        File file = new File(Environment.getExternalStorageDirectory(), filename);

        if (file.exists()) {
            file.delete();
        }

        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.flush();
            filePath = file.getAbsolutePath();
        } catch (IOException e) {
            Timber.w(e, "Error writing bitmap to disk");
        } finally {
            ResourceUtil.closeResource(outputStream);
        }
        return filePath;
    }

}
