package com.iacob.finder.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iacob.finder.R;
import com.iacob.finder.common.SharedItems;

import java.util.Locale;

public class EmotionsFragment extends RoundedSheetFragment {

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.happy_reader_layout, container);
        SharedItems items = new SharedItems(getContext());
        TextView happyprogress = view.findViewById(R.id.hpnsprcs);
        TextView sadprogress = view.findViewById(R.id.sdnprcs);
        float happiness = Math.abs(items.getHappiness() * 100);
        String formattedHappiness = String.format(Locale.ENGLISH,"%.1f", happiness);
        happyprogress.setText(String.format(Locale.ENGLISH, "%s",formattedHappiness  + " %"));
        float sadness = 100 - Math.abs(items.getHappiness() * 100);
        String formattedSadness = String.format(Locale.ENGLISH,"%.1f", sadness);
        sadprogress.setText(String.format(Locale.ENGLISH, "%s",formattedSadness  + " %"));
        ProgressBar happ = view.findViewById(R.id.happiness);
        happ.setProgress((int) happiness, true);
        ProgressBar sadd = view.findViewById(R.id.sadness);
        sadd.setProgress((int) (100-happiness), true);
        view.findViewById(R.id.dismisContainer).setOnClickListener(v -> dismiss());
        return view;
    }

}
