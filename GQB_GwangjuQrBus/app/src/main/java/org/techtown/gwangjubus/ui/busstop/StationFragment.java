package org.techtown.gwangjubus.ui.busstop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.gwangjubus.action.BusArriveAdapter;
import org.techtown.gwangjubus.data.BusArriveImf;
import org.techtown.gwangjubus.MainActivity;
import org.techtown.gwangjubus.action.OnBusArriveClickListener;
import org.techtown.gwangjubus.R;
import org.techtown.gwangjubus.data.NetworkVariable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

/*
StationFragment : 검색한 버스 정류장의 도착 정보를 출력
 */

public class StationFragment extends Fragment {

    Context context;
    public static final String TAG = "StationFragment";
    String key = "bSeWawCaoDQIh9pHcqEVx3Q1BiCDyxhCdoJ4CiXqip2TY3zLfTxJSyjZTyZ%2BIFXmwPbFnkiokLjqo0EI0NDRyw%3D%3D";

    RecyclerView recyclerView;

    ArrayList<BusArriveImf> list = null;
    BusArriveImf bus = null;
    BusArriveAdapter adapter;

    String search;

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
        View root = inflater.inflate(R.layout.fragment_station, container, false);

        // 버스 도착 정보 리사이클뷰
        recyclerView = (RecyclerView) root.findViewById(R.id.stationrecycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        search = ((MainActivity)getActivity()).busstopId;
        BusArriveTask(search);

        return root;
    }

    // 버스 도착 정보를 수집하는 함수 (HomeFragment의 버스 도착 정보 수집 함수와 동일)
    private void BusArriveTask(String search){

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String StationId = null; // 정류소 ID

        try {
            StationId = URLEncoder.encode(search,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 버스 도착정보 목록 조회
        String url = "http://api.gwangju.go.kr/xml/arriveInfo?serviceKey="+key+"&BUSSTOP_ID="+StationId+"";
        Log.d(TAG, "URL:"+url);

        StringRequest request= new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        XMLtoJSONData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue.add(request);
    }

    // XML 데이터를 JSON 데이터로 변환하여 저장하는 함수
    private void XMLtoJSONData(String xml){

        list = new ArrayList<BusArriveImf>();

        XmlToJson xmlToJson = new XmlToJson.Builder(xml).build();
        JSONObject jsonObject = xmlToJson.toJson();
        Log.d(TAG, "jsonObject:"+jsonObject);

        try {
            JSONObject response = jsonObject.getJSONObject("ns2:ARRIVE_INFO");
            JSONObject result = response.getJSONObject("RESULT");

            String resultCode = result.optString("RESULT_CODE");
            Log.d(TAG, "String resultCode :"+resultCode);

            if(resultCode.equals("SUCCESS")){

                JSONObject arrive_list = response.getJSONObject("ARRIVE_LIST");
                JSONArray array = arrive_list.getJSONArray("ARRIVE");

                for(int i=0; i < array.length(); i++){
                    JSONObject obj = array.getJSONObject(i);
                    String busId =obj.optString("BUS_ID");
                    String busName = obj.optString("LINE_NAME");
                    String lineId = obj.optString("LINE_ID");
                    String busArriveTime = obj.optString("REMAIN_MIN");
                    String busstopName =obj.optString("BUSSTOP_NAME");
                    Log.d(TAG, "jString busId :"+busId);
                    Log.d(TAG, "jString busName :"+busName);
                    Log.d(TAG, "jString Lineid :"+lineId);
                    Log.d(TAG, "jString busArriveTime :"+busArriveTime);
                    Log.d(TAG, "jString busstopName :"+busstopName);

                    bus = new BusArriveImf(busId, busName, lineId, busArriveTime, busstopName);

                    list.add(bus);
                }

            } else if(resultCode.equals("ERROR")){
                Toast.makeText(context, "시스템 에러가 발생하였습니다", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter = new BusArriveAdapter(getActivity().getApplicationContext(), list);
        recyclerView.setAdapter(adapter);
        recyclerView.setClickable(true);
        adapter.setOnItemClicklistener(new OnBusArriveClickListener() {
            @Override
            public void onItemClick(BusArriveAdapter.MyViewHolder holder, View view, int position) {
                BusArriveImf item = adapter.getItem(position);
                System.out.println("아이템 선택 " + item.getBusName());
                show(item);
            }
        });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_station, fragment);
        fragmentTransaction.commit();
    }

    // 승차벨 이벤트 함수
    void show(BusArriveImf item)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("승차 요청");
        builder.setMessage("승차하시겠습니까?");
        builder.setPositiveButton("아니요",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        // 승차 요청 시
        builder.setNegativeButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(),"예를 선택했습니다.",Toast.LENGTH_LONG).show();

                        // 현재 버스 관련 정보 저장
                        ((MainActivity)getActivity()).busId = item.getBusId();
                        ((MainActivity)getActivity()).lineId = item.getLineId();
                        ((MainActivity)getActivity()).busstopName = item.getBusstopName();
                        ((MainActivity)getActivity()).lineName = item.getBusName();

                        // 승차 요청한 버스 ID를 소켓 통신으로 서버에 전달
                        String busid = item.getBusId();
                        Thread worker = new Thread(){
                            public void run(){
                                try {
                                    socket = new Socket("168.131.151.207", NetworkVariable.takeOn);
                                    socket_out = new PrintWriter(socket.getOutputStream(), true);
                                    socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                    if (busid != null){
                                        socket_out.println(busid);
                                    }

                                } catch (IOException e){
                                    e.printStackTrace();
                                }
                                try {
                                    while(true) {
                                        if (((MainActivity)getActivity()).busId != null){
                                            ((MainActivity)getActivity()).busId = socket_in.readLine();
                                        }

                                    }
                                } catch (Exception e){
                                    e.printStackTrace();
                                }

                            }
                        };

                        worker.start();
                    }
                });
        builder.show();
    }


}
