package com.concur.mobile.platform.travel.search.hotel;

import java.util.Comparator;

/**
 * An implementation of <code>Comparator</code> for comparing <code>Hotel</code> objects based on different attributes.
 * 
 * @author RatanK
 */
public class HotelComparator implements Comparator<Hotel> {

    // Defines which field to sort on.
    public enum CompareField {
        DISTANCE, // compare on distance.
        STAR_RATING, // compare on star rating.
        CHEAPEST_ROOM, // compare on cheapest room rate.
        // NAME, // compare on hotel name.
        PREFERENCE, // compare on preference rank.
        RECOMMENDATION // compare on hotel recommendation i.e Suggestion
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
     * Constructs an instance of <code>HotelComparator</code> that orders by ascending cheapest room.
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
    public int compare(Hotel hotel1, Hotel hotel2) {
        int retVal = 0;
        if (hotel1 != hotel2) {
            switch (compField) {
            case CHEAPEST_ROOM:
                retVal = compareCheapestRoom(hotel1, hotel2);
                break;
            case DISTANCE:
                retVal = compareDistance(hotel1, hotel2);
                break;
            case STAR_RATING:
                retVal = compareStarRating(hotel1, hotel2);
                break;
            case PREFERENCE:
                retVal = comparePreference(hotel1, hotel2);
                break;
            }
        } else {
            retVal = 0;
        }
        return retVal;
    }

