package org.twistedappdeveloper.statocovid19italia;

import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import org.twistedappdeveloper.statocovid19italia.DataStorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.utils.TrendUtils;

import java.util.ArrayList;

public class BarChartActivity extends AppCompatActivity {

    private BarChart chart;
    private DataStorage dataStorage;
    private TextView txtMarkerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        txtMarkerData = findViewById(R.id.txtMarkerData);
        chart = findViewById(R.id.barChart);

        dataStorage = DataStorage.getIstance();

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

        ValueFormatter xAxisFormatter = new RegioniFormatter();
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(21);
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.setLabelRotationAngle(90);

        setData();
    }

    private void setData() {
        ArrayList<BarEntry> values = new ArrayList<>();

        //FIXME: simulazione dataset
        for (int i = 0; i < 21; i++) {
            float val = (float) (Math.random() * (10));
            values.add(new BarEntry(i, val));

            BarDataSet barDataSet;
            barDataSet = new BarDataSet(values, "Totale casi");
            barDataSet.setDrawIcons(false);
            barDataSet.setColor(TrendUtils.getColorByTrendKey(BarChartActivity.this, "totale_casi"));

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(barDataSet);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);
            chart.setData(data);
        }
    }

    private class RegioniFormatter extends ValueFormatter {

        @Override
        public String getFormattedValue(float value) {
            return "Lomb.";
        }
    }
}
