package cityfreqs.com.pilfershushbackscan;

import android.content.Context;

import java.io.InputStream;
import java.util.ArrayList;

public class FileProcessor {
    private Context context;
    private ArrayList<String> audio_SDK_list;
    private ArrayList<String> user_SDK_list;

    public FileProcessor(Context context) {
        this.context = context;
    }

    private boolean loadAudioSdkList() {
        //TODO requires try {} , proper file handling etc
        InputStream inStream = context.getResources().openRawResource(R.raw.Audio_SDK_names);
        // and
        String filename = "Audio_SDK_names"; // .txt
        int id = context.getResources().getIdentifier(filename, "raw", context.getPackageName());

        return false;
    }

    private boolean loadUserSdkList() {
        //TODO requires try {} , proper file handling etc
        InputStream inStream = context.getResources().openRawResource(R.raw.User_SDK_names);
        // and
        String filename = "User_SDK_names"; // .txt
        int id = context.getResources().getIdentifier(filename, "raw", context.getPackageName());

        return false;
    }


}
