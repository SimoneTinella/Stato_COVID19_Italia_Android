package org.twistedappdeveloper.statocovid19italia.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.twistedappdeveloper.statocovid19italia.R;
import org.twistedappdeveloper.statocovid19italia.model.Data;

import java.util.List;

public class DataAdapter extends ArrayAdapter<Data> {

    public DataAdapter(Context context, int resource, List<Data> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_data, null);
        TextView name = convertView.findViewById(R.id.txtName);
        TextView value = convertView.findViewById(R.id.txtValue);
        Data obj = getItem(position);
        name.setText(obj.getName());
        value.setText(obj.getValue());
        value.setTextColor(Color.parseColor(obj.getColor()));
        return convertView;
    }
}
