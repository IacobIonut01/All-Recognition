package com.iacob.finder.ui.model;

import android.view.View;

public class FeatureItem {

    private String img_url, text, summary, processorName;
    private View.OnClickListener cls;

    public FeatureItem(String img_url, String text, String summary, String processorName) {
        this.img_url = img_url;
        this.text = text;
        this.summary = summary;
        this.processorName = processorName;
    }

    public View.OnClickListener getCls() {
        return cls;
    }

    public String getSummary() {
        return summary;
    }

    public String getImg_url() {
        return img_url;
    }

    public String getProcessorName() {
        return processorName;
    }

    public String getText() {
        return text;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCls(View.OnClickListener cls) {
        this.cls = cls;
    }
}
