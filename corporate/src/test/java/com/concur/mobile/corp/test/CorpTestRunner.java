/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/
package com.concur.mobile.corp.test;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.ResourcePath;

import java.util.List;

public class CorpTestRunner extends RobolectricTestRunner {

    public CorpTestRunner(final Class<?> testClass) throws InitializationError {
        super(testClass);
    }


    @Override
    protected AndroidManifest getAppManifest(Config config) {
        String appRoot = "./";
        String manifestPath = appRoot + "AndroidManifest.xml";
        String resDir = appRoot + "res";
        String assetsDir = appRoot + "assets";

        return new AndroidManifest(Fs.fileFromPath(manifestPath), Fs.fileFromPath(resDir), Fs.fileFromPath(assetsDir)) {
            @Override
            public List<ResourcePath> getIncludedResourcePaths() {
                List<ResourcePath> paths = super.getIncludedResourcePaths();
                paths.add(new ResourcePath(getRClass(), getPackageName(), Fs.fileFromPath("res"), getAssetsDirectory()));
                paths.add(new ResourcePath(getRClass(), getPackageName(), Fs.fileFromPath("../corporate/build/intermediates/exploded-aar/com.android.support/appcompat-v7/22.2.1/res"), getAssetsDirectory()));
                return paths;
            }
        };
    }
}