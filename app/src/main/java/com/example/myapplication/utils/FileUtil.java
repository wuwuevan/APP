package com.example.myapplication.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件工具类，用于处理文件相关操作
 */
public class FileUtil {

    /**
     * 将文件从 Uri 复制到应用的私有存储中，并返回新路径
     *
     * @param context 上下文
     * @param uri     文件 Uri
     * @return 复制后的文件路径
     */
    public static String getPathFromUri(Context context, Uri uri) {
        // 通过复制文件到内部存储来创建一个可靠的路径
        return copyFileToInternalStorage(context, uri);
    }

    /**
     * 将文件从 Uri 复制到应用的私有存储中
     *
     * @param context 上下文
     * @param uri     文件 Uri
     * @return 复制后的文件路径
     */
    private static String copyFileToInternalStorage(Context context, Uri uri) {
        String fileName = getFileName(context, uri);
        if (fileName == null) {
            // 如果无法获取文件名，则创建一个唯一的文件名
            fileName = "avatar_" + System.currentTimeMillis();
        }

        File file = new File(context.getFilesDir(), fileName);
        
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从 Uri 获取文件名
     *
     * @param context 上下文
     * @param uri     文件 Uri
     * @return 文件名
     */
    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        
        // 为避免文件名重复，可以加上时间戳
        if (result != null) {
            result = System.currentTimeMillis() + "_" + result.replaceAll("[^a-zA-Z0-9._-]", "");
        }
        
        return result;
    }
} 