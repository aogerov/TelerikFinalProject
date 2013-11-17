package com.gercho.findmybuddies.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.gercho.findmybuddies.models.RequestModel;

/**
 * Created by Gercho on 11/18/13.
 */
public class RequestsArrayAdapter extends ArrayAdapter<RequestModel> {

    private Context mContext;
    private int mTextViewResourceId;
    private RequestModel[] mAllRequests;

    public RequestsArrayAdapter(Context context, int textViewResourceId, RequestModel[] allRequests) {
        super(context, textViewResourceId, allRequests);

        this.mContext = context;
        this.mTextViewResourceId = textViewResourceId;
        this.mAllRequests = allRequests;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RequestHolder requestHolder;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(this.mTextViewResourceId, parent, false);

            requestHolder = new RequestHolder(row);
            if (row != null) {
                row.setTag(requestHolder);
            }
        } else {
            requestHolder = (RequestHolder) row.getTag();
        }

        RequestModel request = this.mAllRequests[position];
        requestHolder.setValues(request);
        return row;
    }
}
