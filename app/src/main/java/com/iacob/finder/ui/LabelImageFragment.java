package com.iacob.finder.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class LabelImageFragment extends RoundedSheetFragment {

    @Override
    public void dismiss() {
        super.dismiss();
        new SharedItems(getContext()).resetChartData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.image_label_layout, container);

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
        return view;
    }


}
