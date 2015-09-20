package com.example.arthur.multimediademo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by arthur on 15-8-28.
 */
public class TestService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
