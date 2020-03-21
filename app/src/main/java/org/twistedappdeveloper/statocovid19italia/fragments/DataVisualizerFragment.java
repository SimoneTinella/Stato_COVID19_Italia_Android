package org.twistedappdeveloper.statocovid19italia.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.twistedappdeveloper.statocovid19italia.BarChartActivity;
import org.twistedappdeveloper.statocovid19italia.DataStorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.R;
import org.twistedappdeveloper.statocovid19italia.adapters.DataAdapter;
import org.twistedappdeveloper.statocovid19italia.model.RowData;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getColorByTrendKey;
import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getPositionByTrendKey;
import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getTrendNameByTrendKey;


public class DataVisualizerFragment extends Fragment {
    private static final String DATA_CONTEXT_KEY = "dataContext";
    private static final String CURSORE_KEY = "cursore";

    private static final String IS_CUSTOM_CURSOR = "custom_cursore";

    public static DataVisualizerFragment newInstance(@NonNull String dataContext) {
        DataVisualizerFragment fragment = new DataVisualizerFragment();
        Bundle args = new Bundle();
        args.putString(DATA_CONTEXT_KEY, dataContext);
        fragment.setArguments(args);
        return fragment;
    }

    public static DataVisualizerFragment newInstance(@NonNull String dataContext, int cursore) {
        DataVisualizerFragment fragment = new DataVisualizerFragment();
        Bundle args = new Bundle();
        args.putString(DATA_CONTEXT_KEY, dataContext);
        args.putInt(CURSORE_KEY, cursore);
        args.putBoolean(IS_CUSTOM_CURSOR, true);
        fragment.setArguments(args);
        return fragment;
    }

    private DataVisualizerFragment() {
    }

    private String dataContext;

    private DataAdapter adapter;
    private List<RowData> rowDataList;

    private Button btnAvanti, btnIndietro;
    private TextView txtData;

    private int cursore;

    private DataStorage dataStorage;

    public DataStorage getDataStorage() {
        return dataStorage;
    }

    public int getCursore() {
        return cursore;
    }

    private boolean isDataAvailable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();

        rowDataList = new ArrayList<>();
        dataContext = arguments.getString(DATA_CONTEXT_KEY);

        dataStorage = DataStorage.getIstance().getDataStorageByDataContext(dataContext);
        if (dataStorage.getDataLength() > 0) {
            int maxCursorValue = dataStorage.getDataLength() - 1;
            if (arguments.getBoolean(IS_CUSTOM_CURSOR, false)) {
                int customCursor= arguments.getInt(CURSORE_KEY);
                cursore = Math.min(customCursor, maxCursorValue);
            } else {
                cursore = maxCursorValue;
            }
            isDataAvailable = true;
        } else {
            isDataAvailable = false;
        }

    }

    private void displayData() {
        rowDataList.clear();

        for (TrendInfo trendInfo : dataStorage.getTrendsList()) {
            rowDataList.add(new RowData(
                    trendInfo.getName(),
                    String.format("%s", trendInfo.getTrendValueByIndex(cursore).getValue()),
                    getColorByTrendKey(getContext(), trendInfo.getKey()),
                    getPositionByTrendKey(trendInfo.getKey()),
                    trendInfo.getKey()
            ));
        }
        txtData.setText(String.format("Relativo al %s", dataStorage.getFullDateByIndex(cursore)));
        Collections.sort(rowDataList);
        adapter.notifyDataSetChanged();
        btnEnableStatusCheck();
    }

    private void btnEnableStatusCheck() {
        if (cursore > 0) {
            btnIndietro.setEnabled(true);
            btnIndietro.setTextColor(Color.WHITE);
        } else {
            btnIndietro.setEnabled(false);
            btnIndietro.setTextColor(Color.DKGRAY);
        }

        if (cursore < dataStorage.getDataLength() - 1) {
            btnAvanti.setEnabled(true);
            btnAvanti.setTextColor(Color.WHITE);
        } else {
            btnAvanti.setEnabled(false);
            btnAvanti.setTextColor(Color.DKGRAY);
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnAvanti:
                    if (cursore < dataStorage.getDataLength()) {
                        cursore++;
                    }
                    break;
                case R.id.btnIndietro:
                    if (cursore > 0) {
                        cursore--;
                    }
                    break;
            }
            displayData();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_data_visualizer, container, false);

        TextView txtTitle = root.findViewById(R.id.txtTitle);
        txtTitle.setText(String.format("Andamento %s", dataContext));

        txtData = root.findViewById(R.id.txtName);
        btnAvanti = root.findViewById(R.id.btnAvanti);
        btnIndietro = root.findViewById(R.id.btnIndietro);

        ListView listView = root.findViewById(R.id.listView);
        adapter = new DataAdapter(getContext(), R.layout.list_data, rowDataList);
        listView.setAdapter(adapter);
        listView.setEmptyView(root.findViewById(R.id.txtEmpty));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String key = rowDataList.get(position).getKey();

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getTrendNameByTrendKey(key));
                builder.setMessage(String.format("Vuoi vedere il confronto tra Regioni relativo al %s?", dataStorage.getFullDateByIndex(cursore)));
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent barChartActivity = new Intent(getContext(), BarChartActivity.class);
                        barChartActivity.putExtra(Utils.TREND_KEY, key);
                        barChartActivity.putExtra(Utils.CURSORE_KEY, cursore);
                        startActivity(barChartActivity);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        btnIndietro.setOnClickListener(listener);
        btnAvanti.setOnClickListener(listener);

        btnAvanti.setEnabled(false);
        btnIndietro.setEnabled(false);

        if (isDataAvailable) {
            displayData();
        }

        return root;
    }
}
