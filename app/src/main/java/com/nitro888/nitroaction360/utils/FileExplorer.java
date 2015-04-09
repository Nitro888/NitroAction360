package com.nitro888.nitroaction360.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
/**
 * Created by nitro888 on 15. 4. 9..
 */
public class FileExplorer {
    private static String getExtension(String name){
        return name.substring(name.lastIndexOf(".")+1,name.length()).toLowerCase();
    }
    private static boolean isMediaFileType(String name){
        switch (getExtension(name)) {
            case "gp3" :
            case "mp4" :
            case "ts"  :
            case "webm":
            case "mkv" :
                return true;
        }
        return false;
    }
    public static String getRoot() {
        return Environment.getExternalStorageDirectory().getPath();
    }
    public static List<String>[] getDir(String dirPath) {
        final List<String> item = new ArrayList<String>();
        final List<String> path = new ArrayList<String>();

        File f          = new File(dirPath);
        File[] files    = f.listFiles();

        for(int i=0; i < files.length; i++)
        {
            File file = files[i];

            if(!file.isHidden() && file.canRead()){
                path.add(file.getPath());
                if(file.isDirectory())
                    item.add(file.getName() + "/");
                else if(isMediaFileType(file.getName()))
                    item.add(file.getName());
            }
        }

        List<String>[]  result = new List[2];

        result[0]   = path;
        result[1]   = item;

        return result;
    }
    public static boolean isDirectory(String path) {
        final File file = new File(path);
        return file.isDirectory();
    }
    public static int isMediaFile(String path) { // 0 = directory, 1=can't read, 2=media file
        final File file = new File(path);
        if(file.isDirectory())  return 0;
        if(!file.canRead())     return 1;
        return 2;
    }
}
