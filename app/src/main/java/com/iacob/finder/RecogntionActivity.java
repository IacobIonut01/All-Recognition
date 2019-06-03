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
package com.iacob.finder;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fdev.progressview.ProgressView;
import com.google.android.gms.common.annotation.KeepName;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.iacob.finder.common.CameraSource;
import com.iacob.finder.common.CameraSourcePreview;
import com.iacob.finder.common.GraphicOverlay;
import com.iacob.finder.common.SharedItems;
import com.iacob.finder.processors.barcodescanning.BarcodeScanningProcessor;
import com.iacob.finder.processors.cloudimagelabeling.CloudImageLabelingProcessor;
import com.iacob.finder.processors.cloudlandmarkrecognition.CloudLandmarkRecognitionProcessor;
import com.iacob.finder.processors.cloudtextrecognition.CloudDocumentTextRecognitionProcessor;
import com.iacob.finder.processors.facedetection.FaceDetectionProcessor;
import com.iacob.finder.processors.imagelabeling.ImageLabelingProcessor;
import com.iacob.finder.processors.objectdetection.ObjectDetectorProcessor;
import com.iacob.finder.ui.LabelImageFragment;
import com.iacob.finder.ui.RecognisedTextFragment;
import com.iacob.finder.ui.SettingsFragment;
import com.iacob.finder.ui.adapters.FeaturesAdapter;
import com.iacob.finder.ui.model.FeatureItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Demo app showing the various features of ML Kit for Firebase. This class is used to
 * set up continuous frame processing on frames from a camera source.
 */
@KeepName
public final class RecogntionActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener {
    private static final String FACE_DETECTION = "Face Detection";
    private static final String OBJECT_DETECTION = "Object Detection";
    private static final String TEXT_DETECTION = "Text Detection";
    private static final String BARCODE_DETECTION = "Barcode Detection";
    private static final String IMAGE_LABEL_DETECTION = "Label Detection";
    private static final String LANDMARK_DETECTION = "Landmark Detection";
    private static final String TAG = "RecogntionActivity";
    private static final int PERMISSION_REQUESTS = 1;
    RecyclerView featureRecycler;
    List<FeatureItem> featureItems = new ArrayList<>();
    List<String> options = new ArrayList<>();
    List<String> images = new ArrayList<>();
    List<String> text = new ArrayList<>();
    List<String> summary = new ArrayList<>();
    CloudDocumentTextRecognitionProcessor cloudDocumentTextRecognitionProcessor = new CloudDocumentTextRecognitionProcessor();
    CloudImageLabelingProcessor cloudImageLabelingProcessor = new CloudImageLabelingProcessor();
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel = FACE_DETECTION;
    private boolean wasProcessing = false;

    private TextView mltips;
    ImageButton loadml;
    ProgressView loadingml;
    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            //Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        buildRecognition();

        mltips = findViewById(R.id.ml_tips);
        findViewById(R.id.settings).setOnClickListener(v -> {
            SettingsFragment textFragment = new SettingsFragment();
            textFragment.show(getSupportFragmentManager(), "se");
        });
        setTip("Press Search button to start processing...");
        RelativeLayout toobar = findViewById(R.id.toobar);
        CoordinatorLayout.LayoutParams prms = (CoordinatorLayout.LayoutParams) toobar.getLayoutParams();
        prms.topMargin = getStatusBarHeight();

        LinearLayout resultcontainer = findViewById(R.id.result_container);
        CoordinatorLayout.LayoutParams prms2 = (CoordinatorLayout.LayoutParams) resultcontainer.getLayoutParams();
        prms2.bottomMargin = getNavigationBarHeight();

        loadml = findViewById(R.id.loadml);
        SharedItems items = new SharedItems(this);
        loadingml = findViewById(R.id.loadingml);
        loadml.setOnClickListener(v -> {
            items.setShowGraphics(!items.shouldShowGraphics());
            loadingml.setVisibility(items.shouldShowGraphics() ? View.VISIBLE : View.GONE);
        });

