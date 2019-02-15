package upx.uplexa.androidminer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class StatsFragment extends Fragment {
    public static TextView data;
    public static EditText edUser;
    public static String wallet;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false  );
        View view2 = inflater.inflate(R.layout.fragment_settings, container, false  );
        Context appContext = MainActivity.getContextOfApplication();
        data = (TextView) view.findViewById(R.id.fetchdata);
        edUser = view.findViewById(R.id.username);
        wallet = PreferenceHelper.getName();
        fetchData process = new fetchData();
        process.execute();
        return view;
    }
}


