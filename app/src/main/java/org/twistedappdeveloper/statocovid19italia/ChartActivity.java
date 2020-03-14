package org.twistedappdeveloper.statocovid19italia;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.twistedappdeveloper.statocovid19italia.adapters.TrendsAdapter;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.model.TrendsSelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getColorByTrendKey;

public class ChartActivity extends AppCompatActivity implements OnChartValueSelectedListener, View.OnClickListener {
    private LineChart chart;
    private NationalDataStorage nationalDataStorage;
    private TextView txtMarkerData;

    private List<TrendsSelection> trendList;

    private int precIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chart);

        nationalDataStorage = NationalDataStorage.getIstance();
        txtMarkerData = findViewById(R.id.txtMarkerData);
        FloatingActionButton fabTrends = findViewById(R.id.fabTrends);

        fabTrends.setOnClickListener(this);

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
        chart.setBackgroundColor(Color.WHITE);

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

        trendList = new ArrayList<>();
        for (TrendInfo trendInfo : nationalDataStorage.getTrendsList()) {
            trendList.add(new TrendsSelection(trendInfo, isTrendSelected(trendInfo.getKey())));
        }
        Collections.sort(trendList);

        setData();
    }

    private boolean isTrendSelected(String key) {
        switch (key) {
            case "dimessi_guariti":
            case "totale_casi":
            case "deceduti":
                return true;

            default:
                return false;
        }
    }

    private void setData() {
        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(11f);
        data.setHighlightEnabled(true);
        chart.animateX(1000);

        for (TrendsSelection trendSelection : trendList) {
            if (trendSelection.isSelected()) {
                List<Entry> values = new ArrayList<>();
                for (int i = 0; i < trendSelection.getTrendInfo().getTrendValues().size(); i++) {
                    values.add(new Entry(i, trendSelection.getTrendInfo().getTrendValues().get(i).getValue()));
                }
                LineDataSet dataSet = new LineDataSet(values, trendSelection.getTrendInfo().getName());
                dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                dataSet.setColor(getColorByTrendKey(ChartActivity.this, trendSelection.getTrendInfo().getKey()));
                dataSet.setCircleColor(Color.BLACK);
                dataSet.setLineWidth(2f);
                dataSet.setCircleRadius(3f);
                dataSet.setFillAlpha(65);
                dataSet.setFillColor(Color.rgb(255, 131, 0));
                dataSet.setHighLightColor(Color.rgb(244, 117, 117));
                dataSet.setDrawCircleHole(false);
                dataSet.setDrawHorizontalHighlightIndicator(false);
                data.addDataSet(dataSet);
            }
        }

        chart.setData(data);
        onNothingSelected();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int xIndex = (int) e.getX();
        updateLegend(xIndex);
    }

    @Override
    public void onNothingSelected() {
        int index = nationalDataStorage.getDatiNazionaliLength() - 1;
        updateLegend(index);
    }

    private void updateLegend(int index) {
        txtMarkerData.setText(String.format("Dati relativi al %s", nationalDataStorage.getDateByIndex(index).substring(0, 10)));


        for (TrendsSelection trendSelection : trendList) {
            if (trendSelection.isSelected()) {
                ILineDataSet dataSetByLabel = chart.getLineData().getDataSetByLabel(
                        String.format("%s (%s)", trendSelection.getTrendInfo().getName(), trendSelection.getTrendInfo().getTrendValues().get(precIndex).getValue()),
                        false
                );
                if (dataSetByLabel == null) {
                    dataSetByLabel = chart.getLineData().getDataSetByLabel(trendSelection.getTrendInfo().getName(), false);
                }
                if (dataSetByLabel != null) {
                    dataSetByLabel
                            .setLabel(
                                    String.format("%s (%s)", trendSelection.getTrendInfo().getName(), trendSelection.getTrendInfo().getTrendValues().get(index).getValue()));
                }
            }
        }
        precIndex = index;
        chart.notifyDataSetChanged();
    }


    private int numberOfSelectedTrends() {
        int n = 0;
        for (TrendsSelection trendsSelection : trendList) {
            if (trendsSelection.isSelected()) {
                n++;
            }
        }
        return n;
    }

    @Override
    public void onClick(View v) {
        final Dialog dialog = new Dialog(ChartActivity.this, R.style.AppAlert);
        dialog.setContentView(R.layout.dialog_trends);

        final ListView listViewTrends = dialog.findViewById(R.id.listViewDialogTrends);
        Button btnSaveTrends = dialog.findViewById(R.id.btnCloseTrendDialog);

        final TrendsAdapter trendsAdapter = new TrendsAdapter(ChartActivity.this, R.layout.list_trends, trendList);
        listViewTrends.setAdapter(trendsAdapter);
        listViewTrends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (numberOfSelectedTrends() == 1 && trendList.get(position).isSelected()) {
                    Toast.makeText(ChartActivity.this, "Almeno uno deve essere selezionato", Toast.LENGTH_LONG).show();
                    return;
                }
                trendList.get(position).setSelected(!trendList.get(position).isSelected());
                trendsAdapter.notifyDataSetChanged();
            }
        });

        btnSaveTrends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                chart.getLineData().clearValues();
                chart.invalidate();
                chart.clear();
                setData();
            }
        });


        dialog.show();
    }
}
