package com.theo.limitesolal;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(ScreenLimitPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
