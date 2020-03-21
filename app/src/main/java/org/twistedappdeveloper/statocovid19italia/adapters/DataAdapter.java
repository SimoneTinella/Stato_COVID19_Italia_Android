package org.twistedappdeveloper.statocovid19italia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.twistedappdeveloper.statocovid19italia.R;
import org.twistedappdeveloper.statocovid19italia.model.RowData;

import java.util.List;

public class DataAdapter extends ArrayAdapter<RowData> {

    private int resource;

    public DataAdapter(Context context, int resource, List<RowData> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resource, null);
        TextView name = convertView.findViewById(R.id.txtName);
        TextView value = convertView.findViewById(R.id.txtValue);
        RowData obj = getItem(position);
        name.setText(obj.getName());
        value.setText(obj.getValue());
        value.setTextColor(obj.getColor());
        return convertView;
    }
}
