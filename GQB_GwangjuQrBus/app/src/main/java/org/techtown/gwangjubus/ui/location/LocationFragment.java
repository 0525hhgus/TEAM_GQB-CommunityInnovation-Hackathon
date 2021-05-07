package org.techtown.gwangjubus.ui.location;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import org.techtown.gwangjubus.action.BusLineAdapter;
import org.techtown.gwangjubus.MainActivity;
import org.techtown.gwangjubus.action.OnBusLineClickListener;
import org.techtown.gwangjubus.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

/*
LocationFragment : 현재 승차 요청 or 탑승한 버스의 위치를 버스 노선 상에 표현
 */

public class LocationFragment extends Fragment {

    String key = "bSeWawCaoDQIh9pHcqEVx3Q1BiCDyxhCdoJ4CiXqip2TY3zLfTxJSyjZTyZ%2BIFXmwPbFnkiokLjqo0EI0NDRyw%3D%3D";
    private static final String TAG = "BusLocation";
    Context context;
    ArrayList<String> list = null;
    String bus = null;
    BusLineAdapter adapter;

    TextView mybustext;
    RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity().getBaseContext();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_location, container, false);

        mybustext = (TextView) root.findViewById(R.id.mybus_text);
        
        // 노선 정보 리사이클뷰 선언
        recyclerView = (RecyclerView) root.findViewById(R.id.line_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // 현재 승차요청 or 탑승한 노선 ID 전달
        String lineId = ((MainActivity)getActivity()).lineId;

        BusLineTask(lineId);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    // 버스 노선을 불러오고 현재 승차 요청 or 탑승한 버스 위치 출력 함수
    private void BusLineTask(String search){

        // 승차 요청 or 탑승한 버스 없는 경우
        if(search == null)
            return;
        
        RequestQueue requestQueue = Volley.newRequestQueue(context);

        String lineId = null;

        try {
            lineId = URLEncoder.encode(search,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 버스 노선 조회
        String url = "http://api.gwangju.go.kr/xml/lineStationInfo?serviceKey="+key+"&LINE_ID="+lineId+"";
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
    private void XMLtoJSONData(String xml) {

        // 버스 노선을 저장
        list = new ArrayList<String>();

        XmlToJson xmlToJson = new XmlToJson.Builder(xml).build();
        JSONObject jsonObject = xmlToJson.toJson();
        Log.d(TAG, "jsonObject:" + jsonObject);


        int pos = 0; // 노선 중 현재 버스 정류장 위치
        boolean posend = false; // 현재 정류장 위치 발견 여부

        try {
            JSONObject response = jsonObject.getJSONObject("ns2:BUSSTOP_INFO");
            JSONObject result = response.getJSONObject("RESULT");


            String resultCode = result.optString("RESULT_CODE");
            Log.d(TAG, "String resultCode :" + resultCode);

            if (resultCode.equals("SUCCESS")) {

                JSONObject arrive_list = response.getJSONObject("BUSSTOP_LIST");
                JSONArray array = arrive_list.getJSONArray("BUSSTOP");

                String busName = null;


                for (int i = 0; i < array.length() / 2 + 1; i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String lineName = obj.optString("BUSSTOP_NAME"); 
                    busName = obj.optString("LINE_NAME");

                    bus = lineName;

                    // 현재 승차 요청 or 탑승한 버스와 일치하는 경우 해당 위치를 저장
                    if(lineName.equals(((MainActivity)getActivity()).busstopName)){
                        posend = true;
                    } else if (!posend){
                        pos = pos + 1;
                    }

                    list.add(bus);

                }

                // 현재 탑승 버스 이름 출력
                mybustext.setText(busName);

            } else if (resultCode.equals("ERROR")) {
                Toast.makeText(context, "시스템 에러가 발생하였습니다", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 버스 노선 리사이클 뷰 설정
        adapter = new BusLineAdapter(getActivity().getApplicationContext(), list);
        recyclerView.setAdapter(adapter);
        recyclerView.setClickable(true);
        
        recyclerView.scrollToPosition(pos); // 현재 버스가 위치한 정류장에 커서 옮김

        adapter.setOnItemClicklistener(new OnBusLineClickListener() {
            @Override
            public void onItemClick(BusLineAdapter.LineViewHolder holder, View view, int position) {
                String item = adapter.getItem(position);
                System.out.println("아이템 선택 " + item);
            }
        });
    }
}