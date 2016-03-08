package co.mainmethod.fame.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.jakewharton.rxbinding.view.RxView;

import java.io.IOException;

import co.mainmethod.fame.FameApp;
import co.mainmethod.fame.R;
import co.mainmethod.fame.object.Picture;
import co.mainmethod.fame.ui.view.CameraSourcePreview;
import co.mainmethod.fame.ui.view.FaceGraphic;
import co.mainmethod.fame.ui.view.GraphicOverlay;
import co.mainmethod.fame.util.DiskIOUtil;
import co.mainmethod.fame.util.PrefUtil;
import timber.log.Timber;

/**
 * User interface for the image capture
 * Created by evan on 1/30/16.
 */
public class CaptureFragment extends Fragment {

    private static final int RC_HANDLE_GMS = 9001;

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay faceOverlay;

    public CaptureFragment() {

    }

    public static Fragment newInstance() {
        return new CaptureFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_capture, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preview = (CameraSourcePreview) view.findViewById(R.id.preview);
        faceOverlay = (GraphicOverlay) view.findViewById(R.id.faceOverlay);
        RxView.clicks(view.findViewById(R.id.shutter)).subscribe(aVoid -> takePicture());
        RxView.clicks(view.findViewById(R.id.load)).subscribe(aVoid -> loadFromDisk());
        RxView.clicks(view.findViewById(R.id.flipCamera)).subscribe(aVoid -> flipCamera());
        createCameraSource();
    }

    /**
     * Restarts the camera.
     */
    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    public void onPause() {
        super.onPause();
        stopPreview();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyCameraSource();
    }

    private void flipCamera() {
        PrefUtil.setUseFrontFacingCamera(getActivity(), !PrefUtil.useFrontFacingCamera(getActivity()));
        stopPreview();
        destroyCameraSource();
        createCameraSource();
        startCameraSource();
    }

    private void takePicture() {
        cameraSource.takePicture(this::playShutterSound, this::savePicture);
    }

    private void loadFromDisk() {
        // TODO load image from disk and load the next fragment
    }

    private void playShutterSound() {
        Timber.d("Playing shutter sound");
        // TODO play shutter sound
    }

    private void savePicture(byte[] bytes) {
        Timber.d("Picture bytes: %d", bytes.length);

        // write the picture to disk, temporarily
        String filename = DiskIOUtil.writeTempPicture(bytes, "temo.jpg");

        if (TextUtils.isEmpty(filename)) {
            Timber.e("Unable to save the image");
            return;
        }
        Picture picture = new Picture();
        picture.filename = filename;
        picture.fameFaces = faceOverlay.getFameFaces();

        // start the next fragment
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, EditPhotoFragment.newInstance(picture))
                .commit();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {
        Context context = FameApp.getApp(getActivity());
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            Timber.w("FameFace detector dependencies are not yet available.");
        }

        cameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(1050, 1440)
                .setAutoFocusEnabled(true)
                .setFacing(PrefUtil.useFrontFacingCamera(getActivity()) ? CameraSource.CAMERA_FACING_FRONT :
                        CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build();
    }

    private void destroyCameraSource() {

        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    private void stopPreview() {
        preview.stop();
    }

    private void startCameraSource() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(FameApp.getApp(getActivity()));

        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance()
                    .getErrorDialog(getActivity(), code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (cameraSource != null) {

            try {
                preview.start(cameraSource, faceOverlay);
            } catch (IOException e) {
                Timber.e(e, "Unable to start camera source");
                destroyCameraSource();
            }
        }
    }

    //==============================================================================================
    // Graphic FameFace Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {

        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(faceOverlay);
        }
    }

    /**
     * FameFace tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay overlay;
        private FaceGraphic faceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            this.overlay = overlay;
            faceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            faceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            overlay.add(faceGraphic);
            faceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            overlay.remove(faceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            overlay.remove(faceGraphic);
        }
    }
}
