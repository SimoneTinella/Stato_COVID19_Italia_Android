package org.twistedappdeveloper.statocovid19italia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import org.twistedappdeveloper.statocovid19italia.R;
import org.twistedappdeveloper.statocovid19italia.model.TrendsSelection;

import java.util.List;

public class TrendsAdapter extends ArrayAdapter<TrendsSelection> {

    public TrendsAdapter(Context context, int resource, List<TrendsSelection> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_trends, null);
        TrendsSelection obj = getItem(position);
        CheckedTextView checkedTextView = convertView.findViewById(R.id.chkTextView);
        checkedTextView.setText(obj.getTrendInfo().getName());
        checkedTextView.setChecked(obj.isSelected());
        return convertView;
    }
}
