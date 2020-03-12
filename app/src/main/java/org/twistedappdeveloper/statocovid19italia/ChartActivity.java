package org.twistedappdeveloper.statocovid19italia;

import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    private LineChart chart;
    private DataStorage dataStorage;
    private TextView txtMarkerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chart);

        dataStorage = DataStorage.getIstance();
        txtMarkerData = findViewById(R.id.txtMarkerData);

        chart = findViewById(R.id.chart1);
        chart.setOnChartValueSelectedListener(this);
        // no description text
        chart.getDescription().setEnabled(false);
        // enable touch gestures
        chart.setTouchEnabled(true);
        chart.setDragDecelerationFrictionCoef(0.9f);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

//        MyMarkerView myMarkerView = new MyMarkerView(getApplicationContext());
//        chart.setMarker(myMarkerView);

        // set an alternative background color
//        chart.setBackgroundColor(Color.rgb(240, 240, 240));
        chart.setBackgroundColor(Color.WHITE);

        chart.animateX(1000);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.BLACK);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
        l.setYOffset(5f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setYOffset(-0.001f);


        YAxis leftAxis = chart.getAxisRight();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = chart.getAxisLeft();
        rightAxis.setEnabled(false);

        setData();
    }

    private void setData() {
        YAxis leftAxis = chart.getAxisRight();

        ArrayList<Entry> values1 = new ArrayList<>();
        ArrayList<Entry> values2 = new ArrayList<>();
        ArrayList<Entry> values3 = new ArrayList<>();

        JSONArray datiNazionaliJSON = dataStorage.getDatiNazionaliJson();

        try {
            leftAxis.setAxisMaximum(dataStorage.getDatiNazionaliJson().getJSONObject(dataStorage.getDatiNazionaliJson().length() - 1).getInt("totale_casi") + 1000);
            for (int i = 0; i < datiNazionaliJSON.length(); i++) {
                try {
                    int totale_casi = datiNazionaliJSON.getJSONObject(i).getInt("totale_casi");
                    int deceduti = datiNazionaliJSON.getJSONObject(i).getInt("deceduti");
                    int dimessi_guariti = datiNazionaliJSON.getJSONObject(i).getInt("dimessi_guariti");

                    values1.add(new Entry(i, totale_casi));
                    values2.add(new Entry(i, deceduti));
                    values3.add(new Entry(i, dimessi_guariti));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        LineDataSet set1, set2, set3;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) chart.getData().getDataSetByIndex(1);
            set3 = (LineDataSet) chart.getData().getDataSetByIndex(2);
            set1.setValues(values1);
            set2.setValues(values2);
            set3.setValues(values3);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values1, "Totale Casi");

            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set1.setColor(Color.rgb(255, 131, 0));
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setFillAlpha(65);
            set1.setFillColor(Color.rgb(255, 131, 0));
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);
            //set1.setFillFormatter(new MyFillFormatter(0f));
            set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            set2 = new LineDataSet(values2, "Deceduti");
            set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set2.setColor(Color.RED);
            set2.setCircleColor(Color.BLACK);
            set2.setLineWidth(2f);
            set2.setCircleRadius(3f);
            set2.setFillAlpha(65);
            set2.setFillColor(Color.RED);
            set2.setDrawCircleHole(false);
            set2.setHighLightColor(Color.rgb(244, 117, 117));
            set2.setDrawHorizontalHighlightIndicator(false);
            //set2.setFillFormatter(new MyFillFormatter(900f));

            set3 = new LineDataSet(values3, "Guariti");
            set3.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set3.setColor(Color.GREEN);
            set3.setCircleColor(Color.BLACK);
            set3.setLineWidth(2f);
            set3.setCircleRadius(3f);
            set3.setFillAlpha(65);
            set3.setFillColor(ColorTemplate.colorWithAlpha(Color.GREEN, 200));
            set3.setDrawCircleHole(false);
            set3.setHighLightColor(Color.rgb(244, 117, 117));
            set3.setDrawHorizontalHighlightIndicator(false);

            // create a data object with the data sets
            LineData data = new LineData(set1, set2, set3);
            data.setValueTextColor(Color.BLACK);
            data.setValueTextSize(9f);
            data.setHighlightEnabled(true);

            // set data
            chart.setData(data);
            onNothingSelected();
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int xIndex = (int) e.getX();
        updateLegend(xIndex);
    }

    @Override
    public void onNothingSelected() {
        updateLegend(dataStorage.getDatiNazionaliJson().length() - 1);
    }


    private void updateLegend(int index) {

        try {
            JSONObject datiNazionaliObj = dataStorage.getDatiNazionaliJson().getJSONObject(index);
            txtMarkerData.setText(String.format("Dati relativi al %s", datiNazionaliObj.getString("data").substring(0, 10)));

            int i = 0;
            for (ILineDataSet dataSet : chart.getLineData().getDataSets()) {
                switch (i++) {
                    case 0:
                        dataSet.setLabel(String.format("%s (%s)", dataSet.getLabel().split("\\(")[0].trim(), datiNazionaliObj.getString("totale_casi")));
                        break;
                    case 1:
                        dataSet.setLabel(String.format("%s (%s)", dataSet.getLabel().split("\\(")[0].trim(), datiNazionaliObj.getString("deceduti")));
                        break;
                    case 2:
                        dataSet.setLabel(String.format("%s (%s)", dataSet.getLabel().split("\\(")[0].trim(), datiNazionaliObj.getString("dimessi_guariti")));
                        break;
                }

            }
            chart.notifyDataSetChanged();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
