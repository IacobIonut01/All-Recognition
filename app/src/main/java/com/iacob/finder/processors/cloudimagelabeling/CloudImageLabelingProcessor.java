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
package com.iacob.finder.processors.cloudimagelabeling;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.iacob.finder.common.BitmapUtils;
import com.iacob.finder.common.CameraImageGraphic;
import com.iacob.finder.common.FrameMetadata;
import com.iacob.finder.common.GraphicOverlay;
import com.iacob.finder.common.SharedItems;
import com.iacob.finder.vision.VisionProcessorBase;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Cloud Label Detector Demo.
 */
public class CloudImageLabelingProcessor extends VisionProcessorBase<List<FirebaseVisionImageLabel>> {

    private static final String TAG = "CloudImgLabelProc";

    private final FirebaseVisionImageLabeler detector;

    public CloudImageLabelingProcessor() {
        FirebaseVisionCloudImageLabelerOptions.Builder optionsBuilder = new FirebaseVisionCloudImageLabelerOptions.Builder();

        detector = FirebaseVision.getInstance().getCloudImageLabeler(optionsBuilder.build());
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
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay,
                    originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        //Log.d(TAG, "cloud label size: " + labels.size());
        List<String> labelsStr = new ArrayList<>();
        for (int i = 0; i < 4; ++i) {
            FirebaseVisionImageLabel label = labels.get(i);
            //Log.d(TAG, "cloud label: " + label.getText());
            if (label.getText() != null) {
                //labelsStr.add((label.getText()));
                entries.add(new PieEntry((int) (Math.abs(label.getConfidence()) * 100), label.getText()));
            }
        }
        entries.sort((o1, o2) -> Float.compare(o2.getValue(), o1.getValue()));
        new SharedItems(graphicOverlay.getContext()).addAllChartEntry(entries);
        CloudLabelGraphic cloudLabelGraphic = new CloudLabelGraphic(graphicOverlay, labelsStr);
        graphicOverlay.add(cloudLabelGraphic);
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Cloud Label detection failed " + e);
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
