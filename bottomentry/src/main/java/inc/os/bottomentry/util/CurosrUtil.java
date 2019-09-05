package inc.os.bottomentry.util;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CurosrUtil {
    public static List<String> getPhonePicPathList(Context context){
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        List<String> pathList = new ArrayList<>();
        while (Objects.requireNonNull(cursor).moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            pathList.add(path);
        }
        return pathList;
    }
}
