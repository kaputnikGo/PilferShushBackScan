package cityfreqs.com.pilfershushbackscan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PilferShush_BackScan";
    public static final String VERSION = "1.0.01";

    private FileProcessor fileProcessor;
    private BackgroundChecker backgroundChecker;
    private static TextView debugText;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;

    // two plaintext files: Audio_SDK_names.txt, User_SDK_names.txt

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //
        debugText = (TextView) findViewById(R.id.debug_text);
        debugText.setTextColor(Color.parseColor("#00ff00"));
        debugText.setMovementMethod(new ScrollingMovementMethod());
        debugText.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugText.setSoundEffectsEnabled(false); // no further click sounds
            }
        });
        // just do it...
        fileProcessor = new FileProcessor(this);
        backgroundChecker = new BackgroundChecker(fileProcessor);

        if (runBackgroundChecks()) {
            // report
            int audioNum = getAudioRecordAppsNumber();
            if (audioNum > 0) {
                entryLogger(getResources().getString(R.string.main_scanner_3) + audioNum, true);
            }
            else {
                entryLogger(getResources().getString(R.string.main_scanner_4), false);
            }
            if (hasAudioBeaconApps()) {
                entryLogger(getAudioBeaconAppNumber()
                        + getResources().getString(R.string.main_scanner_5), true);
            }
            else {
                entryLogger(getResources().getString(R.string.main_scanner_6), false);
            }
        }
        else {
            //TODO
            // this function is of concern as it may not work, uses logcat
            // may bot be useful here as its intention is to trip up RECORD_AUDIO detection
            // therefore would need the mic checking function
            backgroundChecker.auditLogAsync();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_audio_beacons:
                hasAudioBeaconAppsList();
                return true;
            case R.id.action_override_scan:
                hasUserAppsList();
                return true;
            case R.id.action_beacon_list:
                displayBeaconSdkList();
                return true;
            default:
                // do not consume the action
                return super.onOptionsItemSelected(item);
        }
    }

    /********************************************************************/

    protected int getAudioRecordAppsNumber() {
        return backgroundChecker.getUserRecordNumApps();
    }
    protected boolean hasAudioBeaconApps() {
        return backgroundChecker.checkAudioBeaconApps();
    }
    protected int getAudioBeaconAppNumber() {
        return backgroundChecker.getAudioBeaconAppNames().length;
    }
    protected String[] getAudioBeaconAppList() {
        return backgroundChecker.getAudioBeaconAppNames();
    }

    protected boolean runBackgroundChecks() {
        if (backgroundChecker.initChecker(this.getPackageManager())) {
            // is good
            auditBackgroundChecks();
            return true;
        }
        else {
            // is bad
            entryLogger(getResources().getString(R.string.background_scan_1), true);
            return false;
        }
    }

    protected void displayBeaconSdkList() {
        // current matching method uses:
        // Returns true if and only if this string contains the specified sequence of char values.
        // if (name.contains(SDK_NAMES[i])) {}
        entryLogger("\nCurrent list of Audio beacon SDK names searched for: \n"
                + backgroundChecker.displayAudioSdkNames(), false);
    }

