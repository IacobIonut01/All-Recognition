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
import com.iacob.finder.ui.model.FeatureItem;

import java.util.List;

public class FeaturesAdapter extends RecyclerView.Adapter<FeaturesAdapter.ViewHolder> {

    private List<FeatureItem> mList;

    public FeaturesAdapter( List<FeatureItem> mList) {
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.features_card, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        FeatureItem app = mList.get(position);
        holder.layoutView.setOnClickListener(app.getCls());
        holder.title.setText(app.getText());
        holder.summary.setText(app.getSummary());
        Glide.with(holder.layoutView)
                .load(app.getImg_url())
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutView;
        ImageView img;
        TextView title, summary;

        ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.ft_title);
            summary = view.findViewById(R.id.ft_summary);
            layoutView = view.findViewById(R.id.ft_layout);
            img = view.findViewById(R.id.ft_img);
        }
    }

}