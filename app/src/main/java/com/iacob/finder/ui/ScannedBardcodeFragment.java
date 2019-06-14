package com.iacob.finder.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iacob.finder.R;
import com.iacob.finder.common.SharedItems;

public class ScannedBardcodeFragment extends RoundedSheetFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.barcode_reader_layout, container);
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
        view.findViewById(R.id.dismisContainer).setOnClickListener(v -> dismiss());
        return view;
    }

}
