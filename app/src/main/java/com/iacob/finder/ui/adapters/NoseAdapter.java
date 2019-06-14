package com.iacob.finder.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.iacob.finder.R;
import com.iacob.finder.common.SharedItems;
import com.iacob.finder.ui.model.FeatureItem;
import com.iacob.finder.ui.model.NoseItem;

import java.util.List;

public class NoseAdapter extends RecyclerView.Adapter<NoseAdapter.ViewHolder> {

    private List<NoseItem> mList;

    public NoseAdapter(List<NoseItem> mList) {
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nose_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        NoseItem app = mList.get(position);
        holder.layoutView.setOnClickListener(v -> new SharedItems(holder.img.getContext()).setNoseID(app.getNoseID()));
        holder.img.setImageResource(app.getNoseID());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutView;
        ImageView img;

        ViewHolder(View view) {
            super(view);
            layoutView = view.findViewById(R.id.noseSelect);
            img = view.findViewById(R.id.nosePreview);
        }
    }

}