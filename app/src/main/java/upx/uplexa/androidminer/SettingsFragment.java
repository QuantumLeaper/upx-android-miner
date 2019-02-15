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


public class SettingsFragment extends Fragment {
    public static TextView data;
    public static EditText edUser;
    public static String wallet;
    Button click;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false  );
        Context appContext = MainActivity.getContextOfApplication();
        data = (TextView) view.findViewById(R.id.fetchdata);
        click = view.findViewById(R.id.saveSettings);
        edUser = view.findViewById(R.id.username);
        if(PreferenceHelper.getName() != null) { edUser.setText(PreferenceHelper.getName()); }
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save data.
                wallet = edUser.getText().toString().trim();
                PreferenceHelper.setName(wallet);
                Toast.makeText(appContext, "Saved!", Toast.LENGTH_SHORT).show();
            }
        });
        return view;

    }
}



