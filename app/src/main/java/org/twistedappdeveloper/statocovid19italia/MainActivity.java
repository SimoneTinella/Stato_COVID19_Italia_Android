package org.twistedappdeveloper.statocovid19italia;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.twistedappdeveloper.statocovid19italia.DataStorage.NationalDataStorage;
import org.twistedappdeveloper.statocovid19italia.adapters.DataAdapter;
import org.twistedappdeveloper.statocovid19italia.model.Data;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getColorByTrendKey;
import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getPositionByTrendKey;

public class MainActivity extends AppCompatActivity {

    private TextView txtData;

    private DataAdapter adapter;

    private ProgressDialog pdialog;

    private List<Data> dataList;

    private Button btnAvanti, btnIndietro;

    private int cursore;

    private NationalDataStorage nationalDataStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nationalDataStorage = NationalDataStorage.getIstance();
        dataList = new ArrayList<>();

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                final SpannableString s =
                        new SpannableString(getString(R.string.infoMessage));
                Linkify.addLinks(s, Linkify.WEB_URLS);
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle(R.string.info);
                alertDialog.setMessage(s);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                TextView txtDialog = alertDialog.findViewById(android.R.id.message);
                if (txtDialog != null) {
                    txtDialog.setMovementMethod(LinkMovementMethod.getInstance());
                }
                break;
            case R.id.action_chart:
                if (nationalDataStorage.getDatiNazionaliLength() > 0) {
                    Intent chartActivity = new Intent(getApplicationContext(), ChartActivity.class);
                    startActivity(chartActivity);
                } else {
                    Toast.makeText(MainActivity.this, "Non sono presenti dati da graficare, prova ad aggiornare.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_update:
                updateValues();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void displayData() {
        dataList.clear();

        for (TrendInfo trendInfo : nationalDataStorage.getTrendsList()) {
            dataList.add(new Data(
                    trendInfo.getName(),
                    String.format("%s", trendInfo.getTrendValues().get(cursore).getValue()),
                    getColorByTrendKey(getApplicationContext(), trendInfo.getKey()),
                    getPositionByTrendKey(trendInfo.getKey())
            ));
        }
        txtData.setText(String.format("Relativo al %s", nationalDataStorage.getDateByIndex(cursore)));
        Collections.sort(dataList);
        adapter.notifyDataSetChanged();
        btnEnableStatusCheck();
    }

    private void updateValues() {
        if (!Utils.isDeviceOnline(MainActivity.this)) {
            Toast.makeText(MainActivity.this, "Il dispositivo non ha accesso ad Internet, attiva la connessione e riprova.", Toast.LENGTH_SHORT).show();
            return;
        }

        pdialog = ProgressDialog.show(MainActivity.this, "", "Attendere prego...", true);
        txtData.setText("In Aggiornamento");

        new Thread(new Runnable() {

            SyncHttpClient client = new SyncHttpClient();

            @Override
            public void run() {
                client.get("https://raw.githubusercontent.com/pcm-dpc/COVID-19/master/dati-json/dpc-covid19-ita-andamento-nazionale.json", new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.d("AndroTag", "onSuccessJSONArray");

                        nationalDataStorage.setDatiNazionaliJson(response);
                        cursore = response.length() - 1;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                displayData();
                            }
                        });
                        pdialog.dismiss();
                    }

                });
            }
        }).start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        updateValues();
    }

    private void btnEnableStatusCheck() {
        if (cursore > 0) {
            btnIndietro.setEnabled(true);
            btnIndietro.setTextColor(Color.WHITE);
        } else {
            btnIndietro.setEnabled(false);
            btnIndietro.setTextColor(Color.DKGRAY);
        }

        if (cursore < nationalDataStorage.getDatiNazionaliLength() - 1) {
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
                    if (cursore < nationalDataStorage.getDatiNazionaliLength()) {
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
