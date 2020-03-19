package org.twistedappdeveloper.statocovid19italia;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.twistedappdeveloper.statocovid19italia.DataStorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.adapters.DataAdapter;
import org.twistedappdeveloper.statocovid19italia.model.Data;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getColorByTrendKey;
import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getPositionByTrendKey;

public class RegionalActivity extends AppCompatActivity {

    private String regione;

    private DataAdapter adapter;
    private List<Data> dataList;

    private Button btnAvanti, btnIndietro;
    private TextView txtData;

    private int cursore;

    private DataStorage dataStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        regione = getIntent().getStringExtra("regione");

        dataStorage = DataStorage.getIstance().getRegionalDataStorageByDenRegione(regione);
        cursore = dataStorage.getMainDataLength() - 1;
        dataList = new ArrayList<>();

        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setText(String.format("Andamento %s", regione));

        txtData = findViewById(R.id.txtName);
        btnAvanti = findViewById(R.id.btnAvanti);
        btnIndietro = findViewById(R.id.btnIndietro);

        ListView listView = findViewById(R.id.listView);
        adapter = new DataAdapter(getApplicationContext(), R.layout.list_data, dataList);
        listView.setAdapter(adapter);

        listView.setEmptyView(findViewById(R.id.txtEmpty));

        btnIndietro.setOnClickListener(listener);
        btnAvanti.setOnClickListener(listener);

        btnAvanti.setEnabled(false);
        btnIndietro.setEnabled(false);

        displayData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.regionalmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_chart:
                if (dataStorage.getMainDataLength() > 0) {
                    Intent chartActivity = new Intent(getApplicationContext(), ChartActivity.class);
                    chartActivity.putExtra("contesto", dataStorage.getContestoDati());
                    startActivity(chartActivity);
                } else {
                    Toast.makeText(RegionalActivity.this, "Non sono presenti dati da graficare, prova ad aggiornare.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_regional_data:
                AlertDialog.Builder builder = new AlertDialog.Builder(RegionalActivity.this);
                final String[] regioni = DataStorage.getIstance().getSecondaryKeys().toArray(new String[0]);
                int checkedItem = 0;
                for (int i = 0; i < regioni.length; i++) {
                    if (regioni[i].equalsIgnoreCase(regione)) {
                        checkedItem = i;
                        break;
                    }
                }
                builder.setSingleChoiceItems(regioni, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent regionalActivity = new Intent(getApplicationContext(), RegionalActivity.class);
                        regionalActivity.putExtra("regione", regioni[which]);
                        startActivity(regionalActivity);
                        finish();
                    }
                });
                builder.setTitle("Seleziona Regione");
                AlertDialog alert = builder.create();
                alert.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayData() {
        dataList.clear();

        for (TrendInfo trendInfo : dataStorage.getMainTrendsList()) {
            dataList.add(new Data(
                    trendInfo.getName(),
                    String.format("%s", trendInfo.getTrendValues().get(cursore).getValue()),
                    getColorByTrendKey(getApplicationContext(), trendInfo.getKey()),
                    getPositionByTrendKey(trendInfo.getKey())
            ));
        }
        txtData.setText(String.format("Relativo al %s", dataStorage.getDateByIndex(cursore)));
        Collections.sort(dataList);
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

        if (cursore < dataStorage.getMainDataLength() - 1) {
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
                    if (cursore < dataStorage.getMainDataLength()) {
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
}
