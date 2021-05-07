package org.techtown.gwangjubus.ui.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

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
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.gwangjubus.action.BusArriveAdapter;
import org.techtown.gwangjubus.data.BusArriveImf;
import org.techtown.gwangjubus.MainActivity;
import org.techtown.gwangjubus.action.OnBusArriveClickListener;
import org.techtown.gwangjubus.R;
import org.techtown.gwangjubus.data.NetworkVariable;
import org.techtown.gwangjubus.action.ZxingActivity;
import org.techtown.gwangjubus.ui.location.LocationFragment;

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
HomeFragment : QR 코드로부터 버스 정류장 정보를 전달받아,
                해당 정류장의 버스 도착 정보를 알려주고,
                원하는 버스로 승차 요청을 보냄
 */

public class HomeFragment extends Fragment {

    Context context;
    public static final String TAG = MainActivity.class.getSimpleName();
    // API 인증키
    String key = "bSeWawCaoDQIh9pHcqEVx3Q1BiCDyxhCdoJ4CiXqip2TY3zLfTxJSyjZTyZ%2BIFXmwPbFnkiokLjqo0EI0NDRyw%3D%3D";
    IntentResult result;
    
    // 버스 도착 정보 저장 변수
    ArrayList<BusArriveImf> list = null;
    BusArriveImf bus = null;
    BusArriveAdapter adapter;
    
    // 소켓 통신 변수
    private Socket socket;
    BufferedReader socket_in;
    PrintWriter socket_out;
    
    // 버스 도착 정보 리사이클뷰
    RecyclerView recyclerView;

    // 승차 요청 후 이동할 Fragment
    LocationFragment locationFragment = new LocationFragment();

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }
    private DecoratedBarcodeView barcodeScannerView;

    TextView text;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity().getBaseContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(HomeFragment.this);
        
        // QR 코드 관련 기능 설정
        integrator.setOrientationLocked(false);
        integrator.setPrompt("Scan QR code");
        integrator.setBeepEnabled(false);

        integrator.setCaptureActivity(ZxingActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.initiateScan();

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // 버스 도착 정보를 출력할 리사이클뷰
        recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                System.out.println("QR 스캔 취소함");
                Toast.makeText(getActivity(), "QR 스캔 취소함", Toast.LENGTH_LONG).show();
            } else {
                // QR 코드 스캔된 경우
                System.out.println("Worked: " + result.getContents());
                Toast.makeText(getActivity(), "scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                String busstopDataQR = result.getContents();
                BusArriveTask(busstopDataQR); // QR 코드의 버스정류장 ID로 도착 예정 버스 출력
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // 버스 도착 정보를 수집하는 함수
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
        
        // 버스 도착 정보를 저장할 객체 선언
        list = new ArrayList<BusArriveImf>();
        
        XmlToJson xmlToJson = new XmlToJson.Builder(xml).build();
        JSONObject jsonObject = xmlToJson.toJson();
        Log.d(TAG, "jsonObject:"+jsonObject);

        try {
            JSONObject response = jsonObject.getJSONObject("ns2:ARRIVE_INFO");
            JSONObject result = response.getJSONObject("RESULT");
            

            String resultCode = result.optString("RESULT_CODE");
            Log.d(TAG, "String resultCode :"+resultCode);
            
            // API 검색에 성공한 경우
            if(resultCode.equals("SUCCESS")){

                JSONObject arrive_list = response.getJSONObject("ARRIVE_LIST");
                JSONArray array = arrive_list.getJSONArray("ARRIVE");

                for(int i=0; i < array.length(); i++){
                    JSONObject obj = array.getJSONObject(i);
                    String busId =obj.optString("BUS_ID"); // 버스 ID
                    String busName = obj.optString("LINE_NAME"); // 노선 이름
                    String lineId = obj.optString("LINE_ID"); // 노선 ID
                    String busArriveTime = obj.optString("REMAIN_MIN"); // 도착 예정 시간
                    String busstopName =obj.optString("BUSSTOP_NAME"); // 현재 버스가 있는 정류장

                    Log.d(TAG, "jString busId :"+busId);
                    Log.d(TAG, "jString busName :"+busName);
                    Log.d(TAG, "jString Lineid :"+lineId);
                    Log.d(TAG, "jString busArriveTime :"+busArriveTime);
                    Log.d(TAG, "jString busstopName :"+busstopName);

                    // 버스 도착 정보를 저장
                    bus = new BusArriveImf(busId, busName, lineId, busArriveTime, busstopName);
                    list.add(bus);
                }
                
            } else if(resultCode.equals("ERROR")){
                Toast.makeText(context, "시스템 에러가 발생하였습니다", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        // 리사이클뷰에 버스 도착 정보가 저장된 데이터 표현
        adapter = new BusArriveAdapter(getActivity().getApplicationContext(), list);
        recyclerView.setAdapter(adapter);
        recyclerView.setClickable(true);

        // 승차벨 이벤트
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
        fragmentTransaction.replace(R.id.fragment_home, fragment);
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

                        // 현재 위치를 보여주는 Fragment로 이동
                        ((MainActivity)getActivity()).replaceFragment(locationFragment);
                    }
                });
        builder.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if(socket != null) {
                socket.close();
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

}