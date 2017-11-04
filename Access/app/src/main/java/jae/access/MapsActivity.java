package jae.access;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity {
    static final int INFO_REQUEST = 1;
    public String FILENAME = "markers.txt";
    JSONArray jArray = null;
    GoogleMap mMap; // Might be null if Google Play services APK is not available.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();


        FileOutputStream fos = null;
        if(!(new File(getFilesDir() + "/" + FILENAME).exists())){
            File markerFile = new File(getFilesDir() + "/" + FILENAME);
            try {
                markerFile.createNewFile();
                fos = new FileOutputStream(markerFile);
                fos.flush();
                fos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                String str = readFromFile();
                Double newLat = null;
                Double newLng = null;
                String newAdd = "";
                String newDesc = "";
                if(str.length() == 0){
                    Log.i("jae.access", "No String in File");
                    jArray = new JSONArray();
                }
                else{
                    jArray = new JSONArray(str);
                    for(int loop=0; loop < jArray.length(); loop++) {
                        JSONObject jObject = jArray.getJSONObject(loop);
                        newLat = jObject.getDouble("lat");
                        newLng = jObject.getDouble("lng");
                        newAdd = jObject.getString("address");
                        newDesc = jObject.getString("desc");
                        LatLng newLoc = new LatLng(newLat, newLng);
                        System.out.println(newLoc);
                        mMap.addMarker(new MarkerOptions().position(newLoc).
                                icon(BitmapDescriptorFactory.
                                        defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).
                                title(newAdd).snippet(newDesc));
                    }
                    setUpMap(jArray);
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        try {
            String str = readFromFile();
            Double newLat = null;
            Double newLng = null;
            String newAdd = "";
            String newDesc = "";
            if(str.length() == 0){
                Log.i("jae.access", "No String in File");
                jArray = new JSONArray();
            }
            else{
                jArray = new JSONArray(str);
                for(int loop=0; loop < jArray.length(); loop++) {
                    JSONObject jObject = jArray.getJSONObject(loop);
                    newLat = jObject.getDouble("lat");
                    newLng = jObject.getDouble("lng");
                    newAdd = jObject.getString("address");
                    newDesc = jObject.getString("desc");
                    LatLng newLoc = new LatLng(newLat, newLng);
                    System.out.println(newLoc);
                    mMap.addMarker(new MarkerOptions().position(newLoc).
                            icon(BitmapDescriptorFactory.
                                    defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).
                            title(newAdd).snippet(newDesc));
                }
            }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
    }

    public void startAddMarker(View view) {
        Intent intent = new Intent(this, AddMarker.class);
        startActivityForResult(intent, INFO_REQUEST);
    }

    private void writeToFile(String data){
        try{
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter((openFileOutput(FILENAME, Context.MODE_PRIVATE)));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e){
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(){
        String returnStr = "";
        try{
            InputStream inputStream = openFileInput(FILENAME);

            if(inputStream != null){
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while((receiveString = bufferedReader.readLine()) != null){
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                returnStr = stringBuilder.toString();
            }
        }
        catch(FileNotFoundException e){
            Log.e("marker activity", returnStr + e.toString());
        }
        catch(IOException e){
            Log.e("marker activity", "Can not read file: " + e.toString());
        }
        return returnStr;
    }
    @Override
    public void onActivityResult(int request, int result, Intent i)
    {
        JSONObject jsonObj = new JSONObject();
        FileOutputStream outputStream;
        if(request == 1){
            if(result == Activity.RESULT_OK){
                Bundle extras = i.getExtras();
                Double lat = 0.0;
                Double lng = 0.0;
                String address = null;
                String desc = null;
                if(extras != null){
                    lat = extras.getDouble("lat");
                    lng = extras.getDouble("lng");
                    address = extras.getString("address");
                    desc = extras.getString("desc");
//                    type = extras.getString("type");
                }
                LatLng loc = new LatLng(lat,lng);
                mMap.addMarker(new MarkerOptions().position(loc).title(address).snippet(desc));

                try {
                    String str = readFromFile();
                    Double newLat = null;
                    Double newLng = null;
                    String newAdd = "";
                    String newDesc = "";
                    for(int loop=0; loop < jArray.length()-1; loop++){
                        JSONObject jObject = jArray.getJSONObject(loop);
                        newLat = jObject.getDouble("lat");
                        newLng = jObject.getDouble("lng");
                        newAdd = jObject.getString("address");
                        newDesc = jObject.getString("desc");
                        LatLng newLoc= new LatLng(newLat, newLng);
                        System.out.println(newLoc);
                        mMap.addMarker(new MarkerOptions().position(newLoc).
                                icon(BitmapDescriptorFactory.
                                        defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).
                                title(newAdd).snippet(newDesc));
                    }
                    jsonObj.put("lat", lat);
                    jsonObj.put("lng", lng);
                    jsonObj.put("address", address);
                    jsonObj.put("desc", desc);
//                    jsonObj.put("type", type);
                    jArray.put(jsonObj);
                    String arrayStr = jArray.toString();
                    writeToFile(arrayStr);
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void onSearch(View view) {
        EditText location_tf = (EditText) findViewById(R.id.searchBar);
        String location = location_tf.getText().toString();
        List<Address> listAddress = null;
        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                listAddress = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = listAddress.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            float zoomLevel = 16;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

        }
    }

    public void changeType(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }

    }

    private void setUpMap(JSONArray jArray) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location locationCt;
        LocationManager locationManagerCt = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationCt = locationManagerCt
                .getLastKnownLocation(LocationManager.GPS_PROVIDER);

        double tempLat = locationCt.getLatitude();
        double tempLong = locationCt.getLongitude();

        LatLng latLng = new LatLng(locationCt.getLatitude(),
                locationCt.getLongitude());

        LatLngBounds bounds = getNearbyMarkers(tempLat, tempLong);

        //Load Markers through for(loop) with if(

        Double newLat = null;
        Double newLng = null;
        String newAdd = "";
        String newDesc = "";
            for(int loop=0; loop < jArray.length(); loop++) {
                try {
                    JSONObject jObject = jArray.getJSONObject(loop);
                    newLat = jObject.getDouble("lat");
                    newLng = jObject.getDouble("lng");
                    newAdd = jObject.getString("address");
                    newDesc = jObject.getString("desc");
                    LatLng newLoc = new LatLng(newLat, newLng);
                    System.out.println(newLoc);
                    if (bounds.contains(newLoc)) {
                        //System.out.println(newLoc + " Bounds contains this marker");
                        mMap.addMarker(new MarkerOptions().position(newLoc).
                                icon(BitmapDescriptorFactory.
                                        defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).
                                title(newAdd).snippet(newDesc));
                    }
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }

        mMap.setMyLocationEnabled(true);
        float zoomLevel = 16;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }

    private LatLngBounds getNearbyMarkers(double lat, double longi){
        LatLng lowerBounds = new LatLng(lat - 0.004, longi - 0.004);
        LatLng upperBounds = new LatLng(lat + 0.004, longi + 0.004);

        LatLngBounds perimeter = new LatLngBounds(lowerBounds, upperBounds);

        return perimeter;
    }
}
