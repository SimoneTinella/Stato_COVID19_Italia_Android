package org.twistedappdeveloper.statocovid19italia;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.twistedappdeveloper.statocovid19italia.DataStorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.fragments.DataVisualizerFragment;
import org.twistedappdeveloper.statocovid19italia.utils.Utils;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private FragmentManager fragmentManager;
    private DataVisualizerFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        currentFragment = DataVisualizerFragment.newInstance("Nazionale");
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container, currentFragment);
        fragmentTransaction.commit();
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
                if (currentFragment.getDataStorage().getDataLength() > 0) {
                    Intent chartActivity = new Intent(getApplicationContext(), ChartActivity.class);
                    chartActivity.putExtra(Utils.DATACONTEXT_KEY, currentFragment.getDataStorage().getDataContext());
                    startActivity(chartActivity);
                } else {
                    Toast.makeText(MainActivity.this, "Non sono presenti dati da graficare, prova ad aggiornare.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_update:
                recoverData();
                break;
            case R.id.action_change_datacontex:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final String[] dataContexs = DataStorage.getIstance().getSubLevelDataKeys().toArray(new String[0]);
                int checkedItem = 0;
                for (int i = 0; i < dataContexs.length; i++) {
                    if (dataContexs[i].equalsIgnoreCase(currentFragment.getDataStorage().getDataContext())) {
                        checkedItem = i;
                        break;
                    }
                }
                builder.setSingleChoiceItems(dataContexs, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        DataVisualizerFragment newFragment = DataVisualizerFragment.newInstance(dataContexs[which], currentFragment.getCursore());
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container, newFragment);
                        fragmentTransaction.commit();
                        currentFragment = newFragment;
                    }
                });
                builder.setTitle("Seleziona contesto dati");
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.action_confronto_regionale:
                if (DataStorage.getIstance().getSubLevelDataKeys().size() > 0) {
                    Intent barChartActivity = new Intent(getApplicationContext(), BarChartActivity.class);
                    barChartActivity.putExtra(Utils.CURSORE_KEY, currentFragment.getCursore());
                    startActivity(barChartActivity);
                } else {
                    Toast.makeText(MainActivity.this, "Non sono presenti dati da graficare, prova ad aggiornare.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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
                            } else if (latestVersion < currentVersion) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Hai una versione preview dell'App", Toast.LENGTH_SHORT).show();
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

    private void recoverData() {
        if (!Utils.isDeviceOnline(MainActivity.this)) {
            Toast.makeText(MainActivity.this, "Il dispositivo non ha accesso ad Internet, attiva la connessione e riprova.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = ProgressDialog.show(MainActivity.this, "", "Attendere prego...", true);

        final Thread threadDatiNazionali = new Thread(new Runnable() {

            SyncHttpClient client = new SyncHttpClient();

            @Override
            public void run() {
                client.get("https://raw.githubusercontent.com/pcm-dpc/COVID-19/master/dati-json/dpc-covid19-ita-andamento-nazionale.json", new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        DataStorage.getIstance().setDataArrayJson(response);
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
                        DataStorage.getIstance().setSubLvlDataArrayJson(response);
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
                            currentFragment = DataVisualizerFragment.newInstance(currentFragment.getDataStorage().getDataContext());
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container, currentFragment);
                            fragmentTransaction.commit();
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        recoverData();
        checkAppVersion();
    }


}
