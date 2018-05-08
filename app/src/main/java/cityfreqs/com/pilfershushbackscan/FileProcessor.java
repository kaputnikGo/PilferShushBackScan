package cityfreqs.com.pilfershushbackscan;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FileProcessor {
    private Context context;
    private String[] audioSdkArray;
    private String[] userSdkArray;

    protected FileProcessor(Context context) {
        this.context = context;
    }

    protected String[] getAudioSdkArray() {
        // should always be an internal list of size > 1
        //TODO fix the logic here
        if (audioSdkArray == null) {
            // maybe not created yet...
            if (loadAudioSdkList()) {
                return audioSdkArray;
            }
            else {
                // error in finding and loading internal sdk list
                return null;
            }
        }
        else if (audioSdkArray.length > 0)
            return audioSdkArray;
        else {
            // no list made, trigger it
            if (loadAudioSdkList()) {
                return audioSdkArray;
            }
            else {
                // error in finding and loading internal sdk list
                return null;
            }
        }
    }

    protected String[] getUserSdkArray() {
        // the user list may not exist
        //TODO fix the logic here
        if (userSdkArray == null) {
            // maybe not created yet...
            if (loadUserSdkList()) {
                return userSdkArray;
            }
            else {
                // no finding and loading user sdk list
                return null;
            }
        }
        else if (userSdkArray.length > 0)
            return userSdkArray;
        else {
            // no list made, trigger it
            if (loadUserSdkList()) {
                return userSdkArray;
            }
            else {
                // error in finding and loading internal sdk list
                return null;
            }
        }
    }



    private boolean loadAudioSdkList() {
        // BackScan internal list of audio beacon sdk package names
        try {
            InputStream audioSdkInput = context.getResources().openRawResource(R.raw.audio_sdk_names);
            BufferedReader audioSdkStream = new BufferedReader(new InputStreamReader(audioSdkInput));

            ArrayList<String> audioSdkList = new ArrayList<>();
            String audioSdkLine;
            while ((audioSdkLine = audioSdkStream.readLine()) != null) {
                audioSdkList.add(audioSdkLine);
            }
            // clean up
            audioSdkInput.close();
            audioSdkStream.close();
            // convert list to array
            if (audioSdkList.isEmpty()) {
                return false;
            }
            else {
                audioSdkArray = audioSdkList.toArray(new String[audioSdkList.size()]);
                return true;
            }
        }
        catch (Exception ex) {
            // error
            return false;
        }
    }

    private boolean loadUserSdkList() {
        // may consist of package names that aren't audio beacon types, ie trackers etc.
        // may also be empty, ie unused
        try {
            InputStream userSdkInput = context.getResources().openRawResource(R.raw.user_sdk_names);
            BufferedReader userSdkStream = new BufferedReader(new InputStreamReader(userSdkInput));

            ArrayList<String> userSdkList = new ArrayList<>();
            String userSdkLine;
            while ((userSdkLine = userSdkStream.readLine()) != null) {
                userSdkList.add(userSdkLine);
            }
            // clean up
            userSdkInput.close();
            userSdkStream.close();
            // convert list to array
            if (userSdkList.isEmpty()) {
                return false;
            }
            else {
                userSdkArray = userSdkList.toArray(new String[userSdkList.size()]);
                return true;
            }
        }
        catch (Exception ex) {
            // error
            return false;
        }
    }


}
