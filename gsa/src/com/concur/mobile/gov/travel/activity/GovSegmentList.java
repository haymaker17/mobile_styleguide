package com.concur.mobile.gov.travel.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.concur.gov.R;
import com.concur.mobile.core.travel.activity.SegmentList;
import com.concur.mobile.core.travel.air.data.AirSegment;
import com.concur.mobile.core.travel.car.data.CarSegment;
import com.concur.mobile.core.travel.data.LocationChoice;
import com.concur.mobile.core.travel.data.SearchSuggestion;
import com.concur.mobile.core.travel.data.Segment;
import com.concur.mobile.core.travel.data.Segment.SegmentType;
import com.concur.mobile.core.travel.hotel.data.HotelSegment;
import com.concur.mobile.core.travel.rail.data.RailSegment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.travel.air.activity.GovAirSegmentDetail;
import com.concur.mobile.gov.travel.car.activity.GovCarSearch;
import com.concur.mobile.gov.travel.hotel.activity.GovHotelSearch;
import com.concur.mobile.gov.util.TravelBookingCache;
import com.concur.mobile.gov.util.TravelBookingCache.BookingSelection;

public class GovSegmentList extends SegmentList {

    protected static final int SELECTED_PERDIEM_LOCATION_ADD_HOTEL = 1;

    private static final int SEG_LIST = 100;

    public static final String IS_ADD_CAR = "ADD_CAR";
    public static final String IS_ADD_HOTEL = "ADD_HOTEL";

    private SearchSuggestion suggestion;

