package org.twistedappdeveloper.statocovid19italia;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import org.twistedappdeveloper.statocovid19italia.datastorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.fragments.DataVisualizerFragment;
import org.twistedappdeveloper.statocovid19italia.model.Changelog;
import org.twistedappdeveloper.statocovid19italia.network.NetworkUtils;
import org.twistedappdeveloper.statocovid19italia.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private FragmentManager fragmentManager;
    private DataVisualizerFragment currentFragment;

    private SyncHttpClient client = new SyncHttpClient();

    private List<Changelog> changelogs = new ArrayList<>();

    private DataStorage nationalDataStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nationalDataStorage = DataStorage.createAndGetIstanceIfNotExist(getResources(), DataStorage.Scope.NAZIONALE);

        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();

        currentFragment = DataVisualizerFragment.newInstance(DataStorage.defaultDataContext);
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

    private void openChangelog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setTitle(String.format(getString(R.string.ver_installata), BuildConfig.VERSION_NAME));
        String versione = getString(R.string.versione).substring(0, 1).toUpperCase() + getString(R.string.versione).substring(1);
        String msgChangelogs = "";
        for (Changelog changelog : changelogs) {
            msgChangelogs = String.format("%s\n\n%s %s\n%s", msgChangelogs, versione, changelog.getVersionaName(), changelog.getDescription());
        }
        if (changelogs.isEmpty()) {
            alertDialog.setMessage(getString(R.string.no_changelog));
        } else {
            alertDialog.setMessage(msgChangelogs.substring(2));
        }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog alertDialog;
        switch (item.getItemId()) {
            case R.id.action_info:
                final SpannableString s =
                        new SpannableString(getString(R.string.infoMessage));
                Linkify.addLinks(s, Linkify.WEB_URLS);
                alertDialog = alertDialogBuilder.create();
                alertDialog.setTitle(getString(R.string.info));
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
            case R.id.action_changelog:
                openChangelog();
                break;
            case R.id.action_chart:
                if (currentFragment.getDataStorage().getDataLength() > 0) {
                    Intent chartActivity = new Intent(getApplicationContext(), ChartActivity.class);
                    chartActivity.putExtra(Utils.DATACONTEXT_KEY, currentFragment.getDataStorage().getDataContext());
                    startActivity(chartActivity);
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.no_data_to_display), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_update:
                recoverData();
                checkAppVersion();
                break;
            case R.id.action_change_datacontex:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                List<String> regioni = new ArrayList<>();
                regioni.add(DataStorage.defaultDataContext);
                regioni.addAll(DataStorage.getIstance().getSubLevelDataKeys());
                final String[] dataContexs = regioni.toArray(new String[0]);
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
                builder.setTitle(getResources().getString(R.string.sel_dataContext));
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.action_confronto_regionale:
                if (DataStorage.getIstance().getSubLevelDataKeys().size() > 0) {
                    Intent barChartActivity = new Intent(getApplicationContext(), BarChartActivity.class);
                    barChartActivity.putExtra(Utils.CURSORE_KEY, currentFragment.getCursore());
                    startActivity(barChartActivity);
                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.no_data_to_display), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private String formatText(int n) {
        if (n == 1) {
            return String.format("%s %s", n, getResources().getString(R.string.versione));
        }
        return String.format("%s %s", n, getResources().getString(R.string.versioni));
    }

    private void checkAppVersion() {
        if (!NetworkUtils.isDeviceOnline(MainActivity.this)) {
            return;
        }

        new Thread(new Runnable() {


            @Override
            public void run() {
                client.get(getResources().getString(R.string.notification_file), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        try {
                            final int latestVersion = response.getInt("latest_app_version");

                            changelogs.clear();
                            JSONArray changelogJSONArray = response.getJSONArray("changelog");
                            for (int i = 0; i < changelogJSONArray.length(); i++) {
                                JSONObject jsonObject = changelogJSONArray.getJSONObject(i);
                                changelogs.add(new Changelog(jsonObject.getString("ver"), jsonObject.getString("description")));
                            }

                            final int currentVersion = BuildConfig.VERSION_CODE;
                            if (latestVersion > currentVersion) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle(getResources().getString(R.string.app_update));
                                        builder.setMessage(String.format(getResources().getString(R.string.app_update_message),
                                                formatText(latestVersion - currentVersion),
                                                BuildConfig.VERSION_NAME,
                                                changelogs.get(0).getVersionaName()
                                        ));
                                        if (BuildConfig.DEBUG) {
                                            builder.setPositiveButton(getResources().getString(R.string.aggiorna), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    Intent i = new Intent(Intent.ACTION_VIEW);
                                                    i.setData(Uri.parse(getString(R.string.new_version_app_site)));
                                                    startActivity(i);
                                                }
                                            });
                                        }
                                        builder.setNegativeButton(getString(R.string.chiudi), new DialogInterface.OnClickListener() {
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
                                        Toast.makeText(MainActivity.this, getResources().getString(R.string.app_preview), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                String key = "versione";
                                SharedPreferences pref = getApplicationContext().getSharedPreferences("default", 0);
                                int version = pref.getInt(key, 0);
                                if (version < BuildConfig.VERSION_CODE) {
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putInt(key, BuildConfig.VERSION_CODE);
                                    editor.apply();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            openChangelog();
                                        }
                                    });
                                }
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
        if (!NetworkUtils.isDeviceOnline(MainActivity.this)) {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = ProgressDialog.show(MainActivity.this, "", getResources().getString(R.string.wait_pls), true);

        final Thread threadFetchData = new Thread(new Runnable() {

            @Override
            public void run() {
                client.get(getString(R.string.dataset_avvisi), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        super.onSuccess(statusCode, headers, response);
                        nationalDataStorage.setAvvisiDataArrayJson(response);

                        client.get(getResources().getString(R.string.dataset_nazionale), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                super.onSuccess(statusCode, headers, response);
                                nationalDataStorage.setDataArrayJson(response);

                                client.get(getResources().getString(R.string.dataset_regionale), new JsonHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                        super.onSuccess(statusCode, headers, response);
                                        nationalDataStorage.setSubLvlDataArrayJson(response, DataStorage.DEN_REGIONE_KEY, DataStorage.Scope.REGIONALE);

                                        client.get(getResources().getString(R.string.dataset_provinciale), new JsonHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                                                super.onSuccess(statusCode, headers, response);
                                                Map<String, JSONArray> datiPerRegione = new HashMap<>();
                                                for (int i = 0; i < response.length(); i++) {
                                                    try {
                                                        JSONObject jsonObject = response.getJSONObject(i);
                                                        String regione = jsonObject.getString(DataStorage.DEN_REGIONE_KEY);
                                                        JSONArray datiProvince;
                                                        if (datiPerRegione.containsKey(regione)) {
                                                            datiProvince = datiPerRegione.get(regione);
                                                        } else {
                                                            datiProvince = new JSONArray();
                                                            datiPerRegione.put(regione, datiProvince);
                                                        }
                                                        datiProvince.put(jsonObject);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                for (String regione : datiPerRegione.keySet()) {
                                                    nationalDataStorage
                                                            .getDataStorageByDataContext(regione)
                                                            .setSubLvlDataArrayJson(
                                                                    datiPerRegione.get(regione),
                                                                    DataStorage.DEN_PROVINCIA_KEY,
                                                                    DataStorage.Scope.PROVINCIALE
                                                            );
                                                }
                                            }
                                        });
                                    }
                                });

                            }

                        });

                    }
                });

            }
        });

        threadFetchData.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    threadFetchData.join();
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
