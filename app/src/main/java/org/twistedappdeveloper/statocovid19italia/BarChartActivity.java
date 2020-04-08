package org.twistedappdeveloper.statocovid19italia;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import org.twistedappdeveloper.statocovid19italia.adapters.ProvinceAdapter;
import org.twistedappdeveloper.statocovid19italia.datastorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.model.ProvinceSelection;
import org.twistedappdeveloper.statocovid19italia.model.ProvinceSelectionWrapper;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.model.TrendValue;
import org.twistedappdeveloper.statocovid19italia.utils.TrendUtils;
import org.twistedappdeveloper.statocovid19italia.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static org.twistedappdeveloper.statocovid19italia.ProvincialBarChartActivity.MAX_ELEMENTS;
import static org.twistedappdeveloper.statocovid19italia.ProvincialBarChartActivity.MIN_ELEMENTS;

public class BarChartActivity extends AppCompatActivity implements View.OnClickListener {

    private BarChart chart;
    private DataStorage dataStorage;
    private TextView txtMarkerData;
    ImageButton btnChangeOrder;

    private Button btnPercentage;
    private ImageButton btnIndietro;
    private ImageButton btnAvanti;

    private int cursore, dataLen;

    private String selectedTrendKey;

    private TrendInfo[] trendInfoList;
    private String[] trendsName;
    private int checkedItem;

    private Map<String, List<ProvinceSelection>> provinceListMap;

    private boolean orderTrend = false;

    private boolean dispalyPercentage = false;

    private Map<String, TrendValue> currentValues = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        txtMarkerData = findViewById(R.id.txtMarkerData);
        chart = findViewById(R.id.barChart);
        btnIndietro = findViewById(R.id.btnIndietro);
        btnAvanti = findViewById(R.id.btnAvanti);
        ImageButton btnCambiaMisura = findViewById(R.id.btnCambiaMisura);
        ImageButton btnGraficoProvinciale = findViewById(R.id.btnProvinciale);
        ImageButton btnChangeDate = findViewById(R.id.btnChangeDate);
        btnChangeOrder = findViewById(R.id.btnChangeOrder);
        btnPercentage = findViewById(R.id.btnPercentage);

        BarChartActivityMaker barChartActivityMaker = new BarChartActivityMaker(getApplicationContext());
        chart.setMarker(barChartActivityMaker);

        btnIndietro.setOnClickListener(this);
        btnAvanti.setOnClickListener(this);
        btnCambiaMisura.setOnClickListener(this);
        btnGraficoProvinciale.setOnClickListener(this);
        btnChangeOrder.setOnClickListener(this);
        btnPercentage.setOnClickListener(this);
        btnChangeDate.setOnClickListener(this);

        dataStorage = DataStorage.getIstance();
        dataLen = dataStorage.getDataStorageByDataContext(dataStorage.getSubLevelDataKeys().get(0)).getDataLength();