    public static final String TRAV_ID = "travID";
    public static final String TRIP_ID = "tripId";
    public static final String AUTH_NUM = "auth number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GovAppMobile app = ((GovAppMobile) getApplication());
        // clear cache..
        app.trvlBookingCache = new TravelBookingCache();
    }

    @Override
    protected void searchCar(SearchSuggestion suggestion) {

        // Intent intent = new Intent(this, CarSearch.class);
        GovAppMobile app = ((GovAppMobile) getApplication());
        Intent intent = new Intent(this, GovCarSearch.class);
        app.trvlBookingCache.setSelectedBookingType(BookingSelection.CAR);
        intent.putExtra(IS_ADD_CAR, true);
        // Add the Cliqbook trip id.
        intent.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, trip.cliqbookTripId);
        // Add the client locator.
        intent.putExtra(Const.EXTRA_TRAVEL_CLIENT_LOCATOR, trip.clientLocator);
        // Add the record locator.
        intent.putExtra(Const.EXTRA_TRAVEL_RECORD_LOCATOR, trip.recordLocator);
        // Add the suggestion location.
        LocationChoice locChoice = suggestion.getStartLocationChoice(getApplicationContext());
        if (locChoice != null) {
            intent.putExtra(Const.EXTRA_TRAVEL_LOCATION, locChoice.getBundle());
        }
        // Add the suggestion pick-up date.
        if (suggestion.getStartDate() != null) {
            intent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_PICK_UP_CALENDAR, suggestion.getStartDate());
        }
        // Add the suggestion drop-off date.
        if (suggestion.getStopDate() != null) {
            intent.putExtra(Const.EXTRA_TRAVEL_CAR_SEARCH_DROP_OFF_CALENDAR, suggestion.getStopDate());
        }
        intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIP);
        startActivityForResult(intent, Const.REQUEST_CODE_BOOK_CAR);
    }

    @Override
    protected void searchHotel(SearchSuggestion suggestion) {
        this.suggestion = suggestion;
        Intent it = new Intent(GovSegmentList.this, GovLocationSearch.class);
        startActivityForResult(it, SELECTED_PERDIEM_LOCATION_ADD_HOTEL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case SELECTED_PERDIEM_LOCATION_ADD_HOTEL: {
            if (resultCode == RESULT_OK) {
                GovAppMobile app = ((GovAppMobile) getApplication());
                Intent intent = new Intent(this, GovHotelSearch.class);
                app.trvlBookingCache.setSelectedBookingType(BookingSelection.HOTEL);
                intent.putExtra(IS_ADD_HOTEL, true);
                intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIPS);
                // Add the Cliqbook trip id.
                intent.putExtra(Const.EXTRA_TRAVEL_CLIQBOOK_TRIP_ID, trip.cliqbookTripId);
                // Add the suggestion location.
                LocationChoice locChoice = this.suggestion.getStartLocationChoice(getApplicationContext());
                if (locChoice != null) {
                    intent.putExtra(Const.EXTRA_TRAVEL_LOCATION, locChoice.getBundle());
                }
                // Add the suggestion pick-up date.
                if (this.suggestion.getStartDate() != null) {
                    intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_IN, this.suggestion.getStartDate());
                }
                // Add the suggestion drop-off date.
                if (this.suggestion.getStopDate() != null) {
                    intent.putExtra(Const.EXTRA_TRAVEL_HOTEL_SEARCH_CHECK_OUT, this.suggestion.getStopDate());
                }
                intent.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_TRIP);
                intent.putExtra(IS_ADD_HOTEL, true);
                startActivityForResult(intent, Const.REQUEST_CODE_BOOK_HOTEL);
            }
            break;
        }

        case SEG_LIST: {
            // After successfull booking remove all the cache data
            GovAppMobile app = (GovAppMobile) getApplication();
            app.trvlBookingCache = new TravelBookingCache();
            if (resultCode == RESULT_OK) {
                setResult(resultCode, data);
                finish();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult.SEG_LIST==null");
                // finish();
            }
            break;
        }
        default:
            super.onActivityResult(requestCode, resultCode, data);
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handled = super.onContextItemSelected(item);
        if (!handled) {
            final int itemId = item.getItemId();
            if (itemId == MENU_ITEM_VIEW_DETAILS) {
                handled = false;
            } else if (itemId == MENU_ITEM_CANCEL) {
                if (longPressSegment instanceof HotelSegment) {
                    showDialog(DIALOG_CANCEL_HOTEL_CONFIRM);
                    handled = true;
                } else if (longPressSegment instanceof CarSegment) {
                    showDialog(DIALOG_CANCEL_CAR_CONFIRM);
                    handled = true;
                } else if (longPressSegment instanceof AirSegment) {
                    showDialog(DIALOG_CANCEL_AIR_CONFIRM);
                    handled = true;
                } else if (longPressSegment instanceof RailSegment) {
                    showDialog(DIALOG_CANCEL_RAIL_CONFIRM);
                    handled = true;
                }
            } /*
               * else if (itemId == R.id.menuTripBookCar) {
               * addCar();
               * handled = true;
               * } else if (itemId == R.id.menuTripBookHotel) {
               * addHotel();
               * handled = true;
               * } else if (itemId == R.id.menuTripViewAuth) {
               * gotoDocumentDetail();
               * handled = true;
               * }
               */
        }
        return handled;
    }

    protected void gotoDocumentDetail() {
        Intent it = new Intent(GovSegmentList.this, DocInfoFromTripLocator.class);
        it.putExtra(TRIP_ID, trip.cliqbookTripId);
        it.putExtra(AUTH_NUM, trip.authNumber);
        startActivityForResult(it, SEG_LIST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View,
     * android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        final int id = view.getId();
        /*
         * if (id == R.id.action_button) {
         * MenuInflater infl = getMenuInflater();
         * infl.inflate(R.menu.segmentlist_book, menu);
         * 
         * if (!trip.allowAddCar) {
         * menu.findItem(R.id.menuTripBookCar).setVisible(false);
         * }
         * if (!trip.allowAddHotel) {
         * menu.findItem(R.id.menuTripBookHotel).setVisible(false);
         * }
         * 
         * menu.setHeaderTitle(R.string.home_action_title);
         * } else
         */
        if (id == R.id.seg_list) {
            longPressSegment = null;
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Object tag = info.targetView.getTag();
            if (tag instanceof Segment) {
                longPressSegment = (Segment) tag;
            }
            if (longPressSegment != null) {
                if (isTraveler()) {
                    if (longPressSegment instanceof HotelSegment) {
                        menu.setHeaderTitle(R.string.segmentlist_hotel_long_press_hotel_title);
                        if (trip.allowCancel) {
                            menu.add(0, MENU_ITEM_CANCEL, 0, R.string.general_cancel_hotel);
                        }
                    } else if (longPressSegment instanceof CarSegment) {
                        menu.setHeaderTitle(R.string.segmentlist_car_long_press_car_title);
                        if (trip.allowCancel) {
                            menu.add(0, MENU_ITEM_CANCEL, 0, R.string.general_cancel_car);
                        }
                    } else if (longPressSegment instanceof AirSegment) {
                        menu.setHeaderTitle(R.string.segmentlist_air_long_press_air_title);
                    } else if (longPressSegment instanceof RailSegment) {
                        menu.setHeaderTitle(R.string.segmentlist_rail_long_press_rail_title);
                    }
                }
                MenuItem menuItem = menu.add(0, MENU_ITEM_VIEW_DETAILS, 0, R.string.view_details);
                Intent intent = new Intent(this, longPressSegment.getType().activity);
                intent.putExtra(Const.EXTRA_ITIN_LOCATOR, trip.itinLocator);
                intent.putExtra(Const.EXTRA_SEGMENT_KEY, longPressSegment.segmentKey);
                menuItem.setIntent(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.segmentlist_book, menu);
        if (!trip.allowAddCar) {
            menu.findItem(R.id.menuTripBookCar).setVisible(false);
        }
        if (!trip.allowAddHotel) {
            menu.findItem(R.id.menuTripBookHotel).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean handled = false;
        final int itemId = menuItem.getItemId();
        if (itemId == R.id.menuTripBookCar) {
            addCar();
            handled = true;
        } else if (itemId == R.id.menuTripBookHotel) {
            addHotel();
            handled = true;
        } else if (itemId == R.id.menuTripViewAuth) {
            gotoDocumentDetail();
            handled = true;
        }
        return handled;
    }

    /**
     * get the segment clicked intent. it will return AirSegmentDetail, CarSegmentDetail,HotelSegmentDetail,RailSegmentDetail
     * respectively based on condition
     * */
    @Override
    protected Intent getClickedSegIntent(Segment segmentClicked) {
        if (segmentClicked.getType() == SegmentType.AIR) {
            return new Intent(GovSegmentList.this, GovAirSegmentDetail.class);
        } else {
            return super.getClickedSegIntent(segmentClicked);
        }
    }

}
