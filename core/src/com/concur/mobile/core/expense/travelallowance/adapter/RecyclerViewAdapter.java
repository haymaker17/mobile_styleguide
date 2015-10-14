package com.concur.mobile.core.expense.travelallowance.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by D049515 on 13.10.2015.
 */
public abstract class RecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    public interface OnClickListener {
        void onClick(View v, int position);
    }

}
