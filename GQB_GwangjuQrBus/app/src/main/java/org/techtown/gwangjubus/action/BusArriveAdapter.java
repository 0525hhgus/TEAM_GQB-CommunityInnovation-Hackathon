package org.techtown.gwangjubus.action;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.gwangjubus.data.BusArriveImf;
import org.techtown.gwangjubus.R;

import java.util.ArrayList;

// 리사이클뷰의 버스 도착 정보 연동

public class BusArriveAdapter extends RecyclerView.Adapter<BusArriveAdapter.MyViewHolder>
        implements OnBusArriveClickListener {

    private ArrayList<BusArriveImf> mList;
    private LayoutInflater mInflate;
    private Context mContext;

    OnBusArriveClickListener listener;

    public BusArriveAdapter(Context context, ArrayList<BusArriveImf> itmes) {
        this.mList = itmes;
        this.mInflate = LayoutInflater.from(context);
        this.mContext = context;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflate.inflate(R.layout.bus_arrive_imformation, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view, listener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //binding
        holder.busId.setText(mList.get(position).getBusId());
        holder.busName.setText(mList.get(position).getBusName());
        holder.busArriveTime.setText(mList.get(position).getBusArriveTime());
        holder.busstopName.setText(mList.get(position).getBusstopName());

        //Click event
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    @Override
    public void onItemClick(MyViewHolder holder, View view, int position) {
        if(listener != null){ listener.onItemClick(holder,view,position); }
    }

    public void setOnItemClicklistener(OnBusArriveClickListener listener){
        this.listener = listener;
    }


    //ViewHolder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView busId;
        public TextView busName;
        public TextView busArriveTime;
        public TextView busstopName;

        public MyViewHolder(View itemView, final OnBusArriveClickListener listener) {
            super(itemView);

            busId = itemView.findViewById(R.id.busId);
            busName = itemView.findViewById(R.id.busName);
            busArriveTime = itemView.findViewById(R.id.busArriveTime);
            busstopName = itemView.findViewById(R.id.busstopName);

            // recylerview 클릭 시
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null) {
                        listener.onItemClick(MyViewHolder.this, v, position);
                    }

                }
            });
        }

    }
    public BusArriveImf getItem(int position){
        return mList.get(position);
    }

    public void addItem(BusArriveImf item){ mList.add(item); }

    public void setItems(ArrayList<BusArriveImf> items){ this.mList = items; }


    public void setItem(int position, BusArriveImf item) {
        mList.set(position, item);
    }


}