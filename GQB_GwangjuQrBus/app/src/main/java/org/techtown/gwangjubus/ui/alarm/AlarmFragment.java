package org.techtown.gwangjubus.ui.alarm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.techtown.gwangjubus.MainActivity;
import org.techtown.gwangjubus.R;
import org.techtown.gwangjubus.data.NetworkVariable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/*
AlarmFragment : 현재 승차 요청 or 탑승한 버스의 하차 요청
 */

public class AlarmFragment extends Fragment  {

    Context context;
    String search_id;
    TextView hachaBusname;
    TextView busCurrentStation;
    TextView hachaBusstop;

    String hacha_station_text; // 하차할 정류장
    EditText hacha_station; // 하차할 정류장 입력 받는 변수

    // 소켓 통신 변수
    private Socket socket;
    BufferedReader socket_in;
    PrintWriter socket_out;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity().getBaseContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_alarm, container, false); //add

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // 하차 정보 설정
        hachaBusname = (TextView) rootView.findViewById(R.id.hacha_busName);
        hachaBusname.setText(((MainActivity)getActivity()).lineName);
        busCurrentStation = (TextView) rootView.findViewById(R.id.bus_current_station);
        busCurrentStation.setText(((MainActivity)getActivity()).busstopName);
        hachaBusstop = (TextView)  rootView.findViewById(R.id.hacha_busstop);

        Button hacha_reservation_cancel_button = rootView.findViewById(R.id.hacha_reservation_cancel_button);
        Button hacha_reservation_button = rootView.findViewById(R.id.hacha_reservation_button);
        hacha_station = (EditText) rootView.findViewById((R.id.hacha_station));

        search_id = ((MainActivity)getActivity()).busId;

        // 하차 취소 버튼 클릭시
        hacha_reservation_cancel_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                showCancle();
            }
        });

        // 하차 요청 버튼 클릭시
        hacha_reservation_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                show();
            }
        });

        return rootView;
    }


    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_home, fragment);
        fragmentTransaction.commit();
    }

    // 하차 요청 함수
    void show()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("하차 예약");
        builder.setMessage("하차 예약 하시겠습니까?");
        builder.setPositiveButton("아니요",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.setNegativeButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(),"하차가 예약 되었습니다..",Toast.LENGTH_LONG).show();
                        hacha_station_text = hacha_station.getText().toString(); //내릴 정류장 텍스트 가져오기
                        hachaBusstop.setText(hacha_station_text);

                        String busid = ((MainActivity)getActivity()).busId;

                        Thread worker = new Thread(){
                            public void run(){
                                try {
                                    socket = new Socket("168.131.151.207", NetworkVariable.takeOff);
                                    socket_out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                                    socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                                    if (busid != null && hacha_station_text != null){
                                        socket_out.println(busid);
                                        socket_out.println(hacha_station_text);
                                    }
                                } catch (IOException e){
                                    e.printStackTrace();
                                }
                                try {
                                    while(true) {
                                        ((MainActivity)getActivity()).busId = socket_in.readLine();
                                        hacha_station_text = socket_in.readLine();
                                    }
                                } catch (Exception e){
                                }
                            }
                        };

                        worker.start();

                    }
                });

        builder.show();
    }

    // 하차 요청 취소 함수
    void showCancle()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("하차 취소");
        builder.setMessage("하차 취소 하시겠습니까?");
        builder.setPositiveButton("아니요",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.setNegativeButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(),"하차가 취소 되었습니다..",Toast.LENGTH_LONG).show();
                        hachaBusstop.setText(null);
                    }
                });
        builder.show();
    }


}
