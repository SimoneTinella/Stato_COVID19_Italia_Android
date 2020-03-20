package org.twistedappdeveloper.statocovid19italia;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.twistedappdeveloper.statocovid19italia.DataStorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.model.TrendValue;
import org.twistedappdeveloper.statocovid19italia.utils.TrendUtils;

import java.util.ArrayList;
import java.util.List;

public class BarChartActivity extends AppCompatActivity implements View.OnClickListener {

    private BarChart chart;
    private DataStorage dataStorage;
    private TextView txtMarkerData;

    private Button btnIndietro, btnAvanti, btnCambiaMisura;

    private int cursore;
    private int dataLen;

    private String selectedTrendKey;

    private TrendInfo[] trendInfoList;
    private String[] trendsName;
    private int checkedItem;

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
        btnCambiaMisura = findViewById(R.id.btnCambiaMisura);

        btnIndietro.setOnClickListener(this);
        btnAvanti.setOnClickListener(this);
        btnCambiaMisura.setOnClickListener(this);

        dataStorage = DataStorage.getIstance();
        dataLen = dataStorage.getRegionalDataStorageByDenRegione(dataStorage.getSubLevelDataKeys().get(0)).getDataLength();

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
        for (int i = 0; i < trendInfoListTmp.size(); i++) {
            int pos = TrendUtils.getPositionByTrendKey(trendInfoListTmp.get(i).getKey());
            trendInfoList[pos] = trendInfoListTmp.get(i);
            trendsName[pos] = trendInfoListTmp.get(i).getName();
        }

        cursore = getIntent().getIntExtra("cursore", dataLen - 1);
        selectedTrendKey = getIntent().getStringExtra("trendKey");

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
        l.setDrawInside(true);
        l.setYOffset(19f);
        l.setXOffset(-5f);

        btnEnableStatusCheck();

        setData(selectedTrendKey);
    }


    private void setData(String trendKey) {
        ArrayList<BarEntry> values = new ArrayList<>();

        int i = 0;
        for (String regione : dataStorage.getSubLevelDataKeys()) {
            DataStorage regionalDataStore = dataStorage.getRegionalDataStorageByDenRegione(regione);
            TrendValue trendValue = regionalDataStore.getTrendByKey(trendKey).getTrendValueByIndex(cursore);
            values.add(new BarEntry(i++, trendValue.getValue()));
            txtMarkerData.setText(String.format("Dati relativi al %s", trendValue.getDate()));
        }

        BarDataSet barDataSet;
        barDataSet = new BarDataSet(values, TrendUtils.getTrendNameByTrendKey(trendKey));
        barDataSet.setDrawIcons(false);
        barDataSet.setColor(TrendUtils.getColorByTrendKey(BarChartActivity.this, trendKey));

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);
        chart.setData(data);

        chart.animateY(10);
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
                builder.setTitle("Seleziona Misura");
                AlertDialog alert = builder.create();
                alert.show();
                break;
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

        if (cursore < dataLen - 1) {
            btnAvanti.setEnabled(true);
            btnAvanti.setTextColor(Color.WHITE);
        } else {
            btnAvanti.setEnabled(false);
            btnAvanti.setTextColor(Color.DKGRAY);
        }
    }

    private static class RegioniFormatter extends ValueFormatter {

        List<String> nomiRegioni = DataStorage.getIstance().getSubLevelDataKeys();
        private static final int maxLen = 10;

        @Override
        public String getFormattedValue(float value) {
            String nomeRegione = nomiRegioni.get((int) value);
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
}
