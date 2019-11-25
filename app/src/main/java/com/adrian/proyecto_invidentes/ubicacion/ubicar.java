package com.adrian.proyecto_invidentes.ubicacion;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ubicar {


    public ubicar()
    {


    }

    public void localizar()
    {

    }

    public void getAddress(Context context, double LATITUDE, double LONGITUDE) {

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {



                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                Log.d("test", "getAddress:  address" + address);
                Log.d("test", "getAddress:  city" + city);
                Log.d("test", "getAddress:  state" + state);
                Log.d("test", "getAddress:  postalCode" + postalCode);
                Log.d("test", "getAddress:  knownName" + knownName);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }


}
