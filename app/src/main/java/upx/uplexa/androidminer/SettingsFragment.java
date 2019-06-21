package upx.uplexa.androidminer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;


public class SettingsFragment extends Fragment {
    public static TextView data;
    public static EditText edUser;
    public static EditText edThreshold;
    public static EditText edWorkerID;
    public static String wallet;
    public static String workerid;
    public static float minpay;
    Button click;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false  );
        Context appContext = MainActivity.getContextOfApplication();
        data = (TextView) view.findViewById(R.id.fetchdata);
        click = view.findViewById(R.id.saveSettings);
        edUser = view.findViewById(R.id.username);
        edThreshold = view.findViewById(R.id.threshold);
        edWorkerID = view.findViewById(R.id.workerID);





        if(PreferenceHelper.getName() != null) { edUser.setText(PreferenceHelper.getName()); }
        if(PreferenceHelper.getThreshold() != null) { if(Float.parseFloat(PreferenceHelper.getThreshold()) > 75){ edThreshold.setText(PreferenceHelper.getThreshold()); } else{ edThreshold.setText("75.00"); }}
        if(PreferenceHelper.getWorkerID() != null && PreferenceHelper.getWorkerID().length() >0) { edWorkerID.setText(PreferenceHelper.getWorkerID()); } else{ edWorkerID.setText("MyAndroidDevice"); }

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save data.
                wallet = edUser.getText().toString().trim();
                workerid = edWorkerID.getText().toString().trim();
                PreferenceHelper.setName(wallet);
                minpay = Float.parseFloat(edThreshold.getText().toString());
                if(minpay < 75) {
                    Toast.makeText(appContext, "Minimum Pay Too Low!", Toast.LENGTH_SHORT).show();
                }else {
                    PreferenceHelper.setThreshold(minpay);
                    if(!workerid.contains(" ")) {
                        //success, lets post it!
                        PreferenceHelper.setWorkerID(workerid);
                        MiningService.workerId = workerid;
                        Toast.makeText(appContext, "Saved!", Toast.LENGTH_SHORT).show();

                        postData process = new postData();
                        process.execute();

                      
                    }else{
                        Toast.makeText(appContext, "Worker name may not contain spaces!", Toast.LENGTH_SHORT).show();



                    }
                }
            }
        });
        return view;

    }
}



