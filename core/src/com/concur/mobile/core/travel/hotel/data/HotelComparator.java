/**
 * 
 */
package com.concur.mobile.core.travel.hotel.data;

import java.util.Comparator;

import com.concur.mobile.platform.util.Parse;

/**
 * An implementation of <code>Comparator</code> for comparing <code>HotelChoice</code> objects based on different attributes.
 * 
 * @author AndrewK
 */
public class HotelComparator implements Comparator<HotelChoice> {

    // Defines which field to sort on.
    public enum CompareField {
        DISTANCE, // compare on distance.
        STAR_RATING, // compare on star rating.
        CHEAPEST_ROOM, // compare on cheapest room rate.
        NAME, // compare on hotel name.
        PREFERENCE, // compare on preference rank.
        RECOMMENDATION // compare on hotel recommendation
    };

    // Defines the sort order.
    public enum CompareOrder {
        ASCENDING, DESCENDING
    }

    // Contains the comparison field (defaults to cheapest room).
    private CompareField compField = CompareField.CHEAPEST_ROOM;
    // Contains the compare ordering (defaults to ascending).
    private CompareOrder compOrder = CompareOrder.ASCENDING;

    /**
     * Constructs an instance of <code>HotelComparator</code> that orders by ascending cheapeast room.
     */
    public HotelComparator() {
        this(CompareField.CHEAPEST_ROOM, CompareOrder.ASCENDING);
    }

    /**
     * Constructs an instance of <code>HotelComparator</code> that orders by ascending <code>compareFld</code>.
     * 
     * @param compareFld
     *            the hotel compare field.
     */
    public HotelComparator(CompareField compareFld) {
        this(compareFld, CompareOrder.ASCENDING);
    }

