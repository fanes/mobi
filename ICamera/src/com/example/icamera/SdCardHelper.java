package com.example.icamera;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

public class SdCardHelper {

    private static String[] getSdcardPaths(Activity ac)
    {
        String paths[] = null;
        StorageManager sm = (StorageManager) ac.getSystemService(Context.STORAGE_SERVICE);
        try {
            paths = (String[]) sm.getClass().getMethod("getVolumePaths", null).invoke(sm, null);
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (InvocationTargetException e){
            e.printStackTrace();
        } catch (NoSuchMethodException e){
            e.printStackTrace();
        }
       return paths;
    }

    private static String getMountedStorageDirectory(Activity ac)
    {
        String paths[] = getSdcardPaths(ac);
        for( int i=0;i<paths.length;i++ )
        {
        	Log.d("mydebug", "files Dir: " + paths[i]);
    		Log.d("mydebug", "fileDir can read: " + new File(paths[i]).canRead());
    		Log.d("mydebug", "fileDir can write: " + new File(paths[i]).canWrite());
           String p = paths[i] + "/_file111111111111111";
            try
            {
                File f = new File( p );
                if( !f.exists() )
                {
                    if( f.mkdirs() )
                    {
                        f.delete();
                        return paths[i];
                    }
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static String getStorageDirectory(Activity ac)
    {
        if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) )
        {
           return  Environment.getExternalStorageDirectory().toString();
        }
        else
        {
            return getMountedStorageDirectory(ac);
        }
    }

}


