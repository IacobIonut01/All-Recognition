package com.iacob.finder;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
import com.iacob.finder.common.GraphicOverlay;
import com.iacob.finder.common.SharedItems;
import com.iacob.finder.processors.barcodescanning.BarcodeScanningProcessor;
import com.iacob.finder.processors.facedetection.FaceDetectionProcessor;
import com.iacob.finder.processors.imagelabeling.ImageLabelingProcessor;
import com.iacob.finder.processors.objectdetection.ObjectDetectorProcessor;
import com.iacob.finder.processors.textrecognition.TextRecognitionProcessor;
import com.iacob.finder.ui.AIFragment;
import com.iacob.finder.ui.EmotionsFragment;
import com.iacob.finder.ui.LabelImageFragment;
import com.iacob.finder.ui.RecognisedTextFragment;
import com.iacob.finder.ui.ScannedBardcodeFragment;
import com.iacob.finder.ui.SettingsFragment;
import com.iacob.finder.ui.adapters.FeaturesAdapter;
import com.iacob.finder.ui.model.FeatureItem;
import com.iacob.finder.vision.VisionCPU;
import com.otaliastudios.cameraview.Audio;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Mode;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.SizeSelectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@KeepName
public final class RecognitionActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        CompoundButton.OnCheckedChangeListener {
    private static final String FACE_DETECTION = "Face Detection";
    private static final String OBJECT_DETECTION = "Object Detection";
    private static final String TEXT_DETECTION = "Text Detection";
    private static final String BARCODE_DETECTION = "Barcode Detection";
    private static final String IMAGE_LABEL_DETECTION = "Label Detection";
    private static final String TAG = "RecognitionActivity";
    private static final int PERMISSION_REQUESTS = 1;
    private RecyclerView featureRecycler;
    private List<FeatureItem> featureItems = new ArrayList<>();
    private List<String> options = new ArrayList<>();
    private List<String> images = new ArrayList<>();
    private List<String> summary = new ArrayList<>();
    private FaceDetectionProcessor faceDetectionProcessor;
    private BarcodeScanningProcessor barcodeScanningProcessor = new BarcodeScanningProcessor();
    private TextRecognitionProcessor cloudDocumentTextRecognitionProcessor = new TextRecognitionProcessor();
    private ImageLabelingProcessor cloudImageLabelingProcessor = new ImageLabelingProcessor();
    private GraphicOverlay graphicOverlay;
    private String selectedModel = FACE_DETECTION;
    private boolean wasProcessing = false;

    private TextView mltips;
    private ProgressView loadingml;

    private VisionCPU visionCPU;
    private CameraView camera;

    private ProgressBar mProgress;
    private ImageView img;

    private MaterialCardView rotateCard;
    private LinearLayout rotateLayout;
    private Bitmap localBitmap;
    private Bitmap takenBitmap;
    private float trotation = 0;
    private float rotation = 0;

    private ToggleButton flashSwitch;
    BottomSheetBehavior behavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        faceDetectionProcessor = new FaceDetectionProcessor(this, getResources());

        options.add(FACE_DETECTION);
        //options.add(OBJECT_DETECTION);
        options.add(TEXT_DETECTION);
        options.add(BARCODE_DETECTION);
        options.add(IMAGE_LABEL_DETECTION);
        images.add("https://www.gstatic.com/mobilesdk/181112_mobilesdk/face_detection_with_contour@2x.png");
        //images.add("https://www.gstatic.com/mobilesdk/190415_mobilesdk/object_detection@2x.png");
        images.add("https://www.gstatic.com/mobilesdk/180427_mobilesdk/mlkit/text_recognition@2x.png");
        images.add("https://www.gstatic.com/mobilesdk/180427_mobilesdk/mlkit/barcode_scanning@2x.png");
        images.add("https://www.gstatic.com/mobilesdk/180427_mobilesdk/mlkit/image_classification@2x.png");
        summary.add("Detect faces and facial landmarks, now with Face Contours");
        //summary.add("Detect, track and classify objects in live camera and static images");
        summary.add("Recognize and extract text from images");
        summary.add("Scan and process barcodes");
        summary.add("Identify objects, locations, activities, animal species, products, and more");