    /**
     * Constructs an instance of <code>HotelComparator</code> that orders on <code>compField</code> field with ordering of
     * <code>compOrder</code>.
     * 
     * @param compField
     *            the comparison field.
     * @param compOrder
     *            the comparison order.
     */
    public HotelComparator(CompareField compField, CompareOrder compOrder) {
        this.compField = compField;
        this.compOrder = compOrder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(HotelChoice hotel1, HotelChoice hotel2) {
        int retVal = 0;
        if (hotel1 != hotel2) {
            switch (compField) {
            case CHEAPEST_ROOM:
                retVal = compareCheapestRoom(hotel1, hotel2);
                break;
            case DISTANCE:
                retVal = compareDistance(hotel1, hotel2);
                break;
            case NAME:
                retVal = compareName(hotel1, hotel2);
                break;
            case STAR_RATING:
                retVal = compareStarRating(hotel1, hotel2);
                break;
            case PREFERENCE:
                retVal = comparePreference(hotel1, hotel2);
                break;
            case RECOMMENDATION:
                retVal = compareRecommendation(hotel1, hotel2);
                break;
            }
        } else {
            retVal = 0;
        }
        return retVal;
    }

    /**
     * Compares two hotel choices based on cheapest room.
     * 
     * @param hotel1
     *            the first hotel.
     * @param hotel2
     *            the second hotel.
     * @return
     */
    public int compareCheapestRoom(HotelChoice hotel1, HotelChoice hotel2) {
        int retVal = 0;

        // Obtain cheapest room rate for 'hotel1'.
        Float hotel1Price = null;
        if (hotel1.cheapestRoom != null) {
            hotel1Price = hotel1.cheapestRoom.rateF;
        } else if (hotel1.cheapestRoomWithViolation != null) {
            hotel1Price = Parse.safeParseFloat(hotel1.cheapestRoomWithViolation.rate);
        }
        // Obtain cheapest room rate for 'hotel2'.
        Float hotel2Price = null;
        if (hotel2.cheapestRoom != null) {
            hotel2Price = hotel2.cheapestRoom.rateF;
        } else if (hotel2.cheapestRoomWithViolation != null) {
            hotel2Price = Parse.safeParseFloat(hotel2.cheapestRoomWithViolation.rate);
        }

        switch (compOrder) {
        case ASCENDING:
            if (hotel1Price != null && hotel2Price != null) {
                if (hotel1Price < hotel2Price) {
                    retVal = -1;
                } else if (hotel1Price.floatValue() == hotel2Price.floatValue()) {
                    retVal = 0;
                } else if (hotel1Price > hotel2Price) {
                    retVal = 1;
                }
            } else if (hotel1Price != null) {
                // If no hotel2 price, then put hotel1 before hotel2.
                retVal = -1;
            } else if (hotel2Price != null) {
                // If no hotel1 price, then put hotel1 after hotel2.
                retVal = 1;
            }
            break;
        case DESCENDING:
            if (hotel1Price != null && hotel2Price != null) {
                if (hotel1Price < hotel2Price) {
                    retVal = 1;
                } else if (hotel1Price.floatValue() == hotel2Price.floatValue()) {
                    retVal = 0;
                } else if (hotel1Price > hotel2Price) {
                    retVal = -1;
                }
            } else if (hotel1Price != null) {
                // If no hotel2 price, then put hotel1 before hotel2.
                retVal = -1;
            } else if (hotel2Price != null) {
                // If no hotel1 price, then put hotel1 after hotel2.
                retVal = 1;
            }
            break;
        }

        return retVal;
    }

    /**
     * Compares two hotel choices based on distance.
     * 
     * @param hotel1
     *            the first hotel.
     * @param hotel2
     *            the second hotel.
     * @return
     */
    public int compareDistance(HotelChoice hotel1, HotelChoice hotel2) {
        int retVal = 0;

        switch (compOrder) {
        case ASCENDING:
            if (hotel1.distanceF != null && hotel2.distanceF != null) {
                if (hotel1.distanceF < hotel2.distanceF) {
                    retVal = -1;
                } else if (hotel1.distanceF.floatValue() == hotel2.distanceF.floatValue()) {
                    retVal = 0;
                } else if (hotel1.distanceF > hotel2.distanceF) {
                    retVal = 1;
                }
            } else if (hotel1.distanceF != null) {
                // No distance for hotel2! Have hotel1 come before hotel2.
                retVal = -1;
            } else if (hotel2.distanceF != null) {
                // No distance for hotel1! Have hotel2 come before hotel1.
                retVal = 1;
            }
            break;
        case DESCENDING:
            if (hotel1.distanceF != null && hotel2.distanceF != null) {
                if (hotel1.distanceF < hotel2.distanceF) {
                    retVal = 1;
                } else if (hotel1.distanceF.floatValue() == hotel2.distanceF.floatValue()) {
                    retVal = 0;
                } else if (hotel1.distanceF > hotel2.distanceF) {
                    retVal = -1;
                }
            } else if (hotel1.distanceF != null) {
                // No distance for hotel2! Have hotel1 come before hotel2.
                retVal = -1;
            } else if (hotel2.distanceF != null) {
                // No distance for hotel1! Have hotel2 come before hotel1.
                retVal = 1;
            }
            break;
        }
        return retVal;
    }

    /**
     * Compares two hotel choices based on name.
     * 
     * @param hotel1
     *            the first hotel.
     * @param hotel2
     *            the second hotel.
     * @return
     */
    public int compareName(HotelChoice hotel1, HotelChoice hotel2) {
        int retVal = 0;
        // Obtain hotel1's name for comparison.
        String hotel1Name = null;
        if (hotel1.hotel != null && hotel1.hotel.length() > 0) {
            hotel1Name = hotel1.hotel;
        } else if (hotel1.chainName != null && hotel1.chainName.length() > 0) {
            hotel1Name = hotel1.chainName;
        }
        // Obtain hotel2's name for comparison.
        String hotel2Name = null;
        if (hotel2.hotel != null && hotel2.hotel.length() > 0) {
            hotel2Name = hotel2.hotel;
        } else if (hotel2.chainName != null && hotel2.chainName.length() > 0) {
            hotel2Name = hotel2.chainName;
        }
        switch (compOrder) {
        case ASCENDING:
            if (hotel1Name != null && hotel2Name != null) {
                retVal = hotel1Name.compareTo(hotel2Name);
            } else if (hotel1Name != null) {
                // Hotel 2's name is null! Order hotel1 before hotel2.
                retVal = -1;
            } else if (hotel2Name != null) {
                // Hotel 1's name is null! Order hotel1 after hotel2.
                retVal = 1;
            }
            break;
        case DESCENDING:
            if (hotel1Name != null && hotel2Name != null) {
                // Flip the sign.
                retVal = (hotel1Name.compareTo(hotel2Name) * -1);
            } else if (hotel1Name != null) {
                // Hotel 2's name is null! Order hotel1 before hotel2.
                retVal = -1;
            } else if (hotel2Name != null) {
                // Hotel 1's name is null! Order hotel1 after hotel2.
                retVal = 1;
            }
            break;
        }
        return retVal;
    }

    /**
     * Compares two hotel choices based on star rating.
     * 
     * @param hotel1
     *            the first hotel.
     * @param hotel2
     *            the second hotel.
     * @return
     */
    public int compareStarRating(HotelChoice hotel1, HotelChoice hotel2) {
        int retVal = 0;
        switch (compOrder) {
        case ASCENDING:
            if (hotel1.starRatingI != null && hotel2.starRatingI != null) {
                if (hotel1.starRatingI < hotel2.starRatingI) {
                    retVal = -1;
                } else if (hotel1.starRatingI.intValue() == hotel2.starRatingI.intValue()) {
                    retVal = 0;
                } else if (hotel1.starRatingI > hotel2.starRatingI) {
                    retVal = 1;
                }
            } else if (hotel1.starRatingI != null) {
                // No star rating for hotel2, have hotel 1 come before hotel 2.
                retVal = -1;
            } else if (hotel2.starRatingI != null) {
                // No star rating for hotel1, have hotel 1 come after hotel 2.
                retVal = 1;
            }
            break;
        case DESCENDING:
            if (hotel1.starRatingI != null && hotel2.starRatingI != null) {
                if (hotel1.starRatingI < hotel2.starRatingI) {
                    retVal = 1;
                } else if (hotel1.starRatingI.intValue() == hotel2.starRatingI.intValue()) {
                    retVal = 0;
                } else if (hotel1.starRatingI > hotel2.starRatingI) {
                    retVal = -1;
                }
            } else if (hotel1.starRatingI != null) {
                // No star rating for hotel2, have hotel 1 come before hotel 2.
                retVal = -1;
            } else if (hotel2.starRatingI != null) {
                // No star rating for hotel1, have hotel 1 come after hotel 2.
                retVal = 1;
            }
            break;
        }
        return retVal;
    }

    /**
     * Compares two hotel choices based on their preference rank values.
     * 
     * @param hotel1
     *            the first hotel.
     * @param hotel2
     *            the second hotel.
     * @return
     */
    public int comparePreference(HotelChoice hotel1, HotelChoice hotel2) {
        int retVal = 0;
        switch (compOrder) {
        case ASCENDING:
            if (hotel1.prefRankI != null && hotel2.prefRankI != null) {
                if (hotel1.prefRankI < hotel2.prefRankI) {
                    retVal = -1;
                } else if (hotel1.prefRankI.intValue() == hotel2.prefRankI.intValue()) {
                    retVal = 0;
                } else if (hotel1.prefRankI > hotel2.prefRankI) {
                    retVal = 1;
                }
            } else if (hotel1.prefRankI != null) {
                // No preference rank for hotel2, have hotel 1 come before hotel 2.
                retVal = -1;
            } else if (hotel2.prefRankI != null) {
                // No preference rank for hotel1, have hotel 1 come after hotel 2.
                retVal = 1;
            }
            break;
        case DESCENDING:
            if (hotel1.prefRankI != null && hotel2.prefRankI != null) {
                if (hotel1.prefRankI < hotel2.prefRankI) {
                    retVal = 1;
                } else if (hotel1.prefRankI.intValue() == hotel2.prefRankI.intValue()) {
                    retVal = 0;
                } else if (hotel1.prefRankI > hotel2.prefRankI) {
                    retVal = -1;
                }
            } else if (hotel1.prefRankI != null) {
                // No preference rank for hotel2, have hotel 1 come before hotel 2.
                retVal = -1;
            } else if (hotel2.prefRankI != null) {
                // No preference rank for hotel1, have hotel 1 come after hotel 2.
                retVal = 1;
            }
            break;
        }
        return retVal;
    }

    /**
     * Compares two hotel choices based on their recommendation.
     * 
     * @param hotel1
     *            the first hotel.
     * @param hotel2
     *            the second hotel.
     * @return
     */
    public int compareRecommendation(HotelChoice hotel1, HotelChoice hotel2) {
        int retVal = 0;
        if (hotel1.recommendationSource != null && hotel2.recommendationSource != null) {
            // hotel with high score comes first
            if (hotel1.recommendationScore < hotel2.recommendationScore) {
                retVal = 1;
            } else if (hotel1.recommendationScore == hotel2.recommendationScore) {
                retVal = 0;
            } else if (hotel1.recommendationScore > hotel2.recommendationScore) {
                retVal = -1;
            }
        } else if (hotel1.recommendationSource != null) {
            // No recommendation for hotel2, have hotel 1 come before hotel 2.
            retVal = -1;
        } else if (hotel2.recommendationSource != null) {
            // No recommendation for hotel1, have hotel 1 come after hotel 2.
            retVal = 1;
        }
        return retVal;
    }
}
