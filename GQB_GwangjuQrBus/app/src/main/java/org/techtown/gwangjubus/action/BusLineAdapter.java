package org.techtown.gwangjubus.action;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gwangjubus.MainActivity;
import org.techtown.gwangjubus.R;

import java.util.ArrayList;

// 리사이클뷰의 버스 노선 정보 연동

public class BusLineAdapter extends RecyclerView.Adapter<BusLineAdapter.LineViewHolder>
        implements OnBusLineClickListener {

    private String busstopName;
    private ArrayList<String> mList;
    private LayoutInflater mInflate;
    private Context mContext;

    OnBusLineClickListener listener;

    public BusLineAdapter(Context context, ArrayList<String> itmes) {
        this.busstopName = MainActivity.busstopName;
        this.mList = itmes;
        this.mInflate = LayoutInflater.from(context);
        this.mContext = context;
    }


    @NonNull
    @Override
    public LineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflate.inflate(R.layout.bus_line_imformation, parent, false);
        LineViewHolder viewHolder = new LineViewHolder(view, listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull LineViewHolder holder, int position) {
        //binding
        holder.busLine.setText(mList.get(position));
        //Click event
        if (mList.get(position).equals(busstopName)) {
            holder.busImage.setVisibility(View.VISIBLE);
        }
        else{
            holder.busImage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }



    public void setOnItemClicklistener(OnBusLineClickListener listener){
        this.listener = listener;
    }

    @Override
    public void onItemClick(LineViewHolder holder, View view, int position) {
        //if(listener != null){ listener.onItemClick(holder,view,position);
    }


    //ViewHolder
    public static class LineViewHolder extends RecyclerView.ViewHolder {
        public TextView busLine;
        public ImageView busImage;

        public LineViewHolder(View itemView, final OnBusLineClickListener listener) {
            super(itemView);

            busLine = itemView.findViewById(R.id.busstopshow);
            busImage = itemView.findViewById(R.id.busimage);


            // recylerview 클릭 시
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null) {
                        //listener.onItemClick(LineViewHolder.this, v, position);
                    }

                }
            });
        }

    }
    public String getItem(int position){
        return mList.get(position);
    }

    public void addItem(String item){ mList.add(item); }

    public void setItems(ArrayList<String> items){ this.mList = items; }


    public void setItem(int position, String item) {
        mList.set(position, item);
    }


}
