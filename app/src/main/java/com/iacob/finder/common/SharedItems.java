package com.iacob.finder.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.mikephil.charting.data.PieEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iacob.finder.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SharedItems {

    private SharedPreferences prefs;
    private ArrayList<PieEntry> list = new ArrayList<>();

    public SharedItems(Context context) {
        prefs = context.getSharedPreferences("utils", MODE_PRIVATE);
    }

    public void setText(CharSequence text) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("recognisedText", text.toString());
        editor.apply();
    }

    public String getText() {
        return prefs.getString("recognisedText", "No text");
    }

    public void setBarcode(CharSequence text) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("recognisedCode", text.toString());
        editor.apply();
    }

    public String getBarcode() {
        return prefs.getString("recognisedCode", "Invalid barcode");
    }

    public void setNoseID(int id) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("noseID", id);
        editor.apply();
    }

    public int getNoseID() {
        return prefs.getInt("noseID", R.drawable.clown_nose);
    }

    public void setHappiness(float id) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("happiness", id);
        editor.apply();
    }

    public float getHappiness() {
        return prefs.getFloat("happiness", 0f);
    }

    public void setShowGraphics(boolean show) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("showGraphics", show);
        editor.apply();
    }

    public boolean shouldShowGraphics() {
        return prefs.getBoolean("showGraphics", false);
    }

    public void setProcessAll(boolean all) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("shouldProcessAll", all);
        editor.apply();
    }

    public boolean shouldProcessAll() {
        return prefs.getBoolean("shouldProcessAll", false);
    }

    public void setFacesFound(boolean found) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("foundFaces", found);
        editor.apply();
    }

    public boolean foundFaces() {
        return prefs.getBoolean("foundFaces", false);
    }

    public void setResultFound(boolean show) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("resultText", show);
        editor.apply();
    }

    public boolean isResultFound() {
        return prefs.getBoolean("resultText", false);
    }

    public void addChartEntry(int value, String label) {
        String httpParamJSONList = prefs.getString("chartData", "");
        list = new Gson().fromJson(httpParamJSONList, new TypeToken<List<PieEntry>>() {}.getType());
        if (list != null) {
            list.add(new PieEntry(value, label));
        } else {
            list = new ArrayList<>();
            list.add(new PieEntry(value, label));
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("chartData", new Gson().toJson(list));
        editor.apply();
    }

    public void addAllChartEntry(ArrayList<PieEntry> entries) {
        list = entries;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("chartData", new Gson().toJson(list));
        editor.apply();
    }

    public ArrayList<PieEntry> getChartData() {
        String httpParamJSONList = prefs.getString("chartData", "");
        return new Gson().fromJson(httpParamJSONList, new TypeToken<List<PieEntry>>() {}.getType());
    }

    public void resetChartData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("chartData", "");
        editor.apply();
    }

}
