package org.twistedappdeveloper.statocovid19italia;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.twistedappdeveloper.statocovid19italia.adapters.ProvinceAdapter;
import org.twistedappdeveloper.statocovid19italia.datastorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.model.ProvinceSelection;
import org.twistedappdeveloper.statocovid19italia.model.ProvinceSelectionWrapper;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.model.TrendValue;
import org.twistedappdeveloper.statocovid19italia.utils.TrendUtils;
import org.twistedappdeveloper.statocovid19italia.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProvincialBarChartActivity extends AppCompatActivity implements View.OnClickListener {

    private BarChart chart;
    private DataStorage dataStorage;
    private TextView txtMarkerData;

    private Button btnIndietro, btnAvanti, btnProvince;

    private int cursore, dataLen;

    private String selectedTrendKey;

    private List<String> selectedProvince;

    private Map<String, TrendInfo> trendInfoMap = new HashMap<>();

    private Map<String, List<ProvinceSelection>> provinceListMap;

    private final int MIN_ELEMENTS = 3;
    private final int MAX_ELEMENTS = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provincial_bar_chart);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        dataStorage = DataStorage.getIstance();
        dataLen = dataStorage.getDataStorageByDataContext(dataStorage.getSubLevelDataKeys().get(0)).getDataLength();

        cursore = getIntent().getIntExtra(Utils.CURSORE_KEY, dataLen - 1);
        selectedTrendKey = getIntent().getStringExtra(Utils.TREND_KEY);
        selectedProvince = new ArrayList<>();
        Collections.addAll(selectedProvince, getIntent().getStringArrayExtra(Utils.PROVINCE_ARRAY_KEY));

        for (String regione : dataStorage.getSubLevelDataKeys()) {
            for (String provincia : dataStorage.getDataStorageByDataContext(regione).getSubLevelDataKeys()) {
                if (!provincia.equals("In fase di definizione/aggiornamento")) {
                    trendInfoMap.put(provincia, dataStorage.getDataStorageByDataContext(regione).getDataStorageByDataContext(provincia).getTrendByKey(selectedTrendKey));
                }
            }
        }

        txtMarkerData = findViewById(R.id.txtMarkerData);
        chart = findViewById(R.id.barChart);
        btnIndietro = findViewById(R.id.btnIndietro);
        btnAvanti = findViewById(R.id.btnAvanti);
        btnProvince = findViewById(R.id.btnProvinciale);

        btnIndietro.setOnClickListener(this);
        btnAvanti.setOnClickListener(this);
        btnProvince.setOnClickListener(this);

        chart.setTouchEnabled(false);
        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.setHighlightFullBarEnabled(false);

        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisRight().setAxisMinimum(0);

        ValueFormatter xAxisFormatter = new ProvinceFormatter();
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(21);
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.setLabelRotationAngle(90);

        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.SQUARE);
        l.setTextSize(12f);
        l.setTextColor(Color.BLACK);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXOffset(-5f);

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
                    provinceSelections.add(new ProvinceSelection(provincia, selectedProvince.contains(provincia)));
                }
            }
        }
        for (List<ProvinceSelection> provinceSelections : provinceListMap.values()) {
            Collections.sort(provinceSelections);
        }


        btnEnableStatusCheck();

        setData();
    }


    private void setData() {
        ArrayList<BarEntry> values = new ArrayList<>();

        int i = 0;
        for (String selectedProvincia : selectedProvince) {
            TrendValue trendValue = trendInfoMap.get(selectedProvincia).getTrendValues().get(cursore);
            values.add(new BarEntry(i++, trendValue.getValue()));
            txtMarkerData.setText(String.format(getString(R.string.dati_relativi_al), trendValue.getDate()));
        }

        BarDataSet barDataSet;
        barDataSet = new BarDataSet(values, TrendUtils.getTrendNameByTrendKey(getApplicationContext().getResources(), selectedTrendKey));
        barDataSet.setDrawIcons(false);
        barDataSet.setColor(TrendUtils.getColorByTrendKey(ProvincialBarChartActivity.this, selectedTrendKey));

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);

        chart.setData(data);
        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.animateY(200);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAvanti:
                if (cursore < dataStorage.getDataLength()) {
                    cursore++;
                }
                btnEnableStatusCheck();
                setData();
                break;
            case R.id.btnIndietro:
                if (cursore > 0) {
                    cursore--;
                }
                btnEnableStatusCheck();
                setData();
                break;
            case R.id.btnProvinciale:
                final Dialog dialog = new Dialog(ProvincialBarChartActivity.this, R.style.AppAlert);
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
                final ProvinceAdapter provinceAdapter = new ProvinceAdapter(ProvincialBarChartActivity.this, R.layout.list_province, provinceSelectionWrappers, textView);
                listViewProvince.setAdapter(provinceAdapter);

                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        switch (v.getId()) {
                            case R.id.btnCloseTrendDialog:
                                if (numberOfSelectedElement() < MIN_ELEMENTS || numberOfSelectedElement() > MAX_ELEMENTS) {
                                    Toast.makeText(ProvincialBarChartActivity.this, String.format(getString(R.string.limite_selezione), MIN_ELEMENTS, MAX_ELEMENTS), Toast.LENGTH_LONG).show();
                                } else {
                                    selectedProvince = new ArrayList<>();
                                    for (List<ProvinceSelection> provinceList : provinceListMap.values()) {
                                        for (ProvinceSelection provinceSelection : provinceList) {
                                            if (provinceSelection.isSelected()) {
                                                selectedProvince.add(provinceSelection.getProvincia());
                                            }
                                        }
                                    }
                                    setData();
                                    dialog.dismiss();
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
            btnIndietro.setTextColor(Color.WHITE);
        } else {
            btnIndietro.setEnabled(false);
            btnIndietro.setTextColor(Color.DKGRAY);
        }

        if (cursore < dataLen - 1) {
            btnAvanti.setEnabled(true);
            btnAvanti.setTextColor(Color.WHITE);
        } else {
            btnAvanti.setEnabled(false);
            btnAvanti.setTextColor(Color.DKGRAY);
        }
    }

    private class ProvinceFormatter extends ValueFormatter {
        private static final int maxLen = 8;

        @Override
        public String getFormattedValue(float value) {
            if (value < selectedProvince.size()) {
                String nomeProvincia = selectedProvince.get((int) value);
                if (nomeProvincia.length() > maxLen) {
                    return String.format("%s.", nomeProvincia.substring(0, getMaxLength(nomeProvincia)));
                } else {
                    return nomeProvincia.substring(0, getMaxLength(nomeProvincia));
                }
            }
            return "";
        }

        private int getMaxLength(String nomeProvincia) {
            return Math.min(maxLen, nomeProvincia.length());
        }
    }
}
