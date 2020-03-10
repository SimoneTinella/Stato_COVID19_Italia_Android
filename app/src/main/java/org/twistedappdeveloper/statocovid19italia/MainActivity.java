package org.twistedappdeveloper.statocovid19italia;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

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

    private ListView listView;
    private DataAdapter adapter;

    private ProgressDialog pdialog;

    private List<Data> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataList = new ArrayList<>();

        txtData = findViewById(R.id.txtName);

        listView = findViewById(R.id.listView);
        adapter = new DataAdapter(getApplicationContext(), R.layout.list_data, dataList);
        listView.setAdapter(adapter);
    }


    private String getColorByKey(String key) {
        switch (key) {

            case "deceduti":
                return "#FF0000";

            case "totale_casi":
                return "#FFB300";

            case "dimessi_guariti":
                return "#00FF00";

            default:
                return "#000000";
        }
    }

    private int getPositionByKey(String key) {
        switch (key) {

            case "deceduti":
                return 2;

            case "totale_casi":
                return 0;

            case "dimessi_guariti":
                return 1;

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
                        Log.i("AndroTag", "onSuccessJSONArray");

                        try {
                            JSONObject obj = response.getJSONObject(response.length() - 1);

                            final String data = obj.getString("data");

                            dataList.clear();
                            for (Iterator<String> keys = obj.keys(); keys.hasNext(); ) {
                                String key = keys.next();

                                if (key.equalsIgnoreCase("data") ||
                                        key.equalsIgnoreCase("stato")
                                ) {
                                    continue;
                                }
                                String[] strings = key.split("_");
                                String name = "";
                                for (int i = 0; i < strings.length; i++) {
                                    name = String.format(
                                            "%s %s",
                                            name,
                                            String.format("%s%s", strings[i].substring(0, 1).toUpperCase(), strings[i].substring(1))
                                    );
                                }
                                String value = obj.getString(key);
                                dataList.add(new Data(name.trim(), value, getColorByKey(key), getPositionByKey(key)));
                            }


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtData.setText(String.format("Dati al: %s", data));

                                    Collections.sort(dataList);
                                    adapter.notifyDataSetChanged();
                                    pdialog.dismiss();
                                }
                            });


                        } catch (JSONException e) {
                            Log.e("Errore", e.getMessage(), e);
                        }
                    }

                });

            }
        }).start();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateValues();
    }

}
