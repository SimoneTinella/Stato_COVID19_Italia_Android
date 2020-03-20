package org.twistedappdeveloper.statocovid19italia;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.twistedappdeveloper.statocovid19italia.DataStorage.DataStorage;
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
import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getTrendNameByTrendKey;

public class MainActivity extends AppCompatActivity {

    private TextView txtData;

    private DataAdapter adapter;

    private ProgressDialog pdialog;

    private List<Data> dataList;

    private Button btnAvanti, btnIndietro;

    private int cursore;

    private DataStorage dataStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataStorage = DataStorage.getIstance();
        dataList = new ArrayList<>();

        txtData = findViewById(R.id.txtName);
        btnAvanti = findViewById(R.id.btnAvanti);
        btnIndietro = findViewById(R.id.btnIndietro);

        ListView listView = findViewById(R.id.listView);
        adapter = new DataAdapter(getApplicationContext(), R.layout.list_data, dataList);
        listView.setAdapter(adapter);

        listView.setEmptyView(findViewById(R.id.txtEmpty));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String key = dataList.get(position).getKey();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(getTrendNameByTrendKey(key));
                builder.setMessage(String.format("Vuoi vedere il confronto tra Regioni relativo al %s?", dataStorage.getFullDateByIndex(cursore)));
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent barChartActivity = new Intent(getApplicationContext(), BarChartActivity.class);
                        barChartActivity.putExtra("trendKey", key);
                        barChartActivity.putExtra("cursore", cursore);
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
                if (dataStorage.getDataLength() > 0) {
                    Intent chartActivity = new Intent(getApplicationContext(), ChartActivity.class);
                    chartActivity.putExtra("contesto", dataStorage.getDataContext());
                    startActivity(chartActivity);
                } else {
                    Toast.makeText(MainActivity.this, "Non sono presenti dati da graficare, prova ad aggiornare.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_update:
                updateValues();
                break;
            case R.id.action_regional_data:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final String[] regioni = dataStorage.getSubLevelDataKeys().toArray(new String[0]);
                builder.setSingleChoiceItems(regioni, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent regionalActivity = new Intent(getApplicationContext(), RegionalActivity.class);
                        regionalActivity.putExtra("regione", regioni[which]);
                        startActivity(regionalActivity);
                    }
                });
                builder.setTitle("Seleziona Regione");
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.action_confronto_regionale:
                if (dataStorage.getDataLength() > 0) {
                    Intent barChartActivity = new Intent(getApplicationContext(), BarChartActivity.class);
                    barChartActivity.putExtra("cursore", cursore);
                    startActivity(barChartActivity);
                } else {
                    Toast.makeText(MainActivity.this, "Non sono presenti dati da graficare, prova ad aggiornare.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void displayData() {
        dataList.clear();

        for (TrendInfo trendInfo : dataStorage.getTrendsList()) {
            dataList.add(new Data(
                    trendInfo.getName(),
                    String.format("%s", trendInfo.getTrendValueByIndex(cursore).getValue()),
                    getColorByTrendKey(getApplicationContext(), trendInfo.getKey()),
                    getPositionByTrendKey(trendInfo.getKey()),
                    trendInfo.getKey()
            ));
        }
        txtData.setText(String.format("Relativo al %s", dataStorage.getFullDateByIndex(cursore)));
        Collections.sort(dataList);
        adapter.notifyDataSetChanged();
        btnEnableStatusCheck();
    }


    private String formatText(int n) {
        if (n == 1) {
            return String.format("%s versione", n);
        }
        return String.format("%s versioni", n);
    }

    private void checkAppVersion() {
        if (!Utils.isDeviceOnline(MainActivity.this)) {
            return;
        }

        new Thread(new Runnable() {
            SyncHttpClient client = new SyncHttpClient();

            @Override
            public void run() {
                client.get("https://raw.githubusercontent.com/SimoneTinella/Stato_COVID19_Italia_Android/master/notification.json", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            final int latestVersion = response.getInt("latest_app_version");
                            final int currentVersion = BuildConfig.VERSION_CODE;
                            if (latestVersion > currentVersion) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle("Aggiornamento applicazione");
                                        builder.setMessage(String.format("È stata rilasciata una nuova versione dell'appliazione. Sei indietro di %s. Vuoi scaricare l'ultima versione?", formatText(latestVersion - currentVersion)));
                                        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                Intent i = new Intent(Intent.ACTION_VIEW);
                                                i.setData(Uri.parse("https://github.com/SimoneTinella/Stato_COVID19_Italia_Android"));
                                                startActivity(i);
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
                            }else if (latestVersion < currentVersion){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this,"Hai una versione preview dell'App", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    private void updateValues() {
        if (!Utils.isDeviceOnline(MainActivity.this)) {
            Toast.makeText(MainActivity.this, "Il dispositivo non ha accesso ad Internet, attiva la connessione e riprova.", Toast.LENGTH_SHORT).show();
            return;
        }

        pdialog = ProgressDialog.show(MainActivity.this, "", "Attendere prego...", true);
        txtData.setText("In Aggiornamento");

        final Thread threadDatiNazionali = new Thread(new Runnable() {

            SyncHttpClient client = new SyncHttpClient();

            @Override
            public void run() {
                client.get("https://raw.githubusercontent.com/pcm-dpc/COVID-19/master/dati-json/dpc-covid19-ita-andamento-nazionale.json", new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        dataStorage.setDataArrayJson(response);
                        cursore = response.length() - 1;
                    }

                });
            }
        });

        final Thread threadDatiRegionali = new Thread(new Runnable() {

            SyncHttpClient client = new SyncHttpClient();

            @Override
            public void run() {
                client.get("https://raw.githubusercontent.com/pcm-dpc/COVID-19/master/dati-json/dpc-covid19-ita-regioni.json", new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        dataStorage.setSubLvlDataArrayJson(response);
                    }

                });
            }
        });

        threadDatiNazionali.start();
        threadDatiRegionali.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    threadDatiNazionali.join();
                    threadDatiRegionali.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayData();
                            pdialog.dismiss();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        updateValues();
        checkAppVersion();
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

}
