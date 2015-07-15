package com.concur.mobile.core.expense.travelallowance.datamodel;

/**
 * Enumeration of all possible states of a domain object regarding its
 * synchronization with an arbitrary synchronization target, for example a
 * backend system or a device storage.
 *
 * Created by Michael Becherer on 14-Jul-15.
 */
public enum SynchronizationStatusEnum {

    /**
     * Indicates that a domain object is in sync with its synchronization
     * target.
     */
    //TODO: Set string resource as soon as available
    SYNCHRONIZED("SUCCESS", //R.string.status_synchronized)
                  0),

    /**
     * Indicates that the synchronization of a domain object failed target and
     * needs to be refreshed.
     */
    //TODO: Set string resource as soon as available
    FAILED("FAILED", //R.string.status_failed)
            0);

    /**
     * The text resource id associated to this {@code SynchronizationStatus}.
     * The resource - if resolved - holds a human readable description.
     * @see #getTextResourceId()
     */
    private int textResourceId;

    /**
     * The coded representations
     */
    private String code;

    /**
     * Creates a new {@code SynchronizationStatus} instance associated to
     * the given {@code textResourceId}.
     *
     * @param textResourceId The text resource id
     *
     */
    private SynchronizationStatusEnum(final String code, final int textResourceId) {
        this.code = code;
        this.textResourceId = textResourceId;
    }

    /**
     * Gets the text resource id associated to this
     * {@code SynchronizationStatus}.
     *
     * @return the associated {@link #textResourceId}
     *
     */
    public int getTextResourceId() {
        return this.textResourceId;
    }

    /**
     * Gets the {@code SynchronizationStatusEnum} matching the given
     * code.
     *
     * @param code the code to get the matching {@code SynchronizationStatusEnum} for
     * @return the {@code SynchronizationStatusEnum} matching the given code;
     *         null, if no matching {@code SynchronizationStatusEnum} could be found
     */
    public static SynchronizationStatusEnum fromCode(final String code) {
        for (SynchronizationStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