/*
* 	CHECKS
*/
    private void auditBackgroundChecks() {
        // is good
       entryLogger(getResources().getString(R.string.background_scan_2) + "\n", false);
        backgroundChecker.runChecker();

        entryLogger(getResources().getString(R.string.background_scan_3) + backgroundChecker.getUserRecordNumApps() + "\n", false);

        backgroundChecker.audioAppEntryLog();
    }

    private void listAppAudioBeaconDetails(int selectedIndex) {
        if (backgroundChecker.getAudioBeaconAppEntry(selectedIndex).checkBeaconServiceNames()) {
            entryLogger(getResources().getString(R.string.background_scan_4)
                    + backgroundChecker.getAudioBeaconAppEntry(selectedIndex).getActivityName()
                    + ": " + backgroundChecker.getAudioBeaconAppEntry(selectedIndex).getBeaconServiceNamesNum(), true);

            logAppEntryInfo(backgroundChecker.getAudioBeaconAppEntry(selectedIndex).getBeaconServiceNames());
        }
        //TODO
        // add a call for any receiver names too
        if (backgroundChecker.getAudioBeaconAppEntry(selectedIndex).checkBeaconReceiverNames()) {
            entryLogger(getResources().getString(R.string.background_scan_5)
                    + backgroundChecker.getAudioBeaconAppEntry(selectedIndex).getActivityName()
                    + ": " + backgroundChecker.getAudioBeaconAppEntry(selectedIndex).getBeaconReceiverNamesNum(), true);

            logAppEntryInfo(backgroundChecker.getAudioBeaconAppEntry(selectedIndex).getBeaconReceiverNames());
        }
    }

    private void listAppOverrideScanDetails(int selectedIndex) {
        // check for receivers too?
        entryLogger(getResources().getString(R.string.background_scan_6)
                + backgroundChecker.getOverrideScanAppEntry(selectedIndex).getActivityName()
                + ": " + backgroundChecker.getOverrideScanAppEntry(selectedIndex).getServicesNum(), true);

        if (backgroundChecker.getOverrideScanAppEntry(selectedIndex).getServicesNum() > 0) {
            logAppEntryInfo(backgroundChecker.getOverrideScanAppEntry(selectedIndex).getServiceNames());
        }

        entryLogger(getResources().getString(R.string.background_scan_7)
                + backgroundChecker.getOverrideScanAppEntry(selectedIndex).getActivityName()
                + ": " + backgroundChecker.getOverrideScanAppEntry(selectedIndex).getReceiversNum(), true);

        if (backgroundChecker.getOverrideScanAppEntry(selectedIndex).getReceiversNum() > 0) {
            logAppEntryInfo(backgroundChecker.getOverrideScanAppEntry(selectedIndex).getReceiverNames());
        }
    }

    private void logAppEntryInfo(String[] appEntryInfoList) {
        entryLogger("\n" + getResources().getString(R.string.background_scan_8) + "\n", false);
        for (int i = 0; i < appEntryInfoList.length; i++) {
            entryLogger(appEntryInfoList[i] + "\n", false);
        }
    }


    private void hasAudioBeaconAppsList() {
        String[] appNames = getAudioBeaconAppList();

        if (appNames != null && appNames.length > 0) {
            // proceed to list
            dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setItems(appNames, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int which) {
                    // index position of clicked app name
                    listAppAudioBeaconDetails(which);
                }
            });
            dialogBuilder.setTitle(R.string.dialog_audio_beacon_apps);
            alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
        else {
            // none found, inform user
            entryLogger(getResources().getString(R.string.audio_apps_check_1), true);
        }
    }

    private void hasUserAppsList() {
        String[] appNames = backgroundChecker.getOverrideScanAppNames();

        if (appNames != null && appNames.length > 0) {
            dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setItems(appNames, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int which) {
                    // index position of clicked app name
                    listAppOverrideScanDetails(which);
                }
            });
            dialogBuilder.setTitle(R.string.dialog_override_scan_apps);
            alertDialog = dialogBuilder.create();
            alertDialog.show();
        }
        else {
            entryLogger(getResources().getString(R.string.user_apps_check_1), true);
        }
    }


/*


*/
    protected static void entryLogger(String entry, boolean caution) {
        int start = debugText.getText().length();
        debugText.append("\n" + entry);
        int end = debugText.getText().length();
        Spannable spannableText = (Spannable) debugText.getText();
        if (caution) {
            spannableText.setSpan(new ForegroundColorSpan(Color.YELLOW), start, end, 0);
        }
        else {
            spannableText.setSpan(new ForegroundColorSpan(Color.GREEN), start, end, 0);
        }
    }
}
