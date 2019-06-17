package com.iacob.finder.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
        float hp = items.getHappiness() * 100;
        float happiness = Float.valueOf(String.format(Locale.ENGLISH, "%.0f", hp));
        happyprogress.setText(String.format(Locale.ENGLISH, "%.0f",happiness)  + " %");
        float sadness = 100 - happiness;
        sadprogress.setText(String.format(Locale.ENGLISH, "%.0f",sadness) + " %");
        ProgressBar happ = view.findViewById(R.id.happiness);
        happ.setProgress((int) happiness, true);
        ProgressBar sadd = view.findViewById(R.id.sadness);
        sadd.setProgress((int) (100-happiness), true);
        LinearLayout emotionsLayout = view.findViewById(R.id.emos);
        LinearLayout notfoundLayout = view.findViewById(R.id.no_face);
        if (items.foundFaces()) {
            emotionsLayout.setVisibility(View.VISIBLE);
            notfoundLayout.setVisibility(View.GONE);
        } else {
            emotionsLayout.setVisibility(View.GONE);
            notfoundLayout.setVisibility(View.VISIBLE);
        }
        view.findViewById(R.id.dismisContainer).setOnClickListener(v -> dismiss());
        return view;
    }

}
