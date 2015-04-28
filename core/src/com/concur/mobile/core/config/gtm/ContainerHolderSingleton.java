package com.concur.mobile.core.config.gtm;

import com.google.android.gms.tagmanager.ContainerHolder;

public class ContainerHolderSingleton {

    private static ContainerHolder containerHolder;

    /**
     * Utility class; don't instantiate.
     */
    private ContainerHolderSingleton() {
    }

    public static ContainerHolder getContainerHolder() {
        return containerHolder;
    }

    public static void setContainerHolder(ContainerHolder c) {
        containerHolder = c;
    }
}
