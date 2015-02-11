package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.travel.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Fragment to show Hotel Map
 * 
 * @author tejoa
 * 
 */
public class HotelMapFragment extends PlatformFragmentV1 implements OnMapReadyCallback {

    private static GoogleMap googleMap;
    private LatLng position;
    private MapFragment mapFragment;
    private ImageView snapshotHolder;
    private boolean liteMode;

    public HotelMapFragment(LatLng position, boolean liteMode) {
        this.position = position;
        this.liteMode = liteMode;
    }

    public HotelMapFragment(LatLng position) {
        this.position = position;
        this.liteMode = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // inflate the details fragment
        View mainView = inflater.inflate(R.layout.map_layout, container, false);

        setUpMap();
        if (googleMap != null) {

            // Intent i = getActivity().getIntent();
            // TODO load custom icons
            // position = i.getParcelableExtra(Const.EXTRA_HOTEL_LOCATION);
            addMarkers();

            googleMap.getUiSettings().setZoomControlsEnabled(false);
            // googleMap.setOnMapClickListener(null);

            // googleMap.setOnMapClickListener(new OnMapClickListener() {
            //
            // @Override
            // public void onMapClick(LatLng arg0) {
            //
            // View view = mapFragment.getView();
            // if (view.getLayoutParams().height != -1) {
            //
            // // Create new transaction
            // FragmentTransaction transaction = getFragmentManager().get //beginTransaction();
            //
            // // Replace whatever is in the fragment_container view with this fragment,
            // // and add the transaction to the back stack
            // transaction.replace(R.id.map_view, new HotelMapFragment(arg0));
            // transaction.addToBackStack(null);
            //
            // // Commit the transaction
            // transaction.commit();

            // // LinearLayout lp = new LinearLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            // // view.
            // // view.setLayoutParams(p);
            // // view.requestLayout();
            // // lp.addView(view);
            // }

            // LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            // mapFragment.getFragmentManager().;

            // }
            // });
        }

        return mainView;
    }

    private void setUpMap() {
        if (googleMap == null) {
            Activity activity = getActivity();
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
            if (resultCode == ConnectionResult.SUCCESS) {
                if (liteMode) {
                    GoogleMapOptions options = new GoogleMapOptions().liteMode(true);
                    mapFragment = MapFragment.newInstance(options);
                } else {
                    mapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));

                }
                mapFragment.getMapAsync(this);

            } else {
                Toast.makeText(activity, "Map Unavailable", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void addMarkers() {
        if (googleMap != null) {
            MarkerOptions marker = new MarkerOptions().position(position);
            googleMap.addMarker(marker);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (googleMap != null)
            addMarkers();

        if (googleMap == null) {
            setUpMap();
            // addMarkers();
        }
    }

    // @Override
    // public void onClick(View arg0) {
    // // TODO Auto-generated method stub
    //
    // }

    // private void resizeFragment(Fragment f, int newWidth, int newHeight) {
    // if (f != null) {
    // View view = f.getView();
    // RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(newWidth, newHeight);
    // view.setLayoutParams(p);
    // view.requestLayout();
    //
    // }
    // }

    /****
     * The mapfragment's id must be removed from the FragmentManager or else if the same it is passed on the next time then app
     * will crash
     ****/
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // if (googleMap != null) {
        // getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.layout.map_layout))
        // .commit();
        googleMap = null;
        // }

    }

    // public void CaptureMapScreen() {
    // SnapshotReadyCallback callback = new SnapshotReadyCallback() {
    //
    // @Override
    // public void onSnapshotReady(Bitmap snapshot) {
    // // TODO Auto-generated method stub
    // Bitmap bitmap = snapshot;
    // try {
    // FileOutputStream out = new FileOutputStream("/mnt/sdcard/" + "MyMapScreen"
    // + System.currentTimeMillis() + ".png");
    //
    // // above "/mnt ..... png" => is a storage path (where image will be stored) + name of image you can customize
    // // as per your Requirement
    //
    // bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // openShareImageDialog(filePath);
    // }
    // };
    //
    // googleMap.snapshot(callback);
    //
    // // myMap is object of GoogleMap +> GoogleMap myMap;
    // // which is initialized in onCreate() =>
    // // myMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_pass_home_call)).getMap();
    // }

    // @SuppressLint("WorldReadableFiles")
    // public void captureScreen() {
    // SnapshotReadyCallback callback = new SnapshotReadyCallback() {
    //
    // @Override
    // public void onSnapshotReady(Bitmap snapshot) {
    // // TODO Auto-generated method stub
    // Bitmap bitmap = snapshot;
    //
    // OutputStream fout = null;
    //
    // String filePath = System.currentTimeMillis() + ".jpeg";
    //
    // try {
    // fout = openFileOutput(filePath, getActivity().MODE_WORLD_READABLE);
    // // fout = new FileOutputStream("/mnt/sdcard/" + "MyMapScreen" + System.currentTimeMillis() + ".png");
    //
    // // Write the string to the file
    // bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
    // fout.flush();
    // fout.close();
    // } catch (FileNotFoundException e) {
    // // TODO Auto-generated catch block
    // Log.d("ImageCapture", "FileNotFoundException");
    // Log.d("ImageCapture", e.getMessage());
    // filePath = "";
    // } catch (IOException e) {
    // // TODO Auto-generated catch block
    // Log.d("ImageCapture", "IOException");
    // Log.d("ImageCapture", e.getMessage());
    // filePath = "";
    // }
    //
    // openShareImageDialog(filePath);
    // }
    //
    // public void openShareImageDialog(String filePath) {
    // File file = this.getFileStreamPath(filePath);
    //
    // if (!filePath.equals("")) {
    // final ContentValues values = new ContentValues(2);
    // values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    // values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
    // final Uri contentUriFile = getContentResolver().insert(
    // MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    //
    // final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
    // intent.setType("image/jpeg");
    // intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile);
    // startActivity(Intent.createChooser(intent, "Share Image"));
    // } else {
    // // This is a custom class I use to show dialogs...simply replace this with whatever you want to show an error
    // // message, Toast, etc.
    // DialogUtilities.showOkDialogWithText(this, R.string.shareImageFailed);
    // }
    // }
    // };
    //
    // googleMap.snapshot(callback);
    // }
    //
    // /**
    // * Called when the snapshot button is clicked.
    // */
    // public void onScreenshot(View view) {
    // takeSnapshot();
    // }

    public void takeSnapshot() {

        if (googleMap == null) {
            setUpMap();
        }

        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(position, 4.0f);
        googleMap.animateCamera(yourLocation);

        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            public void onMapLoaded() {
                googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {

                    @Override
                    public void onSnapshotReady(Bitmap snapshot) {
                        // Callback is called from the main thread, so we can modify the ImageView safely.
                        if (snapshotHolder != null && snapshot != null) {
                            // bitmap = snapshot;
                            // FileOutputStream out;
                            // try {
                            // out = new FileOutputStream("/mnt/sdcard/Download/TeleSensors.png");
                            // bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                            // } catch (FileNotFoundException e) {
                            // // TODO Auto-generated catch block
                            // e.printStackTrace();
                            // }

                            snapshotHolder.setImageBitmap(snapshot);
                        }
                    }
                });
            }

        });
    }

    /**
     * Called when the clear button is clicked.
     */
    public void onClearScreenshot(View view) {
        // ImageView snapshotHolder = (ImageView) findViewById(R.id.snapshot_holder);
        snapshotHolder.setImageDrawable(null);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (!liteMode) {
            addMarkers();
        }

    }

}
