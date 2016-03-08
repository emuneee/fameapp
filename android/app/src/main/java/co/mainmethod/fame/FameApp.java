package co.mainmethod.fame;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;

/**
 * Fame app application context
 * Created by evan on 1/30/16.
 */
public class FameApp extends Application {

    public static FameApp getApp(Context context) {
        return (FameApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
