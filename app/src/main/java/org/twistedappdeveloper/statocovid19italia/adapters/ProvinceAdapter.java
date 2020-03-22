package org.twistedappdeveloper.statocovid19italia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.twistedappdeveloper.statocovid19italia.R;
import org.twistedappdeveloper.statocovid19italia.model.ProvinceSelection;
import org.twistedappdeveloper.statocovid19italia.model.ProvinceSelectionWrapper;

import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class ProvinceAdapter extends ArrayAdapter<ProvinceSelectionWrapper> {

    private TextView txtTitle;
    private List<ProvinceSelectionWrapper> objects;

    public ProvinceAdapter(Context context, int resource, List<ProvinceSelectionWrapper> objects, TextView textView) {
        super(context, resource, objects);
        this.txtTitle = textView;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_province, null);


        ProvinceSelectionWrapper obj = getItem(position);
        TextView txtRegione = convertView.findViewById(R.id.txtRegione);
        txtRegione.setText(obj.getRegione());
        List<ProvinceSelection> provinceSelections = obj.getProvinceSelectionList();
        LinearLayout layout = convertView.findViewById(R.id.checkLayout);

        for (final ProvinceSelection provinceSelection : provinceSelections) {
            final CheckBox checkBox = (CheckBox) inflater.inflate(R.layout.custom_checkbox, null);
            checkBox.setText(provinceSelection.getProvincia());
            checkBox.setChecked(provinceSelection.isSelected());
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    provinceSelection.setSelected(checkBox.isChecked());
                    int n = 0;
                    for (ProvinceSelectionWrapper provinceSelectionWrapper: objects) {
                        for (ProvinceSelection provinceSelection : provinceSelectionWrapper.getProvinceSelectionList()) {
                            if (provinceSelection.isSelected()) {
                                n++;
                            }
                        }
                    }
                    txtTitle.setText(String.format("%s (%s sel.)", getContext().getResources().getString(R.string.province_da_visualizzare), n));
                }
            });

            layout.addView(checkBox);
        }
        return convertView;
    }

}
