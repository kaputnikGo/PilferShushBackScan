package cityfreqs.com.pilfershushbackscan;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "PilferShush_BackScan";
    public static final String VERSION = "1.0.01";
    private static final int REQUEST_WRITE_PERMISSION = 1;

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

        fileProcessor = new FileProcessor(this);
        backgroundChecker = new BackgroundChecker(fileProcessor);

        // runtime permissions for external storage write for user SDK names
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(this,
                        new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        REQUEST_WRITE_PERMISSION);
            }
            else {
                appStart();
            }
        }
        else {
            appStart();
        }
    }

    private void appStart() {
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    appStart();
                }
                else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.perms_state_1), Toast.LENGTH_SHORT)
                            .show();
                    // start anyway, with reduced function
                    appStart();
                }
                return;
            }
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
            case R.id.action_override_scan:
                hasUserAppsList();
                return true;
            case R.id.action_beacon_list:
                displayBeaconSdkList();
                return true;
            case R.id.action_user_entry:
                addUserSdkList();
                return true;
            case R.id.action_delete_entry:
                deleteUserSdkList();
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
        return backgroundChecker.hasAudioBeaconApps();
    }
    protected int getAudioBeaconAppNumber() {
        return backgroundChecker.getAudioBeaconAppNames().length;
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
        entryLogger("\n" + getResources().getString(R.string.main_scanner_2) + "\n"
                + backgroundChecker.displayAudioSdkNames(), false);
        // add any user names
        entryLogger(backgroundChecker.displayUserSdkNames() + "\n", false);
    }

/*
* 	CHECKS
*/
    private void auditBackgroundChecks() {
        // is good
       entryLogger(getResources().getString(R.string.background_scan_2) + "\n", false);
        backgroundChecker.runChecker();

        entryLogger(getResources().getString(R.string.background_scan_3) + backgroundChecker.getUserRecordNumApps() + "\n", false);

        backgroundChecker.checkAudioBeaconApps();

        backgroundChecker.audioAppEntryLog();
    }

    private void listAppOverrideScanDetails(int selectedIndex) {
        // check for receivers too?
        entryLogger("\n" + getResources().getString(R.string.background_scan_6)
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

    private void addUserSdkList() {
        dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View inputView = inflater.inflate(R.layout.add_sdk_name, null);
        dialogBuilder.setView(inputView);
        final EditText userInput = (EditText) inputView.findViewById(R.id.add_sdk_name_input);

        dialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (fileProcessor.addUserSdkName(userInput.getText().toString())) {

                            entryLogger("New SDK name added.", false);
                        }
                        else {
                            entryLogger("Failed to add new SDK name.", true);
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // dismissed
                        alertDialog.cancel();
                    }
                });
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void deleteUserSdkList() {
        // deletes the whole file
        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setPositiveButton(R.string.dialog_button_okay, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (fileProcessor.deleteUserSdkFile()) {
                    entryLogger("User added SDK file deleted.", false);
                }
                else {
                    entryLogger("Error deleting user sdk file.", true);
                }
            }
        });
        dialogBuilder.setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                alertDialog.cancel();
            }
        });
        alertDialog = dialogBuilder.create();
        alertDialog.show();
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
