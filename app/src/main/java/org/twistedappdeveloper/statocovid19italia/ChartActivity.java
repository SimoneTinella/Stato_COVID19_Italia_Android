package org.twistedappdeveloper.statocovid19italia;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.BarLineChartTouchListener;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.twistedappdeveloper.statocovid19italia.adapters.TrendsAdapter;
import org.twistedappdeveloper.statocovid19italia.datastorage.DataStorage;
import org.twistedappdeveloper.statocovid19italia.model.RowData;
import org.twistedappdeveloper.statocovid19italia.model.TrendInfo;
import org.twistedappdeveloper.statocovid19italia.model.TrendValue;
import org.twistedappdeveloper.statocovid19italia.model.TrendsSelection;
import org.twistedappdeveloper.statocovid19italia.utils.TrendUtils;
import org.twistedappdeveloper.statocovid19italia.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.twistedappdeveloper.statocovid19italia.utils.TrendUtils.getColorByTrendKey;

public class ChartActivity extends AppCompatActivity implements OnChartValueSelectedListener, View.OnClickListener, OnChartGestureListener {
    private LineChart chart;
    private DataStorage dataStorage;
    private TextView txtMarkerData, txtContesto;

    private List<TrendsSelection> trendList;
    private List<TrendsSelection> trendListTmp;

    private int precIndex = 0;
    private FloatingActionButton fabResetZoom;

    private String contestoDati, contestoDatiProvince;

    private ImageButton btnDisplayValues, btnContextProvince;

    private boolean displayValuesOnChart = false;

    private static final int animDuration = 600;

    private SharedPreferences preferences;

    private void updateContext() {
        dataStorage = DataStorage.getIstance().getDataStorageByDataContext(contestoDati);
        txtContesto.setText(String.format(getString(R.string.andamento), contestoDati));

        if (!contestoDati.equals(contestoDatiProvince)) {
            dataStorage = dataStorage.getDataStorageByDataContext(contestoDatiProvince);
            txtContesto.setText(String.format(getString(R.string.andamento), contestoDatiProvince));
        }

        if (trendList != null && trendList.size() == dataStorage.getTrendsList().size()) {
            List<TrendsSelection> trendListNew = new ArrayList<>();
            for (TrendsSelection trendsSelection : trendList) {
                TrendInfo newTrendInfo = dataStorage.getTrendByKey(trendsSelection.getTrendInfo().getKey());
                if (newTrendInfo != null) {
                    trendsSelection.setTrendInfo(newTrendInfo);
                    trendListNew.add(trendsSelection);
                }
            }
            trendList = trendListNew;
        } else {
            trendList = new ArrayList<>();
            for (TrendInfo trendInfo : dataStorage.getTrendsList()) {
                trendList.add(new TrendsSelection(trendInfo, isTrendSelected(trendInfo.getKey())));
            }
            Collections.sort(trendList);
        }

        checkProvinceButton();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chart);

        preferences = getApplicationContext().getSharedPreferences("default", 0);

        txtContesto = findViewById(R.id.txtContesto);
        txtMarkerData = findViewById(R.id.txtMarkerData);
        ImageButton btnTrends = findViewById(R.id.btnTrends);
        ImageButton btnChangeContext = findViewById(R.id.btnChangeContext);
        btnDisplayValues = findViewById(R.id.btnDisplayValues);
        btnContextProvince = findViewById(R.id.btnContextProvince);
        fabResetZoom = findViewById(R.id.fabResetZoom);
        fabResetZoom.setVisibility(View.INVISIBLE);

        contestoDati = getIntent().getStringExtra(Utils.DATACONTEXT_KEY);
        contestoDatiProvince = contestoDati;
        updateContext();

        btnTrends.setOnClickListener(this);
        btnChangeContext.setOnClickListener(this);
        fabResetZoom.setOnClickListener(this);
        btnDisplayValues.setOnClickListener(this);
        btnContextProvince.setOnClickListener(this);

        chart = findViewById(R.id.chart1);
        chart.setOnChartValueSelectedListener(this);

