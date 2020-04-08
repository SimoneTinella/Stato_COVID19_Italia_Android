package org.twistedappdeveloper.statocovid19italia.fragments;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.twistedappdeveloper.statocovid19italia.BarChartActivity;
import org.twistedappdeveloper.statocovid19italia.R;
import org.twistedappdeveloper.statocovid19italia.adapters.RowDataAdapter;
import org.twistedappdeveloper.statocovid19italia.datastorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.model.Avviso;
import org.twistedappdeveloper.statocovid19italia.model.RowData;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getColorByTrendKey;
import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getPositionByTrendKey;
import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getTrendDescriptionByTrendKey;
import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getTrendNameByTrendKey;
import static org.twistedappdeveloper.statocovid19italia.utils.Utils.CURSORE_KEY;
import static org.twistedappdeveloper.statocovid19italia.utils.Utils.DATACONTEXT_KEY;


public class DataVisualizerFragment extends Fragment {
    private static final String IS_CUSTOM_CURSOR = "custom_cursore";

    public static DataVisualizerFragment newInstance(@NonNull String dataContext) {
        DataVisualizerFragment fragment = new DataVisualizerFragment();
        Bundle args = new Bundle();
        args.putString(DATACONTEXT_KEY, dataContext);
        fragment.setArguments(args);
        return fragment;
    }

    public static DataVisualizerFragment newInstance(@NonNull String dataContext, int cursore) {
        DataVisualizerFragment fragment = new DataVisualizerFragment();
        Bundle args = new Bundle();
        args.putString(DATACONTEXT_KEY, dataContext);
        args.putInt(CURSORE_KEY, cursore);
        args.putBoolean(IS_CUSTOM_CURSOR, true);
        fragment.setArguments(args);
        return fragment;
    }

    private DataVisualizerFragment() {
    }

    private String dataContext;

    private RowDataAdapter adapter;
    private List<RowData> rowDataList;

    private Button btnAvanti, btnIndietro, btnChangeValues, btnAvviso;
    private ImageButton btnChangeDate;
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

    private boolean displayPercentage = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();

        rowDataList = new ArrayList<>();
        dataContext = arguments.getString(DATACONTEXT_KEY);

        dataStorage = DataStorage.getIstance().getDataStorageByDataContext(dataContext);
        if (dataStorage.getDataLength() > 0) {
            int maxCursorValue = dataStorage.getDataLength() - 1;
            if (arguments.getBoolean(IS_CUSTOM_CURSOR, false)) {
                int customCursor = arguments.getInt(CURSORE_KEY);
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
            RowData rowData = new RowData(
                    trendInfo.getName(),
                    trendInfo.getTrendValueByIndex(cursore).getValue(),
                    getColorByTrendKey(getContext(), trendInfo.getKey()),
                    getPositionByTrendKey(trendInfo.getKey()),
                    trendInfo.getKey(),
                    trendInfo.getTrendValueByIndex(cursore).getDeltaPercentage(),
                    trendInfo.getTrendValueByIndex(cursore).getDelta(),
                    trendInfo.getTrendValueByIndex(cursore).getPrecValue()
            );
            //Aggiungo i dati provinciali
            if (dataStorage.getDataContextScope() == DataStorage.Scope.REGIONALE) {
                List<String> province = dataStorage.getSubLevelDataKeys();
                Collections.sort(province);
                for (String provincia : province) {
                    TrendInfo totaleCasiProvincia = dataStorage.getDataStorageByDataContext(provincia).getTrendByKey(trendInfo.getKey());
                    if (totaleCasiProvincia != null) {
                        RowData provincialRowData = new RowData(
                                provincia,
                                totaleCasiProvincia.getTrendValueByIndex(cursore).getValue(),
                                getColorByTrendKey(getContext(), totaleCasiProvincia.getKey()),
                                0, //non usato in questo caso
                                totaleCasiProvincia.getKey()
                        );
                        rowData.addSubItem(provincialRowData);
                    }
                }
            }
            rowDataList.add(rowData);
        }
        String data = dataStorage.getFullDateStringByIndex(cursore);
        txtData.setText(String.format(getString(R.string.relativo_al), data));
        Collections.sort(rowDataList);
        adapter.notifyDataSetChanged();
        btnEnableStatusCheck();

        List<Avviso> note = dataStorage.getAvvisiRelativoByDate(data);
        if (note != null && note.size() > 0) {
            btnAvviso.setVisibility(View.VISIBLE);
        } else {
            btnAvviso.setVisibility(View.GONE);
        }

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

    private void checkExtraInfo() {
        if (displayPercentage) {
            btnChangeValues.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_toggle_on_white_24, 0, 0, 0);
        } else {
            btnChangeValues.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_toggle_off_white_24, 0, 0, 0);
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
                case R.id.btnChangeValues:
                    displayPercentage = !displayPercentage;
                    adapter.setDisplayInfo(displayPercentage);
                    checkExtraInfo();
                    break;
                case R.id.btnChangeDate:
                    try {
                        String minDataS = dataStorage.getFullDateStringByIndex(0);
                        String maxDataS = dataStorage.getFullDateStringByIndex(dataStorage.getDataLength() - 1);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);
                        Date minData = dateFormat.parse(minDataS);
                        Date maxData = dateFormat.parse(maxDataS);

                        final Calendar calendar = Calendar.getInstance();
                        calendar.setTime(dataStorage.getDateByIndex(cursore));
                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                        calendar.set(Calendar.YEAR, year);
                                        calendar.set(Calendar.MONTH, month);
                                        calendar.set(Calendar.DAY_OF_MONTH, day);
                                        cursore = dataStorage.getIndexByDate(calendar.getTime());
                                        displayData();
                                    }
                                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                        datePickerDialog.getDatePicker().setMinDate(minData.getTime());
                        datePickerDialog.getDatePicker().setMaxDate(maxData.getTime());
                        datePickerDialog.show();
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Non è possibile selezionare un data", Toast.LENGTH_SHORT).show();
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
        txtTitle.setText(String.format(getString(R.string.andamento), dataContext));

