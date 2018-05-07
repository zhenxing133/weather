package com.weather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChooseFragment chooseFragment = new ChooseFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content,chooseFragment).commit();
    }
}
