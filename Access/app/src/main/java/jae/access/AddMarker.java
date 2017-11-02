package jae.access;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddMarker extends AppCompatActivity {

    Geocoder geocoder;
    Double lat = null;
    Double lng = null;
    String fullAddress;
    String desc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addmarker);
    }

    public void findCoord(View view){
        geocoder = new Geocoder(this, Locale.ENGLISH);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location;
        LocationManager locationManagerCt = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        location = locationManagerCt.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        lat = location.getLatitude();
        lng = location.getLongitude();

        LatLng latLng = new LatLng(lat, lng);
        EditText curAddress =(EditText) findViewById(R.id.LocationText);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if(addresses != null){
                Address address = addresses.get(0);
                StringBuilder strAddress = new StringBuilder("");
                for(int i=0; i < address.getMaxAddressLineIndex(); i++){
                    if(i == address.getMaxAddressLineIndex() - 1){
                        strAddress.append(address.getAddressLine(i)).append("");
                    }
                    else
                        strAddress.append(address.getAddressLine(i)).append(", ");
                }
                fullAddress = strAddress.toString();
                curAddress.setText(fullAddress);
            }
            else
                curAddress.setText("No Address Found!");


        } catch (IOException e) {
            e.printStackTrace();
            curAddress.setText("Cannot get address!");
        }

    }

    public void create(View view)
    {
        Intent i = new Intent(this, MapsActivity.class);
        EditText descText =(EditText) findViewById(R.id.accessDesc);
        desc = descText.getText().toString();
        Bundle bundle = new Bundle();
        bundle.putDouble("lat", lat);
        bundle.putDouble("lng", lng);
        bundle.putString("address", fullAddress);
        bundle.putString("desc", desc);
        i.putExtras(bundle);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    public void cancel(View view){
        finish();
    }
}
