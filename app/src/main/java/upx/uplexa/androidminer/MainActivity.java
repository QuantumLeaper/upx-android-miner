/*
 *  Monero Miner App (c) 2018 Uwe Post
 *  based on the XMRig Monero Miner https://github.com/xmrig/xmrig
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 * /
 */

package upx.uplexa.androidminer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.os.PowerManager.PARTIAL_WAKE_LOCK;

//import static upx.uplexa.androidminer.MiningService.getUsername;
//import static upx.uplexa.androidminer.MiningService.setUsername;

//public class MainActivity extends Activity implements OnItemSelectedListener {
public class MainActivity extends AppCompatActivity implements OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    SharedPreferences prefs;
    boolean isShowAgain=true;
    boolean accepted=false;
    CheckBox dialogcb;


    private final static String[] SUPPORTED_ARCHITECTURES = {"arm64-v8a", "armeabi-v7a"};

    private ScheduledExecutorService svc;
    private TextView tvLog;
    private EditText edPool; //edUser
    private TextView edThreshold;
    private TextView edUser;

    private TextView tvSpeed,tvAccepted;
    private CheckBox cbUseWorkerId;
    private boolean validArchitecture = true;
    public static SharedPreferences preferences;
    public int av = 1;

    private MiningService.MiningServiceBinder binder;
    public static TextView data;
    public static Context contextOfApplication;
    public String wallet;
    public String pref;

    public static Context getContextOfApplication(){
        return contextOfApplication;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = getSharedPreferences( getPackageName() + "_preferences", MODE_PRIVATE);

        contextOfApplication = getApplicationContext();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl;
        wl = pm.newWakeLock(PARTIAL_WAKE_LOCK, "app:sleeplock");
        wl.acquire();

        Spinner edThreads = findViewById(R.id.threads);
        Spinner edMaxCpu = findViewById(R.id.maxcpu);
        prefs = getSharedPreferences("detail", MODE_PRIVATE);
        boolean isshowagain =prefs.getBoolean("show_again", true);
        if(isshowagain)
            showdialog();
        super.onCreate(savedInstanceState);
        pref = PreferenceHelper.getName();
        if(PreferenceHelper.getName() == null || PreferenceHelper.getName()=="" || PreferenceHelper.getName().length() < 23 || PreferenceHelper.getName() == "YOUR_UPX_ADDRESS_HERE") {
            PreferenceHelper.setName("YOUR_UPX_ADDRESS_HERE");
            wallet = PreferenceHelper.getName();
            setContentView(R.layout.activity_main);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
        }else{
            wallet = PreferenceHelper.getName();
            setContentView(R.layout.activity_main);
        }



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle); //Broke?
        toggle.syncState();


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.threadNumbers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        enableButtons(false);

        // wire views
        tvLog = findViewById(R.id.output);
        tvSpeed = findViewById(R.id.speed);
        tvAccepted = findViewById(R.id.accepted);
        edPool = findViewById(R.id.pool);
        edUser = findViewById(R.id.username);
        //edThreshold = findViewById(R.id.threshold);

        if(PreferenceHelper.getName() == null || PreferenceHelper.getName()=="" || PreferenceHelper.getName().length() < 23) {
            PreferenceHelper.setName("YOUR_UPX_ADDRESS_HERE");
            edUser = findViewById(R.id.username);
            edUser.setText("No wallet set! Update your Wallet Address under 'Settings'");
        }else{
            edUser.setText(PreferenceHelper.getName());
        }
        edMaxCpu = findViewById(R.id.maxcpu);
        cbUseWorkerId = findViewById(R.id.use_worker_id);

        // check architecture
        if (!Arrays.asList(SUPPORTED_ARCHITECTURES).contains(Build.CPU_ABI.toLowerCase())) {
            Toast.makeText(this, "Sorry, this app currently only supports 64 bit architectures, but yours is " + Build.CPU_ABI, Toast.LENGTH_LONG).show();
            // this flag will keep the start button disabled
            validArchitecture = false;
        }
        if (Build.CPU_ABI.toLowerCase().contains("armeabi-v7a")) {
            av = 3;
        }

        // run the service
        Intent intent = new Intent(this, MiningService.class);
        bindService(intent, serverConnection, BIND_AUTO_CREATE);
        startService(intent);



    }

    public static void setUsername(Context context, String username) {
        Context appContext = MainActivity.getContextOfApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.commit();
    }

    public static String getUsername(Context context) {
        Context appContext = MainActivity.getContextOfApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        return prefs.getString("username", "");
    }
