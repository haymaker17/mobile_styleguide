package com.concur.mobile.core.expense.travelallowance.ui.model;

/**
 * Instances of this class can be used as tags to be assigned to views. Originally it was
 * built to add information to views used in list views, where the list view items contain
 * clickable subviews. In order to be able to execute the click event with reference to the
 * position within the list one can add an instance of this class to the corresponding
 * clickable subview. This can be done using a qualified tag (see also res/values/tagkeys.xml
 * tag_key_position) carrying an instance of this class. In your list adapter you might do so
 * in method getView. The listener needs to get the tag and hence gets the knowledge about the
 * list row position.
 *
 * Created by Michael Becherer on 10-Jul-15.
 */
public class PositionInfoTag {

    public final static int INFO_NONE = 0;
    public final static int INFO_OUTBOUND = -1;
    public final static int INFO_INBOUND = 1;

    /**
     * The position (respectively the list row index)
     */
    int position;

    /**
     * Additional information provided to the tag instance. You might use one of the public
     * class constants such as {@link #INFO_NONE}
     */
    int info;

    /**
     * Creates an instance of this class
     * @param position {@link #position}
     * @param info {@link #info}
     */
    public PositionInfoTag(int position, int info) {
        this.position = position;
        this.info = info;
    }

    /**
     * Creates an instance of this class
     * @param position {@link #position}
     */
    public PositionInfoTag(int position) {
        this.position = position;
        this.info = INFO_NONE;
    }

    /**
     * Getter
     * @return {@link #position}
     */
    public int getPosition() {
        return position;
    }

    /**
     * Getter
     * @return {@link #info}
     */
    public int getInfo() {
        return info;
    }
}
