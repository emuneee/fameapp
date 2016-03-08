package co.mainmethod.fame.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.jakewharton.rxbinding.view.RxView;

import co.mainmethod.fame.R;

/**
 * Created by evan on 2/2/16.
 */
public class FameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fame);
        RxView.clicks(findViewById(R.id.create)).subscribe(aVoid -> PhotoActivity.start(this));
    }
}