    /**
     * Compares two hotels based on cheapest room.
     * 
     * @param hotel1
     *            the first hotel.
     * @param hotel2
     *            the second hotel.
     * @return
     */
    public int compareCheapestRoom(Hotel hotel1, Hotel hotel2) {
        int retVal = 0;

        // Obtain cheapest room rate for 'hotel1'.
        Double hotel1Price = null;
        if (hotel1.lowestRate != null) {
            hotel1Price = hotel1.lowestRate;
        }
        // Obtain cheapest room rate for 'hotel2'.
        Double hotel2Price = null;
        if (hotel2.lowestRate != null) {
            hotel2Price = hotel2.lowestRate;
        }

        switch (compOrder) {
        case ASCENDING:
            if (hotel1Price != null && hotel2Price != null) {
                if (hotel1Price < hotel2Price) {
                    retVal = -1;
                } else if (hotel1Price == hotel2Price) {
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
                } else if (hotel1Price == hotel2Price) {
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
     * Compares two hotels based on distance.
     * 
     * @param hotel1
     *            the first hotel.
     * @param hotel2
     *            the second hotel.
     * @return
     */
    public int compareDistance(Hotel hotel1, Hotel hotel2) {
        int retVal = 0;

        switch (compOrder) {
        case ASCENDING:
            if (hotel1.distance != null && hotel2.distance != null) {
                if (hotel1.distance < hotel2.distance) {
                    retVal = -1;
                } else if (hotel1.distance == hotel2.distance) {
                    retVal = 0;
                } else if (hotel1.distance > hotel2.distance) {
                    retVal = 1;
                }
            } else if (hotel1.distance != null) {
                // No distance for hotel2! Have hotel1 come before hotel2.
                retVal = -1;
            } else if (hotel2.distance != null) {
                // No distance for hotel1! Have hotel2 come before hotel1.
                retVal = 1;
            }
            break;
        case DESCENDING:
            if (hotel1.distance != null && hotel2.distance != null) {
                if (hotel1.distance < hotel2.distance) {
                    retVal = 1;
                } else if (hotel1.distance == hotel2.distance) {
                    retVal = 0;
                } else if (hotel1.distance > hotel2.distance) {
                    retVal = -1;
                }
            } else if (hotel1.distance != null) {
                // No distance for hotel2! Have hotel1 come before hotel2.
                retVal = -1;
            } else if (hotel2.distance != null) {
                // No distance for hotel1! Have hotel2 come before hotel1.
                retVal = 1;
            }
            break;
        }
        return retVal;
    }

    /**
     * Compares two hotels based on star rating.
     * 
     * @param hotel1
     *            the first hotel.
     * @param hotel2
     *            the second hotel.
     * @return
     */
    public int compareStarRating(Hotel hotel1, Hotel hotel2) {
        int retVal = 0;
        switch (compOrder) {
        case ASCENDING:
            if (hotel1.preferences != null && hotel1.preferences.starRating != null && hotel2.preferences != null
                    && hotel2.preferences.starRating != null) {
                int starRating1 = Integer.parseInt(hotel1.preferences.starRating);
                int starRating2 = Integer.parseInt(hotel2.preferences.starRating);
                if (starRating1 < starRating2) {
                    retVal = -1;
                } else if (starRating1 == starRating2) {
                    retVal = 0;
                } else if (starRating1 > starRating2) {
                    retVal = 1;
                }
            } else if (hotel1.preferences != null && hotel1.preferences.starRating != null) {
                // No star rating for hotel2, have hotel 1 come before hotel 2.
                retVal = -1;
            } else if (hotel2.preferences != null && hotel2.preferences.starRating != null) {
                // No star rating for hotel1, have hotel 1 come after hotel 2.
                retVal = 1;
            }
            break;
        case DESCENDING:
            if (hotel1.preferences != null && hotel1.preferences.starRating != null && hotel2.preferences != null
                    && hotel2.preferences.starRating != null) {
                int starRating1 = Integer.parseInt(hotel1.preferences.starRating);
                int starRating2 = Integer.parseInt(hotel2.preferences.starRating);
                if (starRating1 < starRating2) {
                    retVal = 1;
                } else if (starRating1 == starRating2) {
                    retVal = 0;
                } else if (starRating1 > starRating2) {
                    retVal = -1;
                }
            } else if (hotel1.preferences != null && hotel1.preferences.starRating != null) {
                // No star rating for hotel2, have hotel 1 come before hotel 2.
                retVal = -1;
            } else if (hotel2.preferences != null && hotel2.preferences.starRating != null) {
                // No star rating for hotel1, have hotel 1 come after hotel 2.
                retVal = 1;
            }
            break;
        }
        return retVal;
    }

    /**
     * Compares two hotels based on their preference rank values.
     * 
     * @param hotel1
     *            the first hotel.
     * @param hotel2
     *            the second hotel.
     * @return
     */
    public int comparePreference(Hotel hotel1, Hotel hotel2) {
        int retVal = 0;

        // Obtain hotel1's preference string for comparison.
        String hotel1PreferredStr = null;
        if (hotel1.preferences != null && hotel1.preferences.companyPreference != null) {
            hotel1PreferredStr = hotel1.preferences.companyPreference;
        }

        // Obtain hotel2's preference string for comparison.
        String hotel2PreferredStr = null;
        if (hotel2.preferences != null && hotel2.preferences.companyPreference != null) {
            hotel2PreferredStr = hotel2.preferences.companyPreference;
        }

        switch (compOrder) {
        case ASCENDING:
            if (hotel1PreferredStr != null && hotel2PreferredStr != null
                    && hotel2.preferences.companyPreference != null) {
                if (hotel1PreferredStr.equals("Preferred")) {
                    retVal = 1;
                } else if (hotel1PreferredStr == hotel2PreferredStr) {
                    retVal = 0;
                } else if (hotel2PreferredStr.equals("Preferred")) {
                    retVal = -1;
                }
            } else if (hotel1PreferredStr != null) {
                // No preference for hotel2, have hotel 1 come before hotel 2.
                retVal = -1;
            } else if (hotel2PreferredStr != null) {
                // No preference for hotel1, have hotel 1 come after hotel 2.
                retVal = 1;
            }
            break;
        case DESCENDING:
            if (hotel1PreferredStr != null && hotel2PreferredStr != null
                    && hotel2.preferences.companyPreference != null) {
                if (hotel1PreferredStr.equals("Preferred")) {
                    retVal = -1;
                } else if (hotel1PreferredStr == hotel2PreferredStr) {
                    retVal = 0;
                } else if (hotel2PreferredStr.equals("Preferred")) {
                    retVal = 1;
                }
            } else if (hotel1PreferredStr != null) {
                // No preference for hotel2, have hotel 1 come before hotel 2.
                retVal = -1;
            } else if (hotel2PreferredStr != null) {
                // No preference for hotel1, have hotel 1 come after hotel 2.
                retVal = 1;
            }
            break;
        }
        return retVal;
    }

    /**
     * Compares two hotels based on their recommendation i.e suggestion
     * 
     * @param hotel1
     *            the first hotel.
     * @param hotel2
     *            the second hotel.
     * @return
     */
    public int compareRecommendation(Hotel hotel1, Hotel hotel2) {
        int retVal = 0;
        if (hotel1.recommended != null && hotel1.recommended.totalScore != null && hotel2.recommended != null
                && hotel2.recommended.totalScore != null) {
            // hotel with high score comes first
            if (hotel1.recommended.totalScore < hotel2.recommended.totalScore) {
                retVal = 1;
            } else if (hotel1.recommended.totalScore == hotel2.recommended.totalScore) {
                retVal = 0;
            } else if (hotel1.recommended.totalScore > hotel2.recommended.totalScore) {
                retVal = -1;
            }
        } else if (hotel1.recommended != null && hotel1.recommended.totalScore != null) {
            // No recommendation for hotel2, have hotel 1 come before hotel 2.
            retVal = -1;
        } else if (hotel2.recommended != null && hotel2.recommended.totalScore != null) {
            // No recommendation for hotel1, have hotel 2 come before hotel 1.
            retVal = 1;
        }
        return retVal;
    }

}
