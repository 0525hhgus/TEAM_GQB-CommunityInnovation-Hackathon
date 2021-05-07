package org.techtown.gwangjubus.ui.busstop;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import org.techtown.gwangjubus.MainActivity;
import org.techtown.gwangjubus.action.OnStationClickListener;
import org.techtown.gwangjubus.R;
import org.techtown.gwangjubus.action.StationAdapter;
import org.techtown.gwangjubus.data.StationList;

import java.util.ArrayList;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

/*
BusstopFragment : 버스 정류장 검색,
                    검색한 버스 정류장의 도착 정보를 StationFragment에서 출력
 */

public class BusstopFragment extends Fragment {

    Context context;
    public static final String TAG = "BusstopFragment";
    String key = "bSeWawCaoDQIh9pHcqEVx3Q1BiCDyxhCdoJ4CiXqip2TY3zLfTxJSyjZTyZ%2BIFXmwPbFnkiokLjqo0EI0NDRyw%3D%3D"; // API 인증키

    RecyclerView stationRecylerView;

    ArrayList<StationList> list = null;
    StationList station = null;
    StationAdapter adapter;

    EditText searchbusstoptext; // 검색할 버스 정류장
    Button searchButton; // 검색 버튼
    String search;

    StationFragment stationFragment = new StationFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getActivity().getBaseContext();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_busstop, container, false);


        searchbusstoptext = (EditText) root.findViewById(R.id.searchbusstoptext); // 검색할 버스 정류장
        searchButton = (Button) root.findViewById(R.id.searchbusstopbutton); // 검색 버튼

        // 검색 버튼 클릭시
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                search = searchbusstoptext.getText().toString();
                StationSearchTask();
            }
        });

        stationRecylerView = (RecyclerView) root.findViewById(R.id.busstopsearchrecyclerview);
        stationRecylerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        stationRecylerView.setLayoutManager(layoutManager);

        return root;
    }

    // 버스 정류장을 조회하는 함수
    private void StationSearchTask(){

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        // 버스 정류장 조회
        String url = "http://api.gwangju.go.kr/xml/stationInfo?serviceKey="+key+"";
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

        list = new ArrayList<StationList>();

        XmlToJson xmlToJson = new XmlToJson.Builder(xml).build();
        JSONObject jsonObject = xmlToJson.toJson();
        Log.d(TAG, "jsonObject:"+jsonObject);



        try {
            JSONObject response = jsonObject.getJSONObject("ns2:STATION_INFO");
            JSONObject result = response.getJSONObject("RESULT");

            String resultCode = result.optString("RESULT_CODE");
            Log.d(TAG, "String resultCode :"+resultCode);

            String updown = "상행"; // 버스 노선 상행 하행 여부

            if(resultCode.equals("SUCCESS")){

                JSONObject arrive_list = response.getJSONObject("STATION_LIST");
                JSONArray array = arrive_list.getJSONArray("STATION");

                for(int i=0; i < array.length(); i++){
                    JSONObject obj = array.getJSONObject(i);
                    String staionId =obj.optString("BUSSTOP_ID"); // 첫번째 차량 번호
                    String stationName = obj.optString("BUSSTOP_NAME"); // 버스 이름
                    Log.d(TAG, "jString busId :"+staionId);
                    Log.d(TAG, "jString busName :"+stationName);
                    station = new StationList(staionId, stationName, updown);
                    
                    if(search.equals(stationName)){
                        list.add(station);
                        updown = "하행";
                    }


                }


            } else if(resultCode.equals("ERROR")){
                Toast.makeText(context, "시스템 에러가 발생하였습니다", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 버스 정류장 리사이클뷰로 표현
        adapter = new StationAdapter(getActivity().getApplicationContext(), list);
        stationRecylerView.setAdapter(adapter);
        stationRecylerView.setClickable(true);

        // 검색된 버스 정류장 클릭시
        adapter.setOnItemClicklistener(new OnStationClickListener() {
            @Override
            public void onItemClick(StationAdapter.StationViewHolder holder, View view, int position) {
                StationList item = adapter.getItem(position);
                System.out.println("아이템 선택 " + item.getBusstopName());
                ((MainActivity)getActivity()).busstopId = item.getBusstopId();

                // 버스 정류장 도착 정보를 알려주는 Fragment로 이동
                replaceFragment(stationFragment);
            }
        });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_busstop, fragment);
        fragmentTransaction.commit();
    }



}
