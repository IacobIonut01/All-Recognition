package com.iacob.finder.ui;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.iacob.finder.R;
import com.iacob.finder.common.SharedItems;

import java.util.ArrayList;
import java.util.Locale;

public class AIFragment extends RoundedSheetFragment {

    @Override
    public void dismiss() {
        super.dismiss();
        new SharedItems(getContext()).resetChartData();
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.ai_reader_layut, container);
        EditText recognisedTextContainer = view.findViewById(R.id.recognisedText);
        recognisedTextContainer.setText(new SharedItems(getContext()).getText());
        recognisedTextContainer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(new SharedItems(getContext()).getText()))
                    recognisedTextContainer.setText(new SharedItems(getContext()).getText());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        EditText scannedBarcode = view.findViewById(R.id.scannedBarcode);
        scannedBarcode.setText(new SharedItems(getContext()).getBarcode());
        scannedBarcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(new SharedItems(getContext()).getBarcode()))
                    scannedBarcode.setText(new SharedItems(getContext()).getBarcode());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        SharedItems items = new SharedItems(getContext());
        TextView happyprogress = view.findViewById(R.id.hpnsprcs);
        TextView sadprogress = view.findViewById(R.id.sdnprcs);
        float hp = items.getHappiness() * 100;
        float happiness = Float.valueOf(String.format(Locale.ENGLISH, "%.0f", hp));
        happyprogress.setText(String.format(Locale.ENGLISH, "%.0f",happiness)  + " %");
        float sadness = 100 - happiness;
        sadprogress.setText(String.format(Locale.ENGLISH, "%.0f",sadness) + " %");
        ProgressBar happ = view.findViewById(R.id.happiness);
        happ.setProgress((int) happiness, true);
        ProgressBar sadd = view.findViewById(R.id.sadness);
        sadd.setProgress((int) (100-happiness), true);
        PieChart chart = view.findViewById(R.id.labelChart);
        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.google_sans);
        Typeface typefaceBold = ResourcesCompat.getFont(getContext(), R.font.google_sans_bold);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(false);
        chart.setRotationAngle(0);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        view.findViewById(R.id.dismisContainer).setOnClickListener(v -> dismiss());
        ArrayList<PieEntry> entries = new SharedItems(getContext()).getChartData();
        PieDataSet dataSet = new PieDataSet(entries, "Image Results");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(chart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.DKGRAY);
        data.setValueTypeface(typefaceBold);
        chart.setEntryLabelColor(Color.DKGRAY);
        chart.setEntryLabelTypeface(typeface);
        chart.setData(data);
        chart.highlightValues(null);
        chart.invalidate();
        view.findViewById(R.id.dismisContainer).setOnClickListener(v -> dismiss());
        LinearLayout text = view.findViewById(R.id.rtext);
        text.setVisibility(items.getText().equals("No text found") ? View.GONE : View.VISIBLE);
        LinearLayout barcode = view.findViewById(R.id.scanb);
        barcode.setVisibility(items.getBarcode().equals("Invalid barcode") ? View.GONE : View.VISIBLE);
        LinearLayout emotions = view.findViewById(R.id.emo);
        emotions.setVisibility(!items.foundFaces() ? View.GONE : View.VISIBLE);
        LinearLayout content = view.findViewById(R.id.cnt);
        content.setVisibility(items.getChartData() == null ? View.GONE : View.VISIBLE);
        return view;
    }

}
