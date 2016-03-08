package co.mainmethod.fame.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import co.mainmethod.fame.R;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class PhotoActivity extends AppCompatActivity
    implements EasyPermissions.PermissionCallbacks {

    public static final int RC_CAMERA = 0x01;
    private static final String[] PERMISSION = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, PhotoActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        setContentView(R.layout.activity_photo);
        startCaptureFragment();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(List<String> list) {
        // Some permissions have been granted
        // ...
    }

    @Override
    public void onPermissionsDenied(List<String> list) {
        // Some permissions have been denied
        // ...
    }

    @AfterPermissionGranted(RC_CAMERA)
    private void startCaptureFragment() {

        if (EasyPermissions.hasPermissions(this, PERMISSION)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, CaptureFragment.newInstance())
                    .commit();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.camera_rationale),
                    RC_CAMERA, PERMISSION);
        }
    }
}
