package org.twistedappdeveloper.statocovid19italia;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private TextView txtTotaleCasi, txtDeceduti, txtGuariti, txtRicoverati, txtTamponi, txtIsolamento, txtNuoviCasi, txtPositivi, txtData;

    private ProgressDialog pdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtTotaleCasi = findViewById(R.id.txtCasiValue);
        txtDeceduti = findViewById(R.id.txtDecedutiValue);
        txtGuariti = findViewById(R.id.txtGuaritiValue);
        txtRicoverati = findViewById(R.id.txtRicoveratiValue);
        txtTamponi = findViewById(R.id.txtTamponiValue);
        txtIsolamento = findViewById(R.id.txtIsolamentoValue);
        txtNuoviCasi = findViewById(R.id.txtNuoviCasiValue);
        txtPositivi = findViewById(R.id.txtAttualmentePositiviValue);

        txtData = findViewById(R.id.txtData);
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

                            final String totaleCasi = obj.getString("totale_casi");
                            final String deceduti = obj.getString("deceduti");
                            final String dimessiGuariti = obj.getString("dimessi_guariti");
                            final String isolamento = obj.getString("isolamento_domiciliare");
                            final String totale_attualmente_positivi = obj.getString("totale_attualmente_positivi");
                            final String nuovi_attualmente_positivi = obj.getString("nuovi_attualmente_positivi");
                            final String ricoverati_con_sintomi = obj.getString("ricoverati_con_sintomi");
                            final String tamponi = obj.getString("tamponi");

                            final String data = obj.getString("data");


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtTotaleCasi.setText(totaleCasi);
                                    txtDeceduti.setText(deceduti);
                                    txtGuariti.setText(dimessiGuariti);
                                    txtTamponi.setText(tamponi);
                                    txtIsolamento.setText(isolamento);
                                    txtPositivi.setText(totale_attualmente_positivi);
                                    txtNuoviCasi.setText(nuovi_attualmente_positivi);
                                    txtRicoverati.setText(ricoverati_con_sintomi);

                                    txtData.setText(String.format("Dati al: %s", data));

                                    pdialog.dismiss();
                                }
                            });


                        } catch (JSONException e) {
                            Log.e("Errore", e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.i("AndroTag", "onSuccessJSONObject");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.i("AndroTag", "onFailureJSONObject");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.i("AndroTag", "onFailureJSONArray");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.i("AndroTag", "onFailureString");
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        super.onSuccess(statusCode, headers, responseString);
                        Log.i("AndroTag", "onSuccessString");
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
