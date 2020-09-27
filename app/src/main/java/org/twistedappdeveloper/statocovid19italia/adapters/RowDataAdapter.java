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
import org.twistedappdeveloper.statocovid19italia.datastorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.model.RowData;

import java.util.List;
import java.util.Locale;

import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.formatNumber;

public class RowDataAdapter extends ArrayAdapter<RowData> {

    private int resource;
    private boolean displayInfo;

    public RowDataAdapter(Context context, int resource, List<RowData> objects) {
        super(context, resource, objects);
        this.resource = resource;
        displayInfo = false;
    }

    public RowDataAdapter(Context context, int resource, List<RowData> objects, boolean displayInfo) {
        super(context, resource, objects);
        this.resource = resource;
        this.displayInfo = displayInfo;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resource, null);
        TextView name = convertView.findViewById(R.id.txtName);
        TextView value = convertView.findViewById(R.id.txtValue);

        RowData obj = getItem(position);
        name.setText(obj.getName());
        value.setText(formatNumber(obj.getValue()));
        if (obj.getKey().equals(DataStorage.C_RAPPORTO_POSITIVI_TAMPONI)) {
            value.setText(String.format("%s %%", value.getText()));
        }
        value.setTextColor(obj.getColor());

        View infoContainer = convertView.findViewById(R.id.infoContainer);
        if (displayInfo && !obj.getKey().equals(DataStorage.C_RAPPORTO_POSITIVI_TAMPONI)) {
            infoContainer.setVisibility(View.VISIBLE);
            TextView txtInfoPercentage = convertView.findViewById(R.id.txtInfoPercentage);
            TextView txtInfoValuePercentage = convertView.findViewById(R.id.txtInfoValuePercentage);
            TextView txtInfoDelta = convertView.findViewById(R.id.txtInfoDelta);
            TextView txtInfoValueDelta = convertView.findViewById(R.id.txtInfoValueDelta);
            TextView txtInfoPrec = convertView.findViewById(R.id.txtInfoPrec);
            TextView txtInfoValuePrec = convertView.findViewById(R.id.txtInfoValuePrec);
            txtInfoPrec.setText(R.string.val_giorno_prima);
            txtInfoPercentage.setText(R.string.var_perc_giorno_prima);
            txtInfoDelta.setText(R.string.var_giorno_prima);
            if (obj.getDeltaPercentage() >= 0f) {
                if (obj.getDelta() == (int) obj.getDelta()) {
                    txtInfoValueDelta.setText(String.format(Locale.ITALIAN, "+%d", (int) obj.getDelta()));
                } else {
                    txtInfoValueDelta.setText(String.format(Locale.ITALIAN, "+%.1f", obj.getDelta()));
                }
                txtInfoValuePercentage.setText(String.format(Locale.ITALIAN, "+%.2f %%", obj.getDeltaPercentage() * 100));
            } else {
                txtInfoValuePercentage.setText(String.format(Locale.ITALIAN, "%.2f %%", obj.getDeltaPercentage() * 100));
                txtInfoValueDelta.setText(formatNumber(obj.getDelta()));
            }
            txtInfoValuePrec.setText(formatNumber(obj.getPrecValue()));

            txtInfoValuePercentage.setTextColor(obj.getColor());
            txtInfoValueDelta.setTextColor(obj.getColor());
            txtInfoValuePrec.setTextColor(obj.getColor());

            List<RowData> subItems = obj.getSubItems();
            //Nel caso in cui non si superano i 2 elementi siamo nei casi di P.A Trento/Bolzano o Valle d'Aosta. Evito di visualizzare i dati perché ridondanti
            if (!subItems.isEmpty() && subItems.size() > 2) {
                LinearLayout linearLayout = convertView.findViewById(R.id.subItems);
                for (RowData rowData : subItems) {
                    View child = inflater.inflate(R.layout.list_sub_data, null);
                    TextView txtName = child.findViewById(R.id.txtName);
                    txtName.setText(rowData.getName());
                    TextView txtValue = child.findViewById(R.id.txtValue);
                    txtValue.setText(formatNumber(rowData.getValue()));
                    txtValue.setTextColor(rowData.getColor());
                    linearLayout.addView(child);
                }
            }
        } else {
            infoContainer.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void setDisplayInfo(boolean displayInfo) {
        this.displayInfo = displayInfo;
    }

}
