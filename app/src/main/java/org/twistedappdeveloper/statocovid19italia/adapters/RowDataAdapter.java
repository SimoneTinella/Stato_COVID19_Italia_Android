package org.twistedappdeveloper.statocovid19italia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.twistedappdeveloper.statocovid19italia.R;
import org.twistedappdeveloper.statocovid19italia.model.RowData;

import java.util.List;

public class RowDataAdapter extends ArrayAdapter<RowData> {

    private int resource;

    public RowDataAdapter(Context context, int resource, List<RowData> objects) {
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

        List<RowData> subItems = obj.getSubItems();
        //Nel caso in cui non si superino i 2 elementi siamo nei casi di P.A Trento/Bolzano o Valle d'Aosta. Evito di visualizzare i dati perché ridondanti
        if (!subItems.isEmpty() && subItems.size() > 2) {
            LinearLayout linearLayout = convertView.findViewById(R.id.subItems);
            for (RowData rowData : subItems) {
                View child = inflater.inflate(R.layout.list_sub_data, null);
                TextView txtName = child.findViewById(R.id.txtName);
                txtName.setText(rowData.getName());
                TextView txtValue = child.findViewById(R.id.txtValue);
                txtValue.setText(rowData.getValue());
                txtValue.setTextColor(rowData.getColor());
                linearLayout.addView(child);
            }
        }

        return convertView;
    }
}
