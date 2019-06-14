package com.iacob.finder.vision;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.widget.ProgressBar;

import com.iacob.finder.common.GraphicOverlay;
import com.iacob.finder.common.SharedItems;
import com.iacob.finder.common.VisionImageProcessor;
import com.iacob.finder.processors.barcodescanning.BarcodeScanningProcessor;
import com.iacob.finder.processors.facedetection.FaceDetectionProcessor;
import com.iacob.finder.processors.imagelabeling.ImageLabelingProcessor;
import com.iacob.finder.processors.textrecognition.TextRecognitionProcessor;

public class VisionCPU {

    private BarcodeScanningProcessor barcodeScanningProcessor = new BarcodeScanningProcessor();
    private TextRecognitionProcessor cloudDocumentTextRecognitionProcessor = new TextRecognitionProcessor();
    private ImageLabelingProcessor cloudImageLabelingProcessor = new ImageLabelingProcessor();

    public static final int CAMERA_FACING_BACK = Camera.CameraInfo.CAMERA_FACING_BACK;
    public static final int CAMERA_FACING_FRONT = Camera.CameraInfo.CAMERA_FACING_FRONT;
    protected int facing = CAMERA_FACING_BACK;

    private Activity activity;
    private VisionImageProcessor frameProcessor;
    private GraphicOverlay graphicOverlay;

    public VisionCPU(Activity activity, GraphicOverlay graphicOverlay) {
        this.activity = activity;
        this.graphicOverlay = graphicOverlay;
    }

    public void setMachineLearningFrameProcessor(VisionImageProcessor processor) {
        cleanScreen();
        if (frameProcessor != null) {
            frameProcessor.stop();
        }
        frameProcessor = processor;
    }

    public void setFacing(int facing) {
        if ((facing != CAMERA_FACING_BACK) && (facing != CAMERA_FACING_FRONT)) {
            throw new IllegalArgumentException("Invalid camera: " + facing);
        }
        this.facing = facing;
    }


    public VisionImageProcessor getFrameProcessor() {
        return frameProcessor;
    }

    private void cleanScreen() {
        graphicOverlay.clear();
    }

    public void process(Bitmap data, ProgressBar progressBar) {
        SharedItems items = new SharedItems(activity);
        if (!items.shouldProcessAll())
            frameProcessor.process(data, graphicOverlay, progressBar);
        else {
            setMachineLearningFrameProcessor(cloudDocumentTextRecognitionProcessor);
            frameProcessor.process(data, graphicOverlay, progressBar);
            setMachineLearningFrameProcessor(new FaceDetectionProcessor(activity, activity.getResources()));
            frameProcessor.process(data, graphicOverlay, progressBar);
            setMachineLearningFrameProcessor(barcodeScanningProcessor);
            frameProcessor.process(data, graphicOverlay, progressBar);
            setMachineLearningFrameProcessor(cloudImageLabelingProcessor);
            frameProcessor.process(data, graphicOverlay, progressBar);
        }
    }

}