        chart.setTouchEnabled(true);
        chart.setDragDecelerationFrictionCoef(0.9f);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);
        chart.setBackgroundColor(Color.TRANSPARENT);
        chart.setOnChartGestureListener(this);

        chart.setMarker(new ChartActivityMaker(ChartActivity.this));

        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.SQUARE);
        l.setTextSize(12f);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
        l.setXOffset(getSPDimension(4));

        chart.getDescription().setEnabled(false);

        DataFormatter dataFormatter = new DataFormatter();
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(11f);

        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setYOffset(-0.001f);
        xAxis.setValueFormatter(dataFormatter);

        YAxis rightAxis = chart.getAxisRight();

        rightAxis.setDrawGridLines(true);
        rightAxis.setGranularityEnabled(true);

        chart.getAxisLeft().setEnabled(false);

        l.setTextColor(getResources().getColor(R.color.textColor));
        rightAxis.setTextColor(getResources().getColor(R.color.textColor));
        xAxis.setTextColor(getResources().getColor(R.color.textColor));


        setData(animDuration);
    }

    private boolean isTrendSelected(String key) {
        switch (key) {
            case DataStorage.TOTALE_ATTUALMENTE_POSITIVI_KEY:
            case DataStorage.NUOVI_POSITIVI_KEY:
                return true;

            default:
                return false;
        }
    }

    private void setData(int animDuration) {
        LineData data = new LineData();

        boolean isMinimumZero = true;

        for (TrendsSelection trendSelection : trendList) {
            if (trendSelection.isSelected()) {
                List<Entry> values = new ArrayList<>();
                for (int i = 0; i < trendSelection.getTrendInfo().getTrendValues().size(); i++) {
                    int value = trendSelection.getTrendInfo().getTrendValueByIndex(i).getValue();
                    values.add(new Entry(i, value));
                    if (value < 0) {
                        isMinimumZero = false;
                    }
                }
                LineDataSet dataSet = new LineDataSet(values, trendSelection.getTrendInfo().getName());
                dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                dataSet.setColor(getColorByTrendKey(ChartActivity.this, trendSelection.getTrendInfo().getKey()));
                dataSet.setCircleColor(getResources().getColor(R.color.chartTextColor));
                dataSet.setLineWidth(1.8f);
                dataSet.setCircleRadius(2f);
                dataSet.setFillAlpha(65);
                dataSet.setFillColor(Color.rgb(255, 131, 0));
                dataSet.setHighLightColor(Color.rgb(244, 117, 117));
                dataSet.setDrawCircleHole(false);
                dataSet.setDrawHorizontalHighlightIndicator(false);
                data.addDataSet(dataSet);
            }
        }

        if (isMinimumZero) {
            chart.getAxisLeft().setAxisMinimum(0);
            chart.getAxisRight().setAxisMinimum(0);
        } else {
            chart.getAxisLeft().resetAxisMinimum();
            chart.getAxisRight().resetAxisMinimum();
        }

        chart.setData(data);

        if (displayValuesOnChart) {
            data.setValueTextColor(getResources().getColor(R.color.chartTextColor));
            data.setValueTextSize(9.5f);
            data.setDrawValues(true);
        } else {
            data.setDrawValues(false);
        }

        data.setHighlightEnabled(true);
        chart.animateX(animDuration);

        onNothingSelected();
        checkChangeVisibilityButton();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int xIndex = (int) e.getX();
        updateLegend(xIndex);
    }

    @Override
    public void onNothingSelected() {
        int index = dataStorage.getDataLength() - 1;
        updateLegend(index);
    }

    private void updateLegend(int index) {
        txtMarkerData.setText(String.format(getString(R.string.dati_relativi_al), dataStorage.getFullDateStringByIndex(index)));


        for (TrendsSelection trendSelection : trendList) {
            if (trendSelection.isSelected()) {
                ILineDataSet dataSetByLabel = chart.getLineData().getDataSetByLabel(
                        String.format("%s (%s)", trendSelection.getTrendInfo().getName(), trendSelection.getTrendInfo().getTrendValueByIndex(precIndex).getValue()),
                        false
                );
                if (dataSetByLabel == null) {
                    dataSetByLabel = chart.getLineData().getDataSetByLabel(trendSelection.getTrendInfo().getName(), false);
                }
                if (dataSetByLabel != null) {
                    dataSetByLabel
                            .setLabel(
                                    String.format("%s (%s)", trendSelection.getTrendInfo().getName(), trendSelection.getTrendInfo().getTrendValueByIndex(index).getValue()));
                }
            }
        }
        precIndex = index;
        chart.notifyDataSetChanged();
    }


    private int numberOfSelectedTrends(List<TrendsSelection> trendList) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(ChartActivity.this);
        AlertDialog alert;
        int checkedItem = 0;
        switch (v.getId()) {
            case R.id.btnTrends:
                final Dialog dialog = new Dialog(ChartActivity.this, R.style.AppAlert);
                dialog.setContentView(R.layout.dialog_trends_sec);

                final ListView listViewTrends = dialog.findViewById(R.id.listViewDialogTrends);
                final Button btnSaveTrends = dialog.findViewById(R.id.btnCloseTrendDialog);
                final Button btnSelectAllTrends = dialog.findViewById(R.id.btnSelectAll);
                final Button btnDeselectAllTrends = dialog.findViewById(R.id.btnDeselectAll);

                trendListTmp = new ArrayList<>();
                for (TrendsSelection trendsSelection : trendList) {
                    try {
                        trendListTmp.add((TrendsSelection) trendsSelection.clone());
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
                final TrendsAdapter trendsAdapter = new TrendsAdapter(ChartActivity.this, R.layout.list_trends, trendListTmp);
                listViewTrends.setAdapter(trendsAdapter);
                listViewTrends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        trendListTmp.get(position).setSelected(!trendListTmp.get(position).isSelected());
                        trendsAdapter.notifyDataSetChanged();
                    }
                });

                View.OnClickListener clickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        switch (v.getId()) {
                            case R.id.btnCloseTrendDialog:
                                if (numberOfSelectedTrends(trendListTmp) == 0) {
                                    Toast.makeText(ChartActivity.this, getString(R.string.seleziona_almeno_un_elem), Toast.LENGTH_LONG).show();
                                } else {
                                    trendList = trendListTmp;
                                    dialog.dismiss();
                                    chart.getLineData().clearValues();
                                    chart.invalidate();
                                    chart.clear();
                                    setData(animDuration);
                                    resetZoom();
                                }
                                break;
                            case R.id.btnSelectAll:
                                for (TrendsSelection trendsSelection : trendListTmp) {
                                    trendsSelection.setSelected(true);
                                }
                                trendsAdapter.notifyDataSetChanged();
                                break;
                            case R.id.btnDeselectAll:
                                for (TrendsSelection trendsSelection : trendListTmp) {
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
            case R.id.btnChangeContext:
                List<String> regioni = new ArrayList<>();
                regioni.add(DataStorage.defaultDataContext);
                regioni.addAll(DataStorage.getIstance().getSubLevelDataKeys());
                final String[] dataContexs = regioni.toArray(new String[0]);
                for (int i = 0; i < dataContexs.length; i++) {
                    if (dataContexs[i].equalsIgnoreCase(contestoDati)) {
                        checkedItem = i;
                        break;
                    }
                }
                builder.setSingleChoiceItems(dataContexs, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        contestoDati = dataContexs[which];
                        contestoDatiProvince = contestoDati;
                        updateContext();
                        setData(animDuration);
                    }
                });
                builder.setTitle(getResources().getString(R.string.sel_dataContext));
                alert = builder.create();
                alert.show();
                break;
            case R.id.btnDisplayValues:
                displayValuesOnChart = !displayValuesOnChart;
                setData(0);
                break;
            case R.id.btnContextProvince:
                List<String> province = new ArrayList<>();
                if (contestoDati.equals(contestoDatiProvince)) {
                    province.add(dataStorage.getDataContext());
                    province.addAll(dataStorage.getSubLevelDataKeys());
                } else {
                    DataStorage dataStorageRegionale = DataStorage.getIstance().getDataStorageByDataContext(contestoDati);
                    province.add(dataStorageRegionale.getDataContext());
                    province.addAll(dataStorageRegionale.getSubLevelDataKeys());
                }
                province.remove("In fase di definizione/aggiornamento");
                final String[] provinceContexs = province.toArray(new String[0]);

                for (int i = 0; i < provinceContexs.length; i++) {
                    if (provinceContexs[i].equalsIgnoreCase(contestoDatiProvince)) {
                        checkedItem = i;
                        break;
                    }
                }
                builder.setSingleChoiceItems(provinceContexs, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        contestoDatiProvince = provinceContexs[which];
                        updateContext();
                        setData(animDuration);
                    }
                });
                builder.setTitle(getResources().getString(R.string.sel_dataContext_sub));
                alert = builder.create();
                alert.show();
                break;
        }
    }

    private void checkProvinceButton() {
        if (dataStorage.getDataContextScope().equals(DataStorage.Scope.NAZIONALE)) {
            btnContextProvince.setEnabled(false);
            btnContextProvince.setImageResource(R.drawable.baseline_location_city_gray_24);
        } else {
            if (dataStorage.getDataContextScope().equals(DataStorage.Scope.REGIONALE)) {
                if (dataStorage.getSubLevelDataKeys().size() > 2) {
                    btnContextProvince.setEnabled(true);
                    btnContextProvince.setImageResource(R.drawable.baseline_location_city_white_24);
                } else {
                    btnContextProvince.setEnabled(false);
                    btnContextProvince.setImageResource(R.drawable.baseline_location_city_gray_24);
                }
            } else {
                btnContextProvince.setEnabled(true);
                btnContextProvince.setImageResource(R.drawable.baseline_location_city_white_24);
            }
        }
    }

    private void checkChangeVisibilityButton() {
        if (isValuesVisualizable()) {
            btnDisplayValues.setEnabled(true);
            if (!displayValuesOnChart) {
                btnDisplayValues.setImageResource(R.drawable.baseline_visibility_off_white_24);
            } else {
                btnDisplayValues.setImageResource(R.drawable.baseline_visibility_white_24);
            }
        } else {
            btnDisplayValues.setEnabled(false);
            btnDisplayValues.setImageResource(R.drawable.baseline_visibility_off_gray_24);
        }
    }

    private boolean isValuesVisualizable() {
        return chart.getData().getEntryCount() < chart.getMaxVisibleCount()
                * chart.getViewPortHandler().getScaleX();
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
        checkChangeVisibilityButton();
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
        checkChangeVisibilityButton();
    }

    private float getSPDimension(int value) {
        Resources r = getApplicationContext().getResources();
        return TypedValue.applyDimension(
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
            txtTitle.setText(getString(R.string.variazioni_giorno_prec));
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
                    TrendValue currentTrendValue = trendInfo.getTrendValueByIndex(position);
                    TrendValue precTrendValue;
                    if (position > 0) {
                        precTrendValue = trendInfo.getTrendValueByIndex(position - 1);
                    } else {
                        precTrendValue = new TrendValue(0, "NoData");
                    }

                    RowData rowData = new RowData(
                            trendInfo.getName(),
                            currentTrendValue.getValue() - precTrendValue.getValue(),
                            getColorByTrendKey(ChartActivity.this, trendInfo.getKey()),
                            TrendUtils.getPositionByTrendKey(trendInfo.getKey()),
                            trendInfo.getKey()
                    );

                    View child = inflater.inflate(R.layout.list_data_marker, null);
                    TextView txtName = child.findViewById(R.id.txtName);
                    txtName.setText(String.format("Diff. %s", rowData.getName()));
                    TextView txtValue = child.findViewById(R.id.txtValue);
                    txtValue.setText(String.format("%s", rowData.getValue()));
                    txtValue.setTextColor(rowData.getColor());
                    linearLayoutMarker.addView(child);

                }
            }
            super.refreshContent(e, highlight);
        }
    }

    private static class DataFormatter extends ValueFormatter {

        DataStorage dataStorage = DataStorage.getIstance();

        @Override
        public String getFormattedValue(float value) {
            return dataStorage.getSimpleDateStringByIndex((int) value);
        }

    }
}
