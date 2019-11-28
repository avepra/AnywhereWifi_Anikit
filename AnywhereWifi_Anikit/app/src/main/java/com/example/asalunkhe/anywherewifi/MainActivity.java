package com.example.asalunkhe.anywherewifi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.TimeUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.R.id.list;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String APP_DEBUG = "BEACON_DEBUG";

    //String[] validBSSIDlist = {"3C:7A:8A:37:28:F4", "3C:7A:8A:37:28:FA"};     //XB3
    //String[] validBSSIDlist = {"A4:15:88:F9:68:70", "A4:15:88:F9:68:78"};       //OG-1600
    String[] validBSSIDlist = {"A4:15:88:F9:68:70", "A4:15:88:F9:68:78", "A4:15:88:F9:68:71", "A4:15:88:F9:68:79"};     //MORE OG-1600
    //String[] validBSSIDlist = {"3C:7A:8A:37:28:F4", "3C:7A:8A:37:28:FA", "A4:15:88:F9:68:70", "A4:15:88:F9:68:78", "A4:15:88:F9:68:71", "A4:15:88:F9:68:79"};     //ALL

    final String SSID_SEPRATOR = "_";

    final int NOTIFICATION_SKIP_MINS = 10;

    WifiManager wifiManager;
    WifiScanReceiver wifiReceiver;
    private static final int REQUEST_CODE_FOR_WIFI_PERMISSION = 12345;

    String m_strReceivedSSID;
    String m_strReceivedBSSID;
    private FirebaseAnalytics mFirebaseAnalytics;

    //NOTIFICATION
    public class NotificationType {
        static final int BEGIN = 0;
        static final int VENDOR = 1;
        static final int TARGETED_AD = 2;
        static final int EAS = 3;
        static final int LIVE = 4;
        static final int END = 5;
    }
    static String[] lastNotifications = new String[] {"", "", "", "", "", ""};
    static int visibilityConnectQue = View.INVISIBLE;
    static List<String> notificationsReceived = new ArrayList<String>();
    static List<Date> notificationsTime = new ArrayList<Date>();

    TextView txtConnectQue;
    CardView continueLayout;
    Button butConnectYes;
    Button butConnectNo;

    TextView txtVendorValue;
    TextView txtADValue;
    TextView txtEASValue;
    TextView txtLiveValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();

        initializeWifi();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setUserId("1234");
        //mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);

    }

    protected void onPause() {
        //unregisterReceiver(wifiReceiver);
        super.onPause();
    }

    protected void onResume() {
        setNoficiations();
        //startWifiScan();
        super.onResume();
    }

    void initializeComponents(){
        continueLayout = (CardView)findViewById(R.id.continue_layout);
        txtConnectQue = (TextView)findViewById(R.id.textConnetQue);
        butConnectYes = (Button) findViewById(R.id.buttonConnectYes);
        butConnectNo  = (Button) findViewById(R.id.buttonConnectNo);

        txtVendorValue = (TextView) findViewById(R.id.textVendorValue);
        txtADValue = (TextView) findViewById(R.id.textADValue);
        txtEASValue = (TextView) findViewById(R.id.textEASValue);
        txtLiveValue = (TextView) findViewById(R.id.textLiveValue);

        butConnectYes.setOnClickListener(this);
        butConnectNo.setOnClickListener(this);

        txtADValue.setOnClickListener(this);
    }

    void setNoficiations() {
        txtVendorValue.setText(lastNotifications[NotificationType.VENDOR]);
        txtADValue.setText(lastNotifications[NotificationType.TARGETED_AD]);
        txtEASValue.setText(lastNotifications[NotificationType.EAS]);
        txtLiveValue.setText(lastNotifications[NotificationType.LIVE]);

        continueLayout.setVisibility(visibilityConnectQue);
       /* txtConnectQue.setVisibility(visibilityConnectQue);
        butConnectYes.setVisibility(visibilityConnectQue);
        butConnectNo.setVisibility(visibilityConnectQue);*/
    }

    void initializeWifi() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        askWifiGpsEnable();
        getWifiLocationPermission();
    }
    public static int scan_count = 0;
    void startWifiScan() {
        wifiReceiver = new WifiScanReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifiManager.startScan();
        Toast.makeText(this, "Started scan= " + scan_count, Toast.LENGTH_SHORT).show();
        ++scan_count;
    }

    private void getWifiLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, (new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}),
                    REQUEST_CODE_FOR_WIFI_PERMISSION);
            //Toast.makeText(this, "getWifiLocationPermission() requesting", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(this, "getWifiLocationPermission() already granted", Toast.LENGTH_SHORT).show();
            //startWifiScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_FOR_WIFI_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            //Toast.makeText(this, "onRequestPermissionsResult() granted", Toast.LENGTH_SHORT).show();
        }
        else {
            //Toast.makeText(this, "onRequestPermissionsResult() not granted", Toast.LENGTH_SHORT).show();
            //startWifiScan();
        }
    }

    void askWifiGpsEnable() {
        final boolean enabledWifi = wifiManager.isWifiEnabled();

        //LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        //final boolean enabledGps = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        final boolean enabledGps = true;

        //PROMPT TO ENABLE WIFI &/or GPS, IF NOT
        if( !enabledWifi || !enabledGps ) {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            if( enabledWifi )
                alertDialog.setMessage("GPS needs to be enabled for app. Do you want to continue?");
            else if( enabledGps )
                alertDialog.setMessage("WiFi needs to be enabled for app. Do you want to continue?");
            else
                alertDialog.setMessage("WiFi & GPS needs to be enabled for app. Do you want to continue?");

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if( !enabledWifi ) {    //AUTO ENABLING WIFI
                        wifiManager.setWifiEnabled(true);
                    }
                    if( !enabledGps ) {     //AUTO ENABLING GPS
                        final Intent poke = new Intent();
                        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                        poke.setData(Uri.parse("3"));
                        sendBroadcast(poke);
                    }
                    if( enabledWifi )
                        Toast.makeText(getApplicationContext(), "Enabling GPS", Toast.LENGTH_SHORT).show();
                    else if( enabledGps )
                        Toast.makeText(getApplicationContext(), "Enabling WIFI", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "Enabling WIFI & GPS", Toast.LENGTH_SHORT).show();

                    startWifiScan();
                }
            });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getApplicationContext(), "Exiting app", Toast.LENGTH_SHORT).show();
                    System.exit(0);
                }
            });

            alertDialog.show();
        }
        else {
            startWifiScan();
        }
    }

    void autoConnectSSID(final String strReceivedSSID, final String strReceivedBSSID) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.connect);
        dialog.setTitle("Connect to Network");
        TextView textSSID = (TextView) dialog.findViewById(R.id.textSSID1);
        textSSID.setText(strReceivedSSID);

        Button dialogButton = (Button) dialog.findViewById(R.id.okButton);
        final EditText password = (EditText) dialog.findViewById(R.id.textPassword);
        password.setHint("password");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // if button is clicked, connect to the network;
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checkPassword = password.getText().toString();
                finallyConnect(strReceivedSSID, strReceivedBSSID, checkPassword);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    int finallyConnect(String networkSSID, String networkBSSID, String networkPass) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        //wifiConfig.BSSID = String.format("\"%s\"", networkBSSID);
        //wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        // remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        /*WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"\"" + networkSSID + "\"\"";
        conf.preSharedKey = "\"" + networkPass + "\"";
        int ret = wifiManager.addNetwork(conf);*/
        return netId;
    }

    void doneVendorConnect() {
        visibilityConnectQue = View.GONE;
        continueLayout.setVisibility(visibilityConnectQue);
        /*txtConnectQue.setVisibility(visibilityConnectQue);
        butConnectYes.setVisibility(visibilityConnectQue);
        butConnectNo.setVisibility(visibilityConnectQue);*/
    }

    void enabledVendorConnection(String strSSID, String strBSSID) {
        visibilityConnectQue = View.VISIBLE;
        continueLayout.setVisibility(visibilityConnectQue);
        /*txtConnectQue.setVisibility(visibilityConnectQue);
        butConnectYes.setVisibility(visibilityConnectQue);
        butConnectNo.setVisibility(visibilityConnectQue);*/

        m_strReceivedSSID = strSSID;
        m_strReceivedBSSID = strBSSID;
    }

    static int new_notification = 1;
    void receivedNotifications(TextView txtValueControl, String strValue, String strToastMsg, String strType) {
        txtValueControl.setText(strValue);
        Toast.makeText(this, strToastMsg, Toast.LENGTH_LONG).show();

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_bp)
                        .setContentTitle(strType)
                        .setContentText(strValue)
                        .setAutoCancel(true)
                        .setSound(alarmSound);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        manager.notify(new_notification, builder.build());
        ++new_notification;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.buttonConnectYes:
                if( finallyConnect(m_strReceivedSSID, m_strReceivedBSSID, "") == -1 )
                    Toast.makeText(this, "Failed to connect. Please try again later.", Toast.LENGTH_LONG).show();
                else {
                    Toast.makeText(this, "Connecting ...", Toast.LENGTH_SHORT).show();
                    doneVendorConnect();
                }
                break;

            case R.id.buttonConnectNo:
                doneVendorConnect();
                break;

            case R.id.textADValue:
                String strADValue = txtADValue.getText().toString();
                if( strADValue.contains("www.") ) {
                    if (!strADValue.startsWith("http://") && !strADValue.startsWith("https://"))
                        strADValue = "http://" + strADValue;
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(strADValue));
                    startActivity(browserIntent);
                }
        }
    }


    public static int wifi_sacn_count = 0;
    class WifiScanReceiver extends BroadcastReceiver {
        @SuppressLint("UseValueOf")
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifiManager.getScanResults();
            if( wifiScanList == null )
                return;




            Log.d(APP_DEBUG, "WIFI UPDATE");
            Toast.makeText(c, "WIFI UPDATE #"+ wifi_sacn_count, Toast.LENGTH_SHORT).show();
            ++wifi_sacn_count;

            Date now = new Date();
            Date before10mins = new Date(now.getYear(), now.getMonth(), now.getDate(), now.getHours(), now.getMinutes() - NOTIFICATION_SKIP_MINS, now.getSeconds());

            for(int i = 0; i < wifiScanList.size(); i++){
                //Log.d(APP_DEBUG, "CHECKING FOR VALID BSSID=" + wifiScanList.get(i).BSSID + " FOR SSID=" + wifiScanList.get(i).SSID);
                if( isStringExistInArrayIgnoreCase(validBSSIDlist, wifiScanList.get(i).BSSID) ) {  //FOUND A VALID BSSID
                    Log.d(APP_DEBUG, "FOUND VALID BSSID=" + wifiScanList.get(i).BSSID );


                    byte[] decryptSSIDBytes = Base64.decode(wifiScanList.get(i).SSID, Base64.DEFAULT);
                    String decryptSSID = new String(decryptSSIDBytes);

                    if( !decryptSSID.contains(SSID_SEPRATOR) ) {
                        Toast.makeText(c, "ERROR CASE: No SSID SEPERATOR", Toast.LENGTH_SHORT).show();
                        continue;
                    }

                    if( decryptSSID.contains(System.getProperty("line.separator")) )
                        decryptSSID = decryptSSID.replaceAll(System.getProperty("line.separator"), "");

                    String[] validSSIDsplit = decryptSSID.split(SSID_SEPRATOR, 2);

//                if ( true ) {
//                    String[] validSSIDsplit = new String[] {String.valueOf(i+1), wifiScanList.get(i).SSID};

                    Integer notificationID = Integer.parseInt(validSSIDsplit[0]);
                    Log.d(APP_DEBUG, "NOTIFICATION ID=" + notificationID + " MSG=" + validSSIDsplit[1] );


                    if( notificationID <= NotificationType.BEGIN || NotificationType.END <= notificationID ) {
                        Log.d(APP_DEBUG, "SKIPPING INVDALID NOTIFICATION");
                        continue;
                    }
                    /*if( lastNotifications[notificationID].equals(validSSIDsplit[1]) ) {  //NOTIFICATION IS SAME AS PREVIOUS, IGNORING IT
                        Log.d(APP_DEBUG, "SKIPPING EXISTING NOTIFICATION");
                        continue;
                    }*/

                    int index = isStringExistInArrayIgnoreCase(notificationsReceived, validSSIDsplit[1]);
                    if( 0 <= index ){
                        if( before10mins.after(notificationsTime.get(index)) ) {
                            Log.d(APP_DEBUG, "UPDATING OLD NOTIFICATION");
                            notificationsTime.set(index, now);
                        }
                        else {
                            Log.d(APP_DEBUG, "SKIPPING EXISTING NOTIFICATION");
                            continue;
                        }
                    }
                    else {
                        notificationsReceived.add(validSSIDsplit[1]);
                        notificationsTime.add(now);
                        Log.d(APP_DEBUG, "NEW NOTIFICATION");


                    }

                    Bundle bundle = new Bundle();

                    switch(notificationID) {
                        case NotificationType.VENDOR :
                            receivedNotifications(txtVendorValue, validSSIDsplit[1], "Found authentic vendor network", "Wi-Fi Availibility");
                            enabledVendorConnection(wifiScanList.get(i).SSID, wifiScanList.get(i).BSSID);


                            bundle.putString("property_name","VENDOR");
                            bundle.putString("item_value", "COMCAST");
                            mFirebaseAnalytics.logEvent("wifi_availability",bundle);
                            break;

                        case NotificationType.TARGETED_AD :
                            receivedNotifications(txtADValue, validSSIDsplit[1], "Received Targeted AD", "Targeted AD");
                       //     Bundle bundle = new Bundle();
                            bundle.putString("property_name","AD");
                            bundle.putString("property_value", "www.facebook.com");
                            mFirebaseAnalytics.logEvent("targeted_ad",bundle);

                            break;
                        case NotificationType.EAS :
                            receivedNotifications(txtEASValue, validSSIDsplit[1], "Received Emergency Alert", "Emergency Alert");
                         //   Bundle bundle = new Bundle();
                            bundle.putString("property_name","EAS");
                            bundle.putString("property_value"," Earth quake detect");
                            mFirebaseAnalytics.logEvent("emergency_alert",bundle);

                            break;
                        case NotificationType.LIVE :
                            receivedNotifications(txtLiveValue, validSSIDsplit[1], "Received Live Update", "Live Update");
                           // Bundle bundle = new Bundle();
                            bundle.putString("property_name","live_notifications");
                            bundle.putString("item_value", "Sachin scores a century");
                            mFirebaseAnalytics.logEvent("live_notifications",bundle);
                            break;
                    }
                    lastNotifications[notificationID] = validSSIDsplit[1];
                }   //VALID SSID
            }   //WIFI SCAN LOOP

            //REMOVING OLD NOTIFICATIONS
            for(int i = 0 ; i < notificationsReceived.size() ; ) {
                if ( before10mins.after(notificationsTime.get(i)) ) {
                    notificationsReceived.remove(i);
                    notificationsTime.remove(i);
                }
                else
                    ++i;
            }
        }
    }


    boolean isStringExistInArray(String[] array, String find) {
        for (String str : array) {
            if (find.equals(str)) { return true; }
        }
        return false;
    }
    boolean isStringExistInArrayIgnoreCase(String[] array, String find) {
        for (String str : array) {
            if (find.toLowerCase().equals(str.toLowerCase())) { return true; }
        }
        return false;
    }

    int isStringExistInArrayIgnoreCase(List<String> array, String find) {
        int index = 0;
        for (String str : array) {
            if (find.toLowerCase().equals(str.toLowerCase()))   return index;
            ++index;
        }
        return -1;
    }
}
