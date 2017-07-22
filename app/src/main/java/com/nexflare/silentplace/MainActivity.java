package com.nexflare.silentplace;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

public class MainActivity extends AppCompatActivity {
    public static final String API_KEY="AIzaSyBwtkA6ds-knHRg7eYutlF_Stgt7r_jVUA";
    public static final String TAG="place_search";
    private static final int REQUEST_CODE_PLACE = 2511;
    FloatingActionButton fabGetPlace;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fabGetPlace= (FloatingActionButton) findViewById(R.id.fabGetPlace);

        fabGetPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent=new PlaceAutocomplete.
                            IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).
                            build(MainActivity.this);
                    startActivityForResult(intent,REQUEST_CODE_PLACE);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_PLACE){
            if(resultCode==RESULT_OK){
                Place place=PlaceAutocomplete.getPlace(this,data);
                Toast.makeText(this, place.getAddress(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
