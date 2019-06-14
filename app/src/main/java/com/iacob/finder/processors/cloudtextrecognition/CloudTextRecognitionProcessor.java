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
package com.iacob.finder.processors.cloudtextrecognition;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.iacob.finder.common.BitmapUtils;
import com.iacob.finder.common.CameraImageGraphic;
import com.iacob.finder.common.FrameMetadata;
import com.iacob.finder.common.GraphicOverlay;
import com.iacob.finder.common.SharedItems;
import com.iacob.finder.vision.VisionProcessorBase;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Processor for the cloud text detector demo.
 */
public class CloudTextRecognitionProcessor extends VisionProcessorBase<FirebaseVisionText> {

    private static final String TAG = "CloudTextRecProc";

    private final FirebaseVisionTextRecognizer detector;

    public CloudTextRecognitionProcessor() {
        super();
        detector = FirebaseVision.getInstance().getCloudTextRecognizer();
    }

    @Override
    protected Task<FirebaseVisionText> detectInImage(FirebaseVisionImage image) {
        return detector.processImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FirebaseVisionText text,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay,
                    originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        if (text == null) {
            return; // TODO: investigate why this is needed
        }
        List<FirebaseVisionText.TextBlock> blocks = text.getTextBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int l = 0; l < elements.size(); l++) {
                    CloudTextGraphic cloudTextGraphic = new CloudTextGraphic(graphicOverlay,
                            elements.get(l));
                    graphicOverlay.add(cloudTextGraphic);
                }
            }
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.w(TAG, "Cloud Text detection failed." + e);
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
