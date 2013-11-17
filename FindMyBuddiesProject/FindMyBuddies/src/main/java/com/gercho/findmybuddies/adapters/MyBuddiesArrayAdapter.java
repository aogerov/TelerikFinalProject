package com.gercho.findmybuddies.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.gercho.findmybuddies.enums.MeasureUnits;
import com.gercho.findmybuddies.models.BuddyModel;

/**
 * Created by Gercho on 11/16/13.
 */
public class MyBuddiesArrayAdapter extends ArrayAdapter<BuddyModel> {

    private Context mContext;
    private int mTextViewResourceId;
    private BuddyModel[] mBuddies;
    private MeasureUnits mMeasureUnits;

    public MyBuddiesArrayAdapter(Context context, int textViewResourceId,
                                 BuddyModel[] buddies, MeasureUnits measureUnits) {
        super(context, textViewResourceId, buddies);

        this.mContext = context;
        this.mTextViewResourceId = textViewResourceId;
        this.mBuddies = buddies;
        this.mMeasureUnits = measureUnits;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        BuddiesHolder buddiesHolder;

        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(this.mTextViewResourceId, parent, false);

            buddiesHolder = new BuddiesHolder(row);
            if (row != null) {
                row.setTag(buddiesHolder);
            }
        } else {
            buddiesHolder = (BuddiesHolder) row.getTag();
        }

        BuddyModel buddy = this.mBuddies[position];
        buddiesHolder.setValues(buddy, this.mMeasureUnits);
        return row;
    }
}
