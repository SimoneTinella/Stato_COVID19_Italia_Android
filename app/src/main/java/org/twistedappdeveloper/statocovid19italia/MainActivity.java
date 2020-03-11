package org.twistedappdeveloper.statocovid19italia;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.twistedappdeveloper.statocovid19italia.adapters.DataAdapter;
import org.twistedappdeveloper.statocovid19italia.model.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cz.msebera.android.httpclient.Header;

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
            case R.id.action_update:
                updateValues();
                break;
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
        }
        return super.onOptionsItemSelected(item);
    }


    private void displayData() {
        dataList.clear();
        try {
            JSONObject obj = dataStorage.getDatiNazionaliJson().getJSONObject(cursore);
            String data = obj.getString("data");
            for (Iterator<String> keys = obj.keys(); keys.hasNext(); ) {
                String key = keys.next();

                if (key.equalsIgnoreCase("data") ||
                        key.equalsIgnoreCase("stato")
                ) {
                    continue;
                }
                String[] strings = key.split("_");
                String name = "";
                for (String string : strings) {
                    name = String.format(
                            "%s %s",
                            name,
                            String.format("%s%s", string.substring(0, 1).toUpperCase(), string.substring(1))
                    );
                }
                String value = obj.getString(key);
                dataList.add(new Data(name.trim(), value, getColorByKey(key), getPositionByKey(key)));
            }

            txtData.setText(String.format("Relativo al %s", data));

            Collections.sort(dataList);
            adapter.notifyDataSetChanged();
            btnEnableStatusCheck();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getColorByKey(String key) {
        switch (key) {

            case "deceduti":
                return "#FF0000";

            case "totale_casi":
                return "#FF8300";

            case "dimessi_guariti":
                return "#00FF00";

            default:
                return "#000000";
        }
    }

    private int getPositionByKey(String key) {
        switch (key) {
            case "totale_casi":
                return 0;
            case "dimessi_guariti":
                return 1;
            case "deceduti":
                return 2;
            case "totale_attualmente_positivi":
                return 3;
            case "nuovi_attualmente_positivi":
                return 4;
            case "totale_ospedalizzati":
                return 5;
            case "terapia_intensiva":
                return 6;
            case "ricoverati_con_sintomi":
                return 7;
            case "isolamento_domiciliare":
                return 8;
            case "tamponi":
                return 9;

            default:
                return Integer.MAX_VALUE;
        }
    }

    private void updateValues() {
        pdialog = ProgressDialog.show(MainActivity.this, "", "Attendere prego...", true);
        new Thread(new Runnable() {

            SyncHttpClient client = new SyncHttpClient();

            @Override
            public void run() {
                client.get("https://raw.githubusercontent.com/pcm-dpc/COVID-19/master/dati-json/dpc-covid19-ita-andamento-nazionale.json", new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.d("AndroTag", "onSuccessJSONArray");

                        dataStorage.setDatiNazionaliJson(response);
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
        } else {
            btnIndietro.setEnabled(false);
        }

        if (cursore < dataStorage.getDatiNazionaliJson().length() - 1) {
            btnAvanti.setEnabled(true);
        } else {
            btnAvanti.setEnabled(false);
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnAvanti:
                    if (cursore < dataStorage.getDatiNazionaliJson().length()) {
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