        txtData = root.findViewById(R.id.txtName);
        btnAvanti = root.findViewById(R.id.btnAvanti);
        btnIndietro = root.findViewById(R.id.btnIndietro);
        btnChangeValues = root.findViewById(R.id.btnChangeValues);
        btnAvviso = root.findViewById(R.id.btnAvviso);
        btnChangeDate = root.findViewById(R.id.btnChangeDate);

        ListView listView = root.findViewById(R.id.listView);
        adapter = new RowDataAdapter(getContext(), R.layout.list_data, rowDataList, true);
        listView.setAdapter(adapter);
        listView.setEmptyView(root.findViewById(R.id.txtEmpty));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String key = rowDataList.get(position).getKey();

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getTrendNameByTrendKey(getContext().getResources(), key));
                builder.setMessage(
                        String.format(
                                "%s\n\n%s",
                                getTrendDescriptionByTrendKey(getContext(), key),
                                String.format(getString(R.string.confronto_regionale_message), dataStorage.getFullDateStringByIndex(cursore))
                        ));
                builder.setPositiveButton(getString(R.string.si), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent barChartActivity = new Intent(getContext(), BarChartActivity.class);
                        barChartActivity.putExtra(Utils.TREND_KEY, key);
                        barChartActivity.putExtra(CURSORE_KEY, cursore);
                        startActivity(barChartActivity);
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
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
        btnChangeValues.setOnClickListener(listener);
        btnChangeDate.setOnClickListener(listener);

        View.OnClickListener listenerAvvisi = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Avviso> avvisi = dataStorage.getAvvisiRelativoByDate(dataStorage.getFullDateStringByIndex(cursore));
                if (avvisi != null && avvisi.size() > 0) {
                    String avvisoText = buildAvvisoText(avvisi);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.setTitle("Avviso sui dati");
                    alertDialog.setMessage(Html.fromHtml(avvisoText));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        };
        btnAvviso.setOnClickListener(listenerAvvisi);

        btnAvanti.setEnabled(false);
        btnIndietro.setEnabled(false);

        if (isDataAvailable) {
            displayData();
        }

        checkExtraInfo();

        return root;
    }

    private String buildAvvisoText(List<Avviso> avvisi) {
        List<String> avvisiText = new ArrayList<>();

        for (Avviso avviso : avvisi) {
            String avvisoText = String.format("<b>Tipo Avviso:</b> %s<br><b>Avviso:</b> %s",
                    avviso.getTipoAvviso(),
                    avviso.getAvviso()
            );
            if (!avviso.getNote().isEmpty()) {
                avvisoText = String.format("%s<br><b>Nota:</b> %s", avvisoText, avviso.getNote());
            }
            if (!avviso.getRegione().isEmpty()) {
                avvisoText = String.format("%s<br><b>Regione:</b> %s", avvisoText, avviso.getRegione());
            }
            if (!avviso.getProvincia().isEmpty()) {
                avvisoText = String.format("%s<br><b>Provincia:</b> %s", avvisoText, avviso.getProvincia());
            }
            avvisiText.add(avvisoText);
        }

        return Utils.joinString("<br><br>", avvisiText);
    }

}