/*
    public static void setThreshold(Context context, String threshold) {
        Context appContext = MainActivity.getContextOfApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("threshold", threshold);
        editor.commit();
    }

    public static String getThreshold(Context context) {
        Context appContext = MainActivity.getContextOfApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        return prefs.getString("threshold", "");
    }
*/
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.stats:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StatsFragment()).commit();
                break;
            /*case R.id.payments:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PaymentsFragment()).commit();
                break;*/
            case R.id.help:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HelpFragment()).commit();
                break;
            case R.id.about:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
                break;
            case R.id.miner:
                if(PreferenceHelper.getName() != null && PreferenceHelper.getName().length() > 23) { edUser.setText(PreferenceHelper.getName()); wallet = PreferenceHelper.getName(); }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MinersFragment()).commit();
                break;
            case R.id.settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
                break;

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void showdialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.disclaimer);
        dialog.setTitle("Disclaimer...");
        dialog.setCancelable(false);
        Button dialogButton = (Button) dialog.findViewById(R.id.button1);
        Button exitButton = (Button) dialog.findViewById(R.id.button2);
        dialogcb= (CheckBox) dialog.findViewById(R.id.checkBox1);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accepted = dialogcb.isChecked();
                SharedPreferences.Editor edit=prefs.edit();
                edit.putBoolean("show_again",!accepted);
                edit.commit();
                dialog.dismiss();
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finish();
            }
        });
        dialog.show();
    }

    private void startMining(View view) {
        if (binder == null) return;
        Spinner edThreads = findViewById(R.id.threads);
        Spinner edMaxCpu = findViewById(R.id.maxcpu);
        TextView threader = (TextView)edThreads.getSelectedView();
        TextView cpuer = (TextView)edMaxCpu.getSelectedView();
        MiningService.MiningConfig cfg = binder.getService().newConfig(wallet, edPool.getText().toString(),
                Integer.parseInt(threader.getText().toString()), Integer.parseInt(cpuer.getText().toString()), cbUseWorkerId.isChecked(), av);
        binder.getService().startMining(cfg);
    }

    private void stopMining(View view) {
        binder.getService().stopMining();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // the executor which will load and display the service status regularly
        svc = Executors.newSingleThreadScheduledExecutor();
        svc.scheduleWithFixedDelay(this::updateLog, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void onPause() {
        svc.shutdown();
        super.onPause();
    }

    private ServiceConnection serverConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (MiningService.MiningServiceBinder) iBinder;
            if (validArchitecture) {
                enableButtons(true);
                findViewById(R.id.start).setOnClickListener(MainActivity.this::startMining);
                findViewById(R.id.stop).setOnClickListener(MainActivity.this::stopMining);
                int cores = binder.getService().getAvailableCores();
                // write suggested cores usage into editText
                int suggested = cores / 2;
                if (suggested == 0) suggested = 1;
                ((TextView) findViewById(R.id.cpus)).setText(String.format("(%d %s)", cores, getString(R.string.cpus)));
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder = null;
            enableButtons(false);
        }
    };

    private void enableButtons(boolean enabled) {
        findViewById(R.id.start).setEnabled(enabled);
        findViewById(R.id.stop).setEnabled(enabled);
    }


    private void updateLog() {
        runOnUiThread(()->{
            if (binder != null) {
                tvLog.setText(binder.getService().getOutput());
                tvAccepted.setText(Integer.toString(binder.getService().getAccepted()));
                tvSpeed.setText(binder.getService().getSpeed());
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String edThreads = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), edThreads, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}


