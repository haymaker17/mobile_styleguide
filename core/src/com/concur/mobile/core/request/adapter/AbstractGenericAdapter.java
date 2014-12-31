package com.concur.mobile.core.request.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.widget.BaseAdapter;

import com.concur.mobile.core.request.util.DateUtil;

/**
 * @author OlivierB
 */
public abstract class AbstractGenericAdapter<T> extends BaseAdapter {

    private final Context context;
    private List<T> listT = null;

    public abstract void updateList(List<T> listT);

    public AbstractGenericAdapter(Context context, List<T> listT) {
        this.context = context;
        if (listT != null)
            this.listT = listT;
        else
            this.listT = new ArrayList<T>();
    }

    public void clearListItems() {
        this.listT.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    };

    @Override
    public int getCount() {
        return (listT != null) ? listT.size() : 0;
    }

    @Override
    public T getItem(int position) {
        return listT.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getDateString(Date date) {
        final Locale loc = context.getResources().getConfiguration().locale;
        return DateUtil.getFormattedDateForLocale(DateUtil.DatePattern.SHORT, (loc != null) ? loc : Locale.US, date);
    }

    public Context getContext() {
        return context;
    }

    public List<T> getList() {
        return listT;
    }
}
