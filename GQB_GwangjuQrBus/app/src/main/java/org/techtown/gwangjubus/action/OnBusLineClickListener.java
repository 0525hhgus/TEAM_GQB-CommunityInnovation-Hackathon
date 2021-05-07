package org.techtown.gwangjubus.action;

import android.view.View;

public interface OnBusLineClickListener {
    public void onItemClick(BusLineAdapter.LineViewHolder holder, View view, int position);
}