        preview = findViewById(R.id.firePreview);
        preview.setOnClickListener(v -> {
            if (cameraSource != null && cameraSource.getFrameProcessor().equals(cloudDocumentTextRecognitionProcessor)) {
                if (!cameraSource.isProcessing())
                    cameraSource.startProcessing();
                if (cameraSource.isProcessing() && items.shouldShowGraphics()) {
                    RecognisedTextFragment textFragment = new RecognisedTextFragment();
                    textFragment.show(getSupportFragmentManager(), "re");
                }
            } else if (cameraSource != null && cameraSource.getFrameProcessor().equals(cloudImageLabelingProcessor)) {
                if (!cameraSource.isProcessing())
                    cameraSource.startProcessing();
                if (cameraSource.isProcessing() && items.shouldShowGraphics()) {
                    LabelImageFragment textFragment = new LabelImageFragment();
                    textFragment.show(getSupportFragmentManager(), "le");
                }
            }
            else if (cameraSource != null) {
                if (!cameraSource.isProcessing() && items.shouldShowGraphics())
                    cameraSource.startProcessing();
            }
        });

        graphicOverlay = findViewById(R.id.fireFaceOverlay);
        options.add(FACE_DETECTION);
        options.add(OBJECT_DETECTION);
        options.add(TEXT_DETECTION);
        options.add(BARCODE_DETECTION);
        options.add(IMAGE_LABEL_DETECTION);
        //options.add(LANDMARK_DETECTION);
        images.add("https://www.gstatic.com/mobilesdk/181112_mobilesdk/face_detection_with_contour@2x.png");
        images.add("https://www.gstatic.com/mobilesdk/190415_mobilesdk/object_detection@2x.png");
        images.add("https://www.gstatic.com/mobilesdk/180427_mobilesdk/mlkit/text_recognition@2x.png");
        images.add("https://www.gstatic.com/mobilesdk/180427_mobilesdk/mlkit/barcode_scanning@2x.png");
        images.add("https://www.gstatic.com/mobilesdk/180427_mobilesdk/mlkit/image_classification@2x.png");
        //images.add("https://www.gstatic.com/mobilesdk/180427_mobilesdk/mlkit/landmark_identification@2x.png");
        text.add(FACE_DETECTION);
        text.add(OBJECT_DETECTION);
        text.add(TEXT_DETECTION);
        text.add(BARCODE_DETECTION);
        text.add(IMAGE_LABEL_DETECTION);
        //text.add(LANDMARK_DETECTION);
        summary.add("Detect faces and facial landmarks, now with Face Contours");
        summary.add("Detect, track and classify objects in live camera and static images");
        summary.add("Recognize and extract text from images");
        summary.add("Scan and process barcodes");
        summary.add("Identify objects, locations, activities, animal species, products, and more");
        //summary.add("Identify popular landmarks in an image");

        for (int i = 0; i < options.size(); i++) {
            FeatureItem item = new FeatureItem(images.get(i), text.get(i), summary.get(i), options.get(i));
            item.setCls(v -> {
                selectedModel = item.getProcessorName();
                preview.stop();
                cameraSource.stop();
                new SharedItems(this).setShowGraphics(false);
                if (allPermissionsGranted()) {
                    createCameraSource(selectedModel);
                    startCameraSource();
                    cameraSource.stopProcessing();
                } else {
                    getRuntimePermissions();
                }
            });
            featureItems.add(item);
        }