        rotateCard = findViewById(R.id.rotateContainer);
        rotateLayout = findViewById(R.id.rotateLayout);
        mProgress = findViewById(R.id.progressBar2);
        img = findViewById(R.id.imageView6);
        mltips = findViewById(R.id.ml_tips);
        camera = findViewById(R.id.camera);
        ImageButton loadml = findViewById(R.id.loadml);
        loadingml = findViewById(R.id.loadingml);
        featureRecycler = findViewById(R.id.featuresRecycler);
        camera.setAudio(Audio.OFF);
        camera.setLifecycleOwner(this);
        camera.setMode(Mode.PICTURE);
        camera.setPictureSize(SizeSelectors.and(SizeSelectors.maxHeight(getDisplayHeight()), SizeSelectors.maxWidth(getDisplayWidth())));
        graphicOverlay = findViewById(R.id.fireFaceOverlay);
        visionCPU = new VisionCPU(this, graphicOverlay);
        rotateCard.setVisibility(View.GONE);
        flashSwitch = findViewById(R.id.flashSwitch);
        flashSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            camera.setFlash(isChecked ? Flash.ON : Flash.OFF);
            flashSwitch.setBackgroundResource(isChecked ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
        });
        buildRecognition();

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

        SharedItems items = new SharedItems(this);
        loadml.setOnClickListener(v -> {
            if (!items.shouldShowGraphics()) {
                camera.takePicture();
                items.setShowGraphics(true);
            } else {
                onBackPressed();
            }
        });

        Switch processAllSwitch = findViewById(R.id.processAllSwitch);
        processAllSwitch.setChecked(new SharedItems(this).shouldProcessAll());
        processAllSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> new SharedItems(this).setProcessAll(isChecked));

        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                camera.setVisibility(View.GONE);
                result.toBitmap(bitmap -> {
                    camera.setVisibility(View.GONE);
                    visionCPU.process(flipBitmap(bitmap, camera.getFacing()), mProgress);
                    img.setImageBitmap(flipBitmap(bitmap, camera.getFacing()));
                    localBitmap = null;
                    takenBitmap = bitmap;
                    rotateCard.setVisibility(View.VISIBLE);
                });
            }
        });

        rotateLayout.setOnClickListener(v -> {
            if (mProgress.getVisibility() == View.INVISIBLE) {
                if (localBitmap != null) {
                    rotation += 90;
                    Bitmap rotatedbitmap = rotateBitmap(localBitmap, rotation);
                    img.setImageBitmap(rotatedbitmap);
                    visionCPU.process(rotatedbitmap, mProgress);
                } else if (takenBitmap != null) {
                    trotation += 90;
                    Bitmap rotatedbitmapT = rotateBitmap(takenBitmap, trotation);
                    img.setImageBitmap(rotatedbitmapT);
                    visionCPU.process(rotatedbitmapT, mProgress);
                }
            }
        });

        findViewById(R.id.usePhoto).setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
        });

        img.setOnClickListener(v -> {
            if (items.shouldShowGraphics() && mProgress.getVisibility() == View.INVISIBLE) {
                if (visionCPU.getFrameProcessor().equals(cloudDocumentTextRecognitionProcessor)) {
                    RecognisedTextFragment textFragment = new RecognisedTextFragment();
                    textFragment.show(getSupportFragmentManager(), "re");
                } else if (visionCPU.getFrameProcessor().equals(cloudImageLabelingProcessor)) {
                    LabelImageFragment textFragment = new LabelImageFragment();
                    textFragment.show(getSupportFragmentManager(), "le");
                } else if (visionCPU.getFrameProcessor().equals(barcodeScanningProcessor)) {
                    ScannedBardcodeFragment barcodeFragment = new ScannedBardcodeFragment();
                    barcodeFragment.show(getSupportFragmentManager(), "be");
                } else if (visionCPU.getFrameProcessor().equals(faceDetectionProcessor)) {
                    EmotionsFragment emotionsFragment = new EmotionsFragment();
                    emotionsFragment.show(getSupportFragmentManager(), "em");
                } else if (items.shouldProcessAll()) {
                    AIFragment aiFragment = new AIFragment();
                    aiFragment.show(getSupportFragmentManager(), "ai");
                }
            }
        });

        for (int i = 0; i < options.size(); i++) {
            FeatureItem item = new FeatureItem(images.get(i), options.get(i), summary.get(i), options.get(i));
            item.setCls(v -> {
                selectedModel = item.getProcessorName();
                new SharedItems(this).setShowGraphics(false);
                if (allPermissionsGranted()) {
                    createCameraSource(selectedModel);
                } else {
                    getRuntimePermissions();
                }
            });
            featureItems.add(item);
        }

        setupBottomSheet();
        featureRecycler.setAdapter(new FeaturesAdapter(featureItems));
        featureRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        ToggleButton facingSwitch = findViewById(R.id.facingSwitch);
        facingSwitch.setOnCheckedChangeListener(this);
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE);
        }

        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        } else {
            getRuntimePermissions();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            if (requestCode == 1) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    camera.setVisibility(View.GONE);
                    takenBitmap = null;
                    localBitmap = bitmap;
                    img.setImageBitmap(bitmap);
                    new SharedItems(this).setShowGraphics(true);
                    visionCPU.process(bitmap, mProgress);
                    rotateCard.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    Log.i("TAG", "Exception" + e);
                }
            }
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public Bitmap flipBitmap(Bitmap source, Facing facing) {
        switch (facing) {
            case BACK:
                return source;
            case FRONT:
                Matrix matrix = new Matrix();
                matrix.preScale(-1.0f, 1.0f);
                return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
            default:
                return source;
        }
    }

    public void setupBottomSheet() {
        MaterialCardView sheet = findViewById(R.id.result_card);
        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) sheet.getLayoutParams();
        params2.bottomMargin = getNavigationBarHeight();
        sheet.setLayoutParams(params2);;
        behavior = BottomSheetBehavior.from(findViewById(R.id.result_container));
        int dp = 196;
        LinearLayout hiddable = findViewById(R.id.hiddableContainer);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setPeekHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()) + getNavigationBarHeight());
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {

            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) hiddable.getLayoutParams();
                params.topMargin = (int) (getNavigationBarHeight() - getNavigationBarHeight() * v);
                hiddable.setLayoutParams(params);
                LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) sheet.getLayoutParams();
                params2.bottomMargin = (int) (getNavigationBarHeight() * v);
                sheet.setLayoutParams(params2);
            }
        });
    }

    @Override
    public void onBackPressed() {
        SharedItems sharedItems = new SharedItems(this);
        if (sharedItems.shouldShowGraphics()) {
            sharedItems.setText("No text");
            sharedItems.setBarcode("Invalid barcode");
            sharedItems.setShowGraphics(false);
            sharedItems.setResultFound(false);
            sharedItems.setHappiness(0);
            sharedItems.resetChartData();
            camera.setVisibility(View.VISIBLE);
            loadingml.setVisibility(View.GONE);
            img.setImageResource(0);
            rotateCard.setVisibility(View.GONE);
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

    public int getDisplayHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public int getDisplayWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (visionCPU != null) {
            if (isChecked) {
                camera.setFacing(Facing.FRONT);
                visionCPU.setFacing(CameraSource.CAMERA_FACING_FRONT);
                flashSwitch.setVisibility(View.GONE);
            } else {
                camera.setFacing(Facing.BACK);
                visionCPU.setFacing(CameraSource.CAMERA_FACING_BACK);
                flashSwitch.setVisibility(View.VISIBLE);
            }
        }
    }

    private void createCameraSource(String model) {
        if (visionCPU == null)
            visionCPU = new VisionCPU(this, graphicOverlay);
        try {
            switch (model) {
                case TEXT_DETECTION:
                    visionCPU.setMachineLearningFrameProcessor(cloudDocumentTextRecognitionProcessor);
                    setTip("Click on screen to copy text");
                    break;
                case FACE_DETECTION:
                    visionCPU.setMachineLearningFrameProcessor(faceDetectionProcessor);
                    setTip("Face Detection shows you when you smile");
                    break;
                case OBJECT_DETECTION:
                    FirebaseVisionObjectDetectorOptions objectDetectorOptions =
                            new FirebaseVisionObjectDetectorOptions.Builder()
                                    .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
                                    .enableClassification().build();
                    visionCPU.setMachineLearningFrameProcessor(new ObjectDetectorProcessor(objectDetectorOptions));
                    setTip("Track and identify a bowl of cereals");
                    break;
                case BARCODE_DETECTION:
                    visionCPU.setMachineLearningFrameProcessor(barcodeScanningProcessor);
                    setTip("Get a bag of chips and scan it");
                    break;
                case IMAGE_LABEL_DETECTION:
                    visionCPU.setMachineLearningFrameProcessor(cloudImageLabelingProcessor);
                    setTip("Try to recognise some objects in your room");
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

    public void buildRecognition() {
        wasProcessing = false;
        SharedItems sharedItems = new SharedItems(this);
        sharedItems.setText("No text");
        sharedItems.setBarcode("Invalid barcode");
        sharedItems.setHappiness(0);
        sharedItems.resetChartData();
        sharedItems.setShowGraphics(wasProcessing);
    }

    public void destroyRecognition() {
        wasProcessing = false;
        SharedItems sharedItems = new SharedItems(this);
        sharedItems.setText("No text");
        sharedItems.setHappiness(0);
        sharedItems.resetChartData();
        sharedItems.setShowGraphics(wasProcessing);
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayout hiddable = findViewById(R.id.hiddableContainer);
        if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) hiddable.getLayoutParams();
            params.topMargin = getNavigationBarHeight();
            hiddable.setLayoutParams(params);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyRecognition();
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

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
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