        chart.setTouchEnabled(true);
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);

        chart.getAxisLeft().setEnabled(false);

        ValueFormatter xAxisFormatter = new RegioniFormatter();
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(21);
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.setLabelRotationAngle(90);

        List<TrendInfo> trendInfoListTmp = dataStorage.getTrendsList();
        trendInfoList = new TrendInfo[trendInfoListTmp.size()];
        trendsName = new String[trendInfoListTmp.size()];
        for (TrendInfo trendInfo : trendInfoListTmp) {
            int pos = TrendUtils.getPositionByTrendKey(trendInfo.getKey());
            trendInfoList[pos] = trendInfo;
            trendsName[pos] = trendInfo.getName();
        }

        cursore = getIntent().getIntExtra(Utils.CURSORE_KEY, dataLen - 1);
        selectedTrendKey = getIntent().getStringExtra(Utils.TREND_KEY);

        if (selectedTrendKey == null || selectedTrendKey.isEmpty()) {
            selectedTrendKey = trendInfoList[checkedItem].getKey();
        }

        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.SQUARE);
        l.setTextSize(12f);
        l.setTextColor(Color.BLACK);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXOffset(-5f);

        btnEnableStatusCheck();

        provinceListMap = new HashMap<>();
        for (String regione : dataStorage.getSubLevelDataKeys()) {
            for (String provincia : dataStorage.getDataStorageByDataContext(regione).getSubLevelDataKeys()) {
                if (!provincia.equals("In fase di definizione/aggiornamento")) {
                    List<ProvinceSelection> provinceSelections;
                    if (provinceListMap.containsKey(regione)) {
                        provinceSelections = provinceListMap.get(regione);
                    } else {
                        provinceSelections = new ArrayList<>();
                        provinceListMap.put(regione, provinceSelections);
                    }
                    provinceSelections.add(new ProvinceSelection(provincia, false));
                }
            }
        }
        for (List<ProvinceSelection> provinceSelections : provinceListMap.values()) {
            Collections.sort(provinceSelections);
        }
        setData(selectedTrendKey);
    }


    private void setData(String trendKey) {
        currentValues.clear();
        ArrayList<BarEntry> barValues = new ArrayList<>();

        boolean isMinimumZero = true;

        int i = 0;
        for (String dataContext : dataStorage.getSubLevelDataKeys()) {
            DataStorage regionalDataStore = dataStorage.getDataStorageByDataContext(dataContext);
            TrendValue trendValue = regionalDataStore.getTrendByKey(trendKey).getTrendValueByIndex(cursore);
            currentValues.put(dataContext, trendValue);
            float value;
            if (dispalyPercentage) {
                value = trendValue.getDeltaPercentage() * 100;
            } else {
                value = trendValue.getValue();
            }
            barValues.add(new BarEntry(i++, value, dataContext));
            txtMarkerData.setText(String.format(getString(R.string.dati_relativi_al), trendValue.getDate()));

            if (value < 0) {
                isMinimumZero = false;
            }
        }
        if (orderTrend) {
            Utils.quickSort(barValues, 0, barValues.size() - 1);
        }

        if (isMinimumZero) {
            chart.getAxisLeft().setAxisMinimum(0);
            chart.getAxisRight().setAxisMinimum(0);
        } else {
            chart.getAxisLeft().resetAxisMinimum();
            chart.getAxisRight().resetAxisMinimum();
        }

        BarDataSet barDataSet;
        barDataSet = new BarDataSet(barValues, TrendUtils.getTrendNameByTrendKey(getApplicationContext().getResources(), trendKey));
        barDataSet.setDrawIcons(false);
        barDataSet.setColor(TrendUtils.getColorByTrendKey(BarChartActivity.this, trendKey));

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(9f);
        chart.setData(data);
        chart.animateY(200);
        checkBarValueVisualization();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAvanti:
                if (cursore < dataStorage.getDataLength()) {
                    cursore++;
                }
                btnEnableStatusCheck();
                setData(selectedTrendKey);
                break;
            case R.id.btnIndietro:
                if (cursore > 0) {
                    cursore--;
                }
                btnEnableStatusCheck();
                setData(selectedTrendKey);
                break;
            case R.id.btnCambiaMisura:
                AlertDialog.Builder builder = new AlertDialog.Builder(BarChartActivity.this);

                builder.setSingleChoiceItems(trendsName, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        selectedTrendKey = trendInfoList[which].getKey();
                        setData(selectedTrendKey);
                        checkedItem = which;
                    }
                });
                builder.setTitle(getResources().getString(R.string.sel_misura));
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.btnProvinciale:
                final Dialog dialog = new Dialog(BarChartActivity.this, R.style.AppAlert);
                dialog.setContentView(R.layout.dialog_province);

                final ListView listViewProvince = dialog.findViewById(R.id.listViewDialogProvince);
                final Button btnOk = dialog.findViewById(R.id.btnCloseTrendDialog);
                final Button btnDeselectAllTrends = dialog.findViewById(R.id.btnDeselectAll);

                final List<ProvinceSelectionWrapper> provinceSelectionWrappers = new ArrayList<>();
                for (String regione : provinceListMap.keySet()) {
                    provinceSelectionWrappers.add(new ProvinceSelectionWrapper(regione, provinceListMap.get(regione)));
                }
                Collections.sort(provinceSelectionWrappers);

                TextView textView = dialog.findViewById(R.id.txtProvinceDialogTitle);
                textView.setText(String.format("%s (%s sel.)", getString(R.string.province_da_visualizzare), numberOfSelectedElement()));
                final ProvinceAdapter provinceAdapter = new ProvinceAdapter(BarChartActivity.this, R.layout.list_province, provinceSelectionWrappers, textView);
                listViewProvince.setAdapter(provinceAdapter);

                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        switch (v.getId()) {
                            case R.id.btnCloseTrendDialog:
                                if (numberOfSelectedElement() < MIN_ELEMENTS || numberOfSelectedElement() > MAX_ELEMENTS) {
                                    Toast.makeText(BarChartActivity.this, String.format(getString(R.string.limite_selezione), MIN_ELEMENTS, MAX_ELEMENTS), Toast.LENGTH_LONG).show();
                                } else {
                                    dialog.dismiss();
                                    Intent provincialBarActivity = new Intent(getApplicationContext(), ProvincialBarChartActivity.class);
                                    List<String> selectedProvince = new ArrayList<>();
                                    for (List<ProvinceSelection> provinceList : provinceListMap.values()) {
                                        for (ProvinceSelection provinceSelection : provinceList) {
                                            if (provinceSelection.isSelected()) {
                                                selectedProvince.add(provinceSelection.getProvincia());
                                            }
                                        }
                                    }
                                    provincialBarActivity.putExtra(Utils.CURSORE_KEY, cursore);
                                    provincialBarActivity.putExtra(Utils.TREND_KEY, DataStorage.TOTALE_CASI_KEY);
                                    provincialBarActivity.putExtra(Utils.PROVINCE_ARRAY_KEY, selectedProvince.toArray(new String[0]));
                                    startActivityForResult(provincialBarActivity, 0);
                                }
                                break;
                            case R.id.btnDeselectAll:
                                for (List<ProvinceSelection> provinceList : provinceListMap.values()) {
                                    for (ProvinceSelection provinceSelection : provinceList) {
                                        provinceSelection.setSelected(false);
                                    }
                                }
                                provinceAdapter.notifyDataSetChanged();
                                break;
                        }
                    }
                };

                btnOk.setOnClickListener(clickListener);
                btnDeselectAllTrends.setOnClickListener(clickListener);
                dialog.show();
                break;

            case R.id.btnChangeOrder:
                orderTrend = !orderTrend;
                if (!orderTrend) {
                    btnChangeOrder.setImageResource(R.drawable.baseline_bar_chart_white_24);
                } else {
                    btnChangeOrder.setImageResource(R.drawable.baseline_signal_cellular_alt_white_24);
                }
                setData(selectedTrendKey);
                break;
            case R.id.btnPercentage:
                dispalyPercentage = !dispalyPercentage;
                if (!dispalyPercentage) {
                    btnPercentage.setText("123");
                } else {
                    btnPercentage.setText("%");
                }
                setData(selectedTrendKey);
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
                    DatePickerDialog datePickerDialog = new DatePickerDialog(BarChartActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, month);
                                    calendar.set(Calendar.DAY_OF_MONTH, day);
                                    cursore = dataStorage.getIndexByDate(calendar.getTime());
                                    btnEnableStatusCheck();
                                    setData(selectedTrendKey);
                                }
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                    datePickerDialog.getDatePicker().setMinDate(minData.getTime());
                    datePickerDialog.getDatePicker().setMaxDate(maxData.getTime());
                    datePickerDialog.show();
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(BarChartActivity.this, "Non è possibile selezionare un data", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private int numberOfSelectedElement() {
        int n = 0;
        for (List<ProvinceSelection> provinceList : provinceListMap.values()) {
            for (ProvinceSelection provinceSelection : provinceList) {
                if (provinceSelection.isSelected()) {
                    n++;
                }
            }
        }
        return n;
    }

    private void btnEnableStatusCheck() {
        if (cursore > 0) {
            btnIndietro.setEnabled(true);
            btnIndietro.setImageResource(R.drawable.baseline_keyboard_backspace_white_24);
        } else {
            btnIndietro.setEnabled(false);
            btnIndietro.setImageResource(R.drawable.baseline_keyboard_backspace_gray_24);
        }

        if (cursore < dataLen - 1) {
            btnAvanti.setEnabled(true);
            btnAvanti.setImageResource(R.drawable.baseline_keyboard_white_24);
        } else {
            btnAvanti.setEnabled(false);
            btnAvanti.setImageResource(R.drawable.baseline_keyboard_gray_24);
        }
    }

    private void checkBarValueVisualization() {
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == ORIENTATION_LANDSCAPE) {
            chart.getData().setDrawValues(true);
        } else {
            chart.getData().setDrawValues(dispalyPercentage);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        checkBarValueVisualization();
    }

    private class RegioniFormatter extends ValueFormatter {
        private static final int maxLen = 10;

        @Override
        public String getFormattedValue(float value) {
            BarEntry barEntry = chart.getData().getDataSetByIndex(0).getEntryForIndex((int) value);
            String nomeRegione = barEntry.getData().toString();
            if (nomeRegione.length() > maxLen) {
                return String.format("%s.", nomeRegione.substring(0, getMaxLength(nomeRegione)));
            } else {
                return nomeRegione.substring(0, getMaxLength(nomeRegione));
            }
        }

        private int getMaxLength(String nomeRegione) {
            return Math.min(maxLen, nomeRegione.length());
        }
    }


    public class BarChartActivityMaker extends MarkerView {

        private TextView txtBarMarkerTitle, txtBarMarkerCurrentValue, txtBarPrecValue, txtBarMarkerPercentage, txtBarMarkerVariazione;

        public BarChartActivityMaker(Context context) {
            super(context, R.layout.bar_chart_marker);
            txtBarMarkerTitle = findViewById(R.id.txtBarMarkerTitle);
            txtBarMarkerCurrentValue = findViewById(R.id.txtBarMarkerCurrentValue);
            txtBarPrecValue = findViewById(R.id.txtBarPrecValue);
            txtBarMarkerPercentage = findViewById(R.id.txtBarMarkerPercentage);
            txtBarMarkerVariazione = findViewById(R.id.txtBarMarkerVariazione);
        }

        @Override
        public MPPointF getOffset() {
            super.getOffset().x = -getWidth();
            super.getOffset().y = -getHeight();
            return super.getOffset();
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            int position = (int) e.getX();
            String dataContext = chart.getData().getDataSetByIndex(0).getEntryForIndex(position).getData().toString();
            txtBarMarkerTitle.setText(dataContext);

            TrendValue trendValue = currentValues.get(dataContext);
            txtBarMarkerCurrentValue.setText(String.format("%s", trendValue.getValue()));
            txtBarPrecValue.setText(String.format("%s", trendValue.getPrecValue()));
            txtBarMarkerPercentage.setText(String.format(Locale.ITALIAN, "%.2f%%", trendValue.getDeltaPercentage() * 100));
            txtBarMarkerVariazione.setText(String.format("%s", trendValue.getDelta()));

            int color = TrendUtils.getColorByTrendKey(getApplicationContext(), selectedTrendKey);
            txtBarMarkerCurrentValue.setTextColor(color);
            txtBarPrecValue.setTextColor(color);
            txtBarMarkerVariazione.setTextColor(color);
            txtBarMarkerPercentage.setTextColor(color);

            super.refreshContent(e, highlight);
        }
    }

}
