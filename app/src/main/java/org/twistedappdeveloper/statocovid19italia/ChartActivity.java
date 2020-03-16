package org.twistedappdeveloper.statocovid19italia;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.twistedappdeveloper.statocovid19italia.DataStorage.NationalDataStorage;
import org.twistedappdeveloper.statocovid19italia.adapters.TrendsAdapter;
import org.twistedappdeveloper.statocovid19italia.model.Data;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.model.TrendValue;
import org.twistedappdeveloper.statocovid19italia.model.TrendsSelection;
import org.twistedappdeveloper.statocovid19italia.utils.TrendUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getColorByTrendKey;

public class ChartActivity extends AppCompatActivity implements OnChartValueSelectedListener, View.OnClickListener, OnChartGestureListener {
    private LineChart chart;
    private NationalDataStorage nationalDataStorage;
    private TextView txtMarkerData;

    private List<TrendsSelection> trendList;

    private int precIndex = 0;
    FloatingActionButton fabResetZoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chart);

        nationalDataStorage = NationalDataStorage.getIstance();
        txtMarkerData = findViewById(R.id.txtMarkerData);
        FloatingActionButton fabTrends = findViewById(R.id.fabTrends);
        fabResetZoom = findViewById(R.id.fabResetZoom);
        fabResetZoom.setVisibility(View.INVISIBLE);

        fabTrends.setOnClickListener(this);
        fabResetZoom.setOnClickListener(this);

        chart = findViewById(R.id.chart1);
        chart.setOnChartValueSelectedListener(this);

        DisplayMetrics ds = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(ds);
        int width = ds.widthPixels;
        Description description = chart.getDescription();
        description.setText("Andamento Nazionale");
        description.setTextSize(15f);
        description.setPosition(width-getSPDimension(45), getSPDimension(15));

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
        chart.setOnChartGestureListener(this);

        chart.setMarker(new ChartActivityMaker(ChartActivity.this));

        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.SQUARE);
        l.setTextSize(12f);
        l.setTextColor(Color.BLACK);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
        l.setYOffset(6f);
        l.setXOffset(getSPDimension(4));

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

        chart.getAxisLeft().setEnabled(false);

        trendList = new ArrayList<>();
        for (TrendInfo trendInfo : nationalDataStorage.getTrendsList()) {
            trendList.add(new TrendsSelection(trendInfo, isTrendSelected(trendInfo.getKey())));
        }
        Collections.sort(trendList);

        setData();
    }

    private boolean isTrendSelected(String key) {
        switch (key) {
            case NationalDataStorage.C_NUOVI_DIMESSI_GUARITI:
            case NationalDataStorage.TOTALE_ATTUALMENTE_POSITIVI_KEY:
            case NationalDataStorage.C_NUOVI_DECEDUTI:
            case NationalDataStorage.C_NUOVI_POSITIVI:
                return true;

            default:
                return false;
        }
    }

    private void setData() {
        LineData data = new LineData();

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

        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(10f);
        data.setHighlightEnabled(true);
        chart.animateX(1000);

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

        switch (v.getId()) {
            case R.id.fabTrends:
                final Dialog dialog = new Dialog(ChartActivity.this, R.style.AppAlert);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_trends);

                final ListView listViewTrends = dialog.findViewById(R.id.listViewDialogTrends);
                final Button btnSaveTrends = dialog.findViewById(R.id.btnCloseTrendDialog);
                final Button btnSelectAllTrends = dialog.findViewById(R.id.btnSelectAll);
                final Button btnDeselectAllTrends = dialog.findViewById(R.id.btnDeselectAll);

                final TrendsAdapter trendsAdapter = new TrendsAdapter(ChartActivity.this, R.layout.list_trends, trendList);
                listViewTrends.setAdapter(trendsAdapter);
                listViewTrends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        trendList.get(position).setSelected(!trendList.get(position).isSelected());
                        trendsAdapter.notifyDataSetChanged();
                    }
                });

                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        switch (v.getId()) {
                            case R.id.btnCloseTrendDialog:
                                if (numberOfSelectedTrends() == 0) {
                                    Toast.makeText(ChartActivity.this, "Seleziona almeno un elemento", Toast.LENGTH_LONG).show();
                                } else {
                                    dialog.dismiss();
                                    chart.getLineData().clearValues();
                                    chart.invalidate();
                                    chart.clear();
                                    resetZoom();
                                    setData();
                                }
                                break;
                            case R.id.btnSelectAll:
                                for (TrendsSelection trendsSelection : trendList) {
                                    trendsSelection.setSelected(true);
                                }
                                trendsAdapter.notifyDataSetChanged();
                                break;
                            case R.id.btnDeselectAll:
                                for (TrendsSelection trendsSelection : trendList) {
                                    trendsSelection.setSelected(false);
                                }
                                trendsAdapter.notifyDataSetChanged();
                                break;
                        }
                    }
                };

                btnSaveTrends.setOnClickListener(clickListener);
                btnSelectAllTrends.setOnClickListener(clickListener);
                btnDeselectAllTrends.setOnClickListener(clickListener);
                dialog.show();
                break;

            case R.id.fabResetZoom:
                resetZoom();
                break;
        }
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        if (chart.getScaleX() <= 1.05f && chart.getScaleY() <= 1.05f) {
            resetZoom();
        } else {
            fabResetZoom.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        fabResetZoom.setVisibility(View.VISIBLE);
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    private void resetZoom() {
        fabResetZoom.setVisibility(View.INVISIBLE);
        BarLineChartTouchListener onTouchListener = (BarLineChartTouchListener) chart.getOnTouchListener();
        onTouchListener.stopDeceleration();
        chart.fitScreen();
    }

    private float getSPDimension(int value){
        Resources r = getApplicationContext().getResources();
        return  TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                value,
                r.getDisplayMetrics()
        );
    }

    public class ChartActivityMaker extends MarkerView {

        private LinearLayout linearLayoutMarker;

        public ChartActivityMaker(Context context) {
            super(context, R.layout.chart_marker);
            linearLayoutMarker = findViewById(R.id.linearLayoutMarker);
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

            linearLayoutMarker.removeAllViews();
            LayoutParams lparams = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            Resources r = getApplicationContext().getResources();
            lparams.bottomMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    5,
                    r.getDisplayMetrics()
            );
            int margin = (int) getSPDimension(5);
            lparams.leftMargin = margin;
            lparams.rightMargin = margin;
            TextView txtTitle = new TextView(ChartActivity.this);
            txtTitle.setLayoutParams(lparams);
            txtTitle.setText("Variazioni con il giorno precedente");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                txtTitle.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            }
            txtTitle.setGravity(CENTER_HORIZONTAL);
            txtTitle.setTextColor(Color.BLACK);
            txtTitle.setTypeface(txtTitle.getTypeface(), Typeface.BOLD);
            linearLayoutMarker.addView(txtTitle);


            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for (TrendsSelection trendsSelection : trendList) {
                if (trendsSelection.isSelected()) {
                    TrendInfo trendInfo = trendsSelection.getTrendInfo();
                    TrendValue currentTrendValue = trendInfo.getTrendValues().get(position);
                    TrendValue precTrendValue;
                    if (position > 0) {
                        precTrendValue = trendInfo.getTrendValues().get(position - 1);
                    } else {
                        precTrendValue = new TrendValue(0, "NoData");
                    }

                    Data data = new Data(
                            trendInfo.getName(),
                            String.format("%s", currentTrendValue.getValue() - precTrendValue.getValue()),
                            getColorByTrendKey(ChartActivity.this, trendInfo.getKey()),
                            TrendUtils.getPositionByTrendKey(trendInfo.getKey())
                    );

                    View child = inflater.inflate(R.layout.list_data_marker, null);
                    TextView txtName = child.findViewById(R.id.txtName);
                    txtName.setText(String.format("Diff. %s", data.getName()));
                    TextView txtValue = child.findViewById(R.id.txtValue);
                    txtValue.setText(data.getValue());
                    txtValue.setTextColor(data.getColor());
                    linearLayoutMarker.addView(child);

                }
            }
            super.refreshContent(e, highlight);
        }

    }
}