        featureRecycler = findViewById(R.id.featuresRecycler);
        setupBottomSheet();
        featureRecycler.setAdapter(new FeaturesAdapter(featureItems));
        featureRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        ToggleButton facingSwitch = findViewById(R.id.facingSwitch);
        facingSwitch.setOnCheckedChangeListener(this);
        // Hide the toggle button if there is only 1 camera
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE);
        }

        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        } else {
            getRuntimePermissions();
        }
    }

    public void buildRecognition() {
        wasProcessing = false;
        SharedItems sharedItems = new SharedItems(this);
        sharedItems.setText("No text");
        sharedItems.resetChartData();
        sharedItems.setShowGraphics(wasProcessing);
    }

    public void resumeRecognition() {
        SharedItems sharedItems = new SharedItems(this);
        sharedItems.setShowGraphics(wasProcessing);
        if (cameraSource != null && cameraSource.getCamera() != null) {
            if (wasProcessing)
                cameraSource.startProcessing();
            else
                cameraSource.stopProcessing();
        }
    }

    public void pauseRecognition() {
        if (cameraSource != null && cameraSource.getCamera() != null) {
            wasProcessing = cameraSource.isProcessing();
        }
    }

    public void destoryRecognition() {
        wasProcessing = false;
        SharedItems sharedItems = new SharedItems(this);
        sharedItems.setText("No text");
        sharedItems.resetChartData();
        sharedItems.setShowGraphics(wasProcessing);
    }

    public void setupBottomSheet() {
        MaterialCardView sheet = findViewById(R.id.result_card);
        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) sheet.getLayoutParams();
        params2.bottomMargin = getNavigationBarHeight();
        sheet.setLayoutParams(params2);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(findViewById(R.id.result_container));
        int dp = 178;
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setPeekHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()) + getNavigationBarHeight());
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {

            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) featureRecycler.getLayoutParams();
                params.topMargin = (int) (getNavigationBarHeight() - getNavigationBarHeight() * v);
                featureRecycler.setLayoutParams(params);
                LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) sheet.getLayoutParams();
                params2.bottomMargin = (int) (getNavigationBarHeight() * v);
                sheet.setLayoutParams(params2);
            }
        });
    }

    @Override
    public void onBackPressed() {
        SharedItems sharedItems = new SharedItems(this);
        if (cameraSource != null && cameraSource.isProcessing()) {
            cameraSource.stopProcessing();
            startCameraSource();
            sharedItems.setText("No text");
            sharedItems.setShowGraphics(false);
            sharedItems.setResultFound(false);
            sharedItems.resetChartData();
            loadingml.setVisibility(View.GONE);
        } else if (loadingml.getVisibility() == View.VISIBLE && sharedItems.shouldShowGraphics()) {
            sharedItems.setText("No text");
            sharedItems.setShowGraphics(false);
            sharedItems.setResultFound(false);
            sharedItems.resetChartData();
            loadingml.setVisibility(View.GONE);
        } else
            super.onBackPressed();
    }

    public void setTip(CharSequence charSequence) {
        moveAndAnimate(mltips, "alpha", 1, 0, 400);
        mltips.setText(String.format("Tips : %s", charSequence));
        moveAndAnimate(mltips, "alpha", 0, 1, 400);
    }

    public void moveAndAnimate(View v, String direction, long start, long end, long duration) {
        ObjectAnimator mover = ObjectAnimator.ofFloat(v, direction, start, end);
        mover.setDuration(duration);
        mover.start();
    }

    public boolean hasNavBar() {
        int id = getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && getResources().getBoolean(id);
    }

    public int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
            }
        }
        preview.stop();
        startCameraSource();
    }

    private void createCameraSource(String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        try {
            switch (model) {
                case TEXT_DETECTION:
                    cameraSource.setMachineLearningFrameProcessor(cloudDocumentTextRecognitionProcessor);
                    if (mltips != null)
                        setTip("Click on screen to copy text");
                    break;
                case FACE_DETECTION:
                    cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor(this, getResources()));
                    if (mltips != null)
                        setTip("Face Detection shows you when you smile");
                    break;
                case OBJECT_DETECTION:
                    FirebaseVisionObjectDetectorOptions objectDetectorOptions =
                            new FirebaseVisionObjectDetectorOptions.Builder()
                                    .setDetectorMode(FirebaseVisionObjectDetectorOptions.STREAM_MODE)
                                    .enableClassification().build();
                    cameraSource.setMachineLearningFrameProcessor(
                            new ObjectDetectorProcessor(objectDetectorOptions));
                    if (mltips != null)
                        setTip("Track and identify a bowl of cereals");
                    break;
                case BARCODE_DETECTION:
                    cameraSource.setMachineLearningFrameProcessor(new BarcodeScanningProcessor());
                    if (mltips != null)
                        setTip("Get a bag of chips and scan it");
                    break;
                case IMAGE_LABEL_DETECTION:
                    cameraSource.setMachineLearningFrameProcessor(cloudImageLabelingProcessor);
                    if (mltips != null)
                        setTip("Try to recognise some objects in your room");
                    break;
                case LANDMARK_DETECTION:
                    cameraSource.setMachineLearningFrameProcessor(new CloudLandmarkRecognitionProcessor());
                    break;
                default:
                    Log.e(TAG, "Unknown model: " + model);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + model, e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
        resumeRecognition();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        pauseRecognition();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destoryRecognition();
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
