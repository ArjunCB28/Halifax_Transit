package com.example.assignment3;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.transit.realtime.GtfsRealtime;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

class VehiclePositionReader extends AsyncTask<Void, Void, List<GtfsRealtime.FeedEntity>> {
    private static GoogleMap googleMap;
    private static Bitmap busIcon;
    private static HashMap<String, Marker> vehicleHashMap = new HashMap<>();

    VehiclePositionReader(GoogleMap gmap, Bitmap icon) {
        googleMap = gmap;
        busIcon = icon;
    }

    VehiclePositionReader() {
        googleMap.clear();
    }

    @Override
    protected List<GtfsRealtime.FeedEntity> doInBackground(Void... voids) {
        List<GtfsRealtime.FeedEntity> entityList = null;
        try {
            URL url = new URL("http://gtfs.halifax.ca/realtime/Vehicle/VehiclePositions.pb");
            entityList = GtfsRealtime.FeedMessage.parseFrom(url.openStream()).getEntityList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entityList;
    }

    private Bitmap drawStringOnBitmap(String routeNo) {
        Bitmap bitmap = Bitmap.createBitmap(512, 512, busIcon.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(busIcon, -10, 0, null);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#000000"));
        paint.setTextSize(200);
        paint.setAntiAlias(true);
        int xPos = (canvas.getWidth() / 5);
        canvas.drawText(routeNo, xPos, 250, paint);
        bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
        return bitmap;
    }

    @Override
    protected void onPostExecute(List<GtfsRealtime.FeedEntity> feedEntities) {
        for (GtfsRealtime.FeedEntity entity : feedEntities) {
            GtfsRealtime.VehiclePosition vehiclePosition = entity.getVehicle();
            String routeId = vehiclePosition.getTrip().getRouteId();
            String tripId = vehiclePosition.getTrip().getTripId();
            float latitude = vehiclePosition.getPosition().getLatitude();
            float longitude = vehiclePosition.getPosition().getLongitude();
            LatLng busLocation = new LatLng(latitude, longitude);
            // check if the hashmap already has the marker.
            // update the marker position. if it is not present create a marker to the google map
            if (vehicleHashMap.containsKey(routeId + "-" + tripId)) {
                if (MapsActivity.filterCalled) {
                    if (MapsActivity.filteredBusNo.contains(routeId)) {
                        MarkerOptions busOption;
                        busOption = new MarkerOptions().position(busLocation);
                        busOption.icon(BitmapDescriptorFactory.fromBitmap(drawStringOnBitmap(routeId)));
                        busOption.title(routeId);
                        Marker vehiclePositionMarker = googleMap.addMarker(busOption);
                        vehicleHashMap.put(routeId + "-" + tripId, vehiclePositionMarker);
                        vehicleHashMap.put(routeId + "-" + tripId, vehiclePositionMarker);
                    }
                } else {
                    Marker vehiclePositionMarker = vehicleHashMap.get(routeId + "-" + tripId);
                    vehiclePositionMarker.setPosition(busLocation);
                    vehicleHashMap.put(routeId + "-" + tripId, vehiclePositionMarker);
                }
            } else {
                MarkerOptions busOption;
                busOption = new MarkerOptions().position(busLocation);
                busOption.icon(BitmapDescriptorFactory.fromBitmap(drawStringOnBitmap(routeId)));
                busOption.title(routeId);
                if (MapsActivity.firstTime) {
                    MapsActivity.totalBusNos.add(routeId);
                }
                Marker vehiclePositionMarker = googleMap.addMarker(busOption);
                vehicleHashMap.put(routeId + "-" + tripId, vehiclePositionMarker);
            }
        }
        MapsActivity.firstTime = false;
    }
}


// http://gtfs.halifax.ca/realtime/Vehicle/VehiclePositions.pb
// http://gtfs.halifax.ca/realtime/Alert/Alerts.pb
// http://gtfs.halifax.ca/realtime/TripUpdate/TripUpdates.pb
// http://gtfs.halifax.ca/static/google_transit.zip
