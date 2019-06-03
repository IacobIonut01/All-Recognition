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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iacob.finder.R;
import com.iacob.finder.common.SharedItems;
import com.iacob.finder.ui.adapters.NoseAdapter;
import com.iacob.finder.ui.model.NoseItem;

import java.util.ArrayList;

public class SettingsFragment extends RoundedSheetFragment {

    public int[] noseIDs = {
            R.drawable.clown_nose,
            R.drawable.big_nose_svgrepo_com,
            R.drawable.christmas_tree_svgrepo_com,
            R.drawable.fox_svgrepo_com
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.settings_layout, container);
        RecyclerView noseRecylcer = view.findViewById(R.id.noseRecycler);
        ArrayList<NoseItem> items = new ArrayList<>();
        for (int noseID : noseIDs) {
            items.add(new NoseItem(noseID));
        }
        NoseAdapter adapter = new NoseAdapter(items);
        noseRecylcer.setAdapter(adapter);
        noseRecylcer.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        view.findViewById(R.id.dismisContainer).setOnClickListener(v -> dismiss());
        return view;
    }

}
