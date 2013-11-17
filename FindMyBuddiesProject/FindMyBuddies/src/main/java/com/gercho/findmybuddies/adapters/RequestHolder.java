package com.gercho.findmybuddies.adapters;

import android.view.View;
import android.widget.TextView;

import com.gercho.findmybuddies.R;
import com.gercho.findmybuddies.models.RequestModel;

/**
 * Created by Gercho on 11/18/13.
 */
public class RequestHolder {

    private TextView mNickname;

    public RequestHolder (View row) {
        this.mNickname = (TextView) row.findViewById(R.id.textView_requestFrom);
    }

    public void setValues(RequestModel request) {
        String text = request.getFromUserNickname();
        if (!request.isShowed()) {
            text += "  * new";
        }

        this.mNickname.setText(text);
    }
}
