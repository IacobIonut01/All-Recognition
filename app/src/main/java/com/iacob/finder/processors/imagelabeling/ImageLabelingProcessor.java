// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.iacob.finder.processors.imagelabeling;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.iacob.finder.common.BitmapUtils;
import com.iacob.finder.common.CameraImageGraphic;
import com.iacob.finder.common.FrameMetadata;
import com.iacob.finder.common.GraphicOverlay;
import com.iacob.finder.common.SharedItems;
import com.iacob.finder.vision.VisionProcessorBase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ImageLabelingProcessor extends VisionProcessorBase<List<FirebaseVisionImageLabel>> {

    private static final String TAG = "ImageLabelingProcessor";

    private final FirebaseVisionImageLabeler detector;

    public ImageLabelingProcessor() {
        detector = FirebaseVision.getInstance().getOnDeviceImageLabeler();
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Text Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionImageLabel>> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionImageLabel> labels,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        new SharedItems(graphicOverlay.getContext()).resetChartData();
        ArrayList<PieEntry> entries = new ArrayList<>();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        for (int i = 0; i < (labels.size() >= 1 && labels.size() < 4 ? 1 : 4); ++i) {
            FirebaseVisionImageLabel label = labels.get(i);
            //Log.d(TAG, "cloud label: " + label.getText());
            if (label.getText() != null) {
                //labelsStr.add((label.getText()));
                entries.add(new PieEntry((int) (Math.abs(label.getConfidence()) * 100), label.getText()));
            }
        }
        entries.sort((o1, o2) -> Float.compare(o2.getValue(), o1.getValue()));
        new SharedItems(graphicOverlay.getContext()).addAllChartEntry(entries);
        LabelGraphic labelGraphic = new LabelGraphic(graphicOverlay, labels);
        graphicOverlay.add(labelGraphic);
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Label detection failed." + e);
    }

    @Override
    protected void drawPreview(@NonNull GraphicOverlay graphicOverlay, @Nullable Bitmap originalCameraImage) {
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.clear();
            graphicOverlay.add(imageGraphic);
            graphicOverlay.postInvalidate();
        }
    }
}
