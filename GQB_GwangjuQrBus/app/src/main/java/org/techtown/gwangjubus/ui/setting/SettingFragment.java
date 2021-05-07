package org.techtown.gwangjubus.ui.setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.techtown.gwangjubus.R;
import org.techtown.gwangjubus.ui.busstop.StationFragment;

public class SettingFragment extends Fragment {

    private SettingViewModel settingViewModel;
    private long contentView;
    private Object setContentView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_setting, container, false);

        Button setting_button1 = rootView.findViewById(R.id.setting_button1);
        Button setting_button2 = rootView.findViewById(R.id.setting_button2);
        Button setting_button3 = rootView.findViewById(R.id.setting_button3);
        Button setting_button4 = rootView.findViewById(R.id.setting_button4);

        setting_button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Toast.makeText(getActivity(), "업데이트 예정입니다! ", Toast.LENGTH_SHORT).show();
            }
        });

        setting_button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(("https://baroeungdap.gwangju.go.kr/")));
                startActivity(myIntent);
            }
        });

        setting_button3.setOnClickListener(new View.OnClickListener() {
                                               public void onClick(View v){
                                                   Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(("tel:062-120")));
                                                   startActivity(myIntent);
                                               }
                                           }
        );

        setting_button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){

                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(("http://bus.gwangju.go.kr/guide/bustime/busTime")));
                startActivity(myIntent);
            }
            /*
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();


            public void onClick(View v){

                transaction.replace(R.id.fragment_setting, alarm_setting_Fragment);
                transaction.commit();
            }
            */
        });



        return rootView;
    }

    public void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_setting, fragment);
        fragmentTransaction.commit();
    }



}