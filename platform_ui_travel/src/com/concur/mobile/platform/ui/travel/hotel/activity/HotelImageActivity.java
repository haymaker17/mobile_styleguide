// package com.concur.mobile.platform.ui.travel.hotel.activity;
//
// import android.app.Activity;
// import android.content.Intent;
// import android.os.Bundle;
//
// import com.concur.mobile.platform.travel.search.hotel.HotelImagePair;
// import com.concur.mobile.platform.ui.travel.R;
// import com.concur.mobile.platform.ui.travel.hotel.fragment.ImageListItem;
// import com.concur.mobile.platform.ui.travel.util.Const;
//
// /**
// * Activity to show hotel images in full screen
// *
// * @author tejoa
// *
// */
// public class HotelImageActivity extends Activity {
//
// @Override
// public void onCreate(Bundle savedInstanceState) {
// super.onCreate(savedInstanceState);
// setContentView(R.layout.full_image_view);
//
// Intent i = this.getIntent();
// final Bundle bundle = i.getExtras();
// HotelImagePair hotelImage = (HotelImagePair) bundle.getSerializable(Const.EXTRA_HOTEL_IMAGE_ITEM);
// ImageListItem imageListItem = new ImageListItem(hotelImage);
//
// // ImageView imageView = (ImageView) findViewById(R.id.expanded_image);
// imageListItem.buildView(getApplicationContext(), imageView, null);
// }
//
// }
