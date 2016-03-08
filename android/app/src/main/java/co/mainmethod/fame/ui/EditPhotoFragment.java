package co.mainmethod.fame.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import org.parceler.Parcels;

import java.io.File;

import co.mainmethod.fame.R;
import co.mainmethod.fame.object.Picture;
import timber.log.Timber;

/**
 * Created by evanhalley on 2/3/16.
 */
public class EditPhotoFragment extends Fragment implements View.OnLayoutChangeListener {

    private static final String PICTURE = "picture";

    private ImageView image;
    private RelativeLayout facesContainer;
    private Picture pictureData;

    public static Fragment newInstance(Picture picture) {
        Bundle args = new Bundle();
        args.putParcelable(PICTURE, Parcels.wrap(picture));
        EditPhotoFragment fragment = new EditPhotoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public EditPhotoFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_photo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pictureData = Parcels.unwrap(getArguments().getParcelable(PICTURE));
        image = (ImageView) view.findViewById(R.id.image);
        image.addOnLayoutChangeListener(this);
        facesContainer = (RelativeLayout) view.findViewById(R.id.facesContainer);

        Glide.with(this)
                .load(new File(pictureData.filename))
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(image);

        /**
         * TODO
         * get the image & facesContainer from the fragment arguments
         * populate the imageview with the captured image
         * add the facesContainer
         */
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int width = v.getWidth();
        int height = v.getHeight();
        Timber.d("ImageView Width: %d, Height: %d", width, height);

        if (height > 0) {
            // set the  facesContainer to match the image dimensions (height)
            facesContainer.getLayoutParams().height = height;
            facesContainer.setTop(v.getTop());
            facesContainer.setBottom(v.getBottom());
            facesContainer.invalidate();
            Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
            Timber.d("Bitmap Width: %d, Height: %d", bitmap.getWidth(), bitmap.getHeight());
            FaceDetector detector = new FaceDetector.Builder(getActivity())
                    .setTrackingEnabled(false)
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .build();
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Face> faces = detector.detect(frame);
            Timber.d("Faces found: %d", faces.size());
            detector.release();
            addFaces(faces);
        }
    }

    private void addFaces(SparseArray<Face> faces) {

        if (faces == null) {
            return;
        }

        for (int i = 0; i < faces.size(); i++) {
            Face face = faces.valueAt(i);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) face.getWidth(), (int) face.getHeight());
            params.setMargins((int) face.getPosition().x, (int) face.getPosition().y, 0, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            ImageView faceView = new ImageView(getActivity());
            //faceView.setImageResource(R.drawable.sad_jordan);
            faceView.setBackgroundColor(0xFFFFFF);
            facesContainer.addView(faceView, i, params);
        }

    }
}
