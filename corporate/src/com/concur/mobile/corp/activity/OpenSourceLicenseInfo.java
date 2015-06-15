package com.concur.mobile.corp.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.concur.breeze.R;
import com.concur.mobile.platform.base.OpenSourceLicenseAdapter;
import com.concur.mobile.platform.base.OpenSourceLicenseListItem;
import com.concur.mobile.platform.base.OpenSourceLicenseUtils;

/**
 * Creates a list of {@link OpenSourceLicenseListItem} items, showing the primary items used in Platform as well as any additional
 * licenses we use. Places that list into a ListView and sets up an adapter, which when an item is clicked, will show the
 * specified license.
 * 
 * @author westonw
 * 
 */
public class OpenSourceLicenseInfo extends FragmentActivity {

    private ListView mOpenSrcLicListView;
    private List<OpenSourceLicenseListItem> mOpenSrcLicListItems;

    private String LICENSE_TITLE_TAG = "license.title.tag";
    private String LICENSE_TEXT_TAG = "license.text.tag";

    public OpenSourceLicenseInfo() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.open_source_license_info);

        mOpenSrcLicListView = (ListView) findViewById(R.id.openSourceLicenseListView);
        mOpenSrcLicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }

        });

        mOpenSrcLicListItems = OpenSourceLicenseUtils.getPlatformOpenSrcLicListItems();

        // This appends additional licenses to the existing Platform used licenses.
        mOpenSrcLicListItems.add(new OpenSourceLicenseListItem(R.raw.apptentive_license, "Apptentive License",
                new String[] { "Concur" }));

        OpenSourceLicenseAdapter adapter = new OpenSourceLicenseAdapter(this, R.layout.open_source_license_list_item,
                R.id.licenseName, R.id.usedBy, mOpenSrcLicListItems);

        mOpenSrcLicListView.setAdapter(adapter);

        super.onCreate(savedInstanceState);
    }

    /**
     * Gets the {@link OpenSourceLicenseListItem} for the given position, reads the license file for the current item's file
     * resource id, then launches a new activity to display the license info.
     * 
     * @param position
     *            The position of the item clicked.
     */
    private void selectItem(int position) {
        if (mOpenSrcLicListItems != null && mOpenSrcLicListItems.size() > position) {
            final OpenSourceLicenseListItem currentItem = mOpenSrcLicListItems.get(position);

            if (currentItem != null) {
                final String licenseText = OpenSourceLicenseUtils.readLicenseFile(getResources(),
                        currentItem.getFileResId());

                Intent intent = new Intent(OpenSourceLicenseInfo.this, OpenSourceLicenseDisplay.class);
                intent.putExtra(LICENSE_TEXT_TAG, licenseText);
                intent.putExtra(LICENSE_TITLE_TAG, currentItem.getLicenseName());
                startActivity(intent);
            }
        }
    }

}
