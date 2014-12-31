package com.concur.mobile.platform.base;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * An Adapter used for {@link OpenSourceLicenseListItem} items. It will get the strings required from each list item and display
 * them in the provided views.
 * 
 * @author westonw
 * 
 */
public class OpenSourceLicenseAdapter extends ArrayAdapter<OpenSourceLicenseListItem> {

    private Context context;
    private int layoutResId;
    private int licenseNameResId;
    private int usedByResId;
    private List<OpenSourceLicenseListItem> openSrcLibItemsList;

    /**
     * Constructs an Adapter used for {@link OpenSourceLicenseListItem} items. The <code>resourceId</code>,
     * <code>licenseNameResId</code>, and <code>usedByResId</code> must be provided from the user's custom layout.
     * 
     * @param context
     *            The <code>Context</code> of the calling activity.
     * @param resourceId
     *            The Resource ID for the Layout of the ListItem displaying the a license.
     * @param licenseNameResId
     *            Resource ID of the <code>TextView</code> where the License Name should be displayed.
     * @param usedByResId
     *            Resource ID of the <code>TextView</code> where the list of libraries using the license is displayed.
     * @param listItems
     *            List of {@link OpenSourceLicenseListItem} items to be displayed.
     */
    public OpenSourceLicenseAdapter(Context context, int resourceId, int licenseNameResId, int usedByResId,
            List<OpenSourceLicenseListItem> listItems) {
        super(context, resourceId, listItems);

        this.context = context;
        layoutResId = resourceId;
        this.licenseNameResId = licenseNameResId;
        this.usedByResId = usedByResId;
        openSrcLibItemsList = listItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ListItemHolder listItemHolder;

        if (view == null) {
            listItemHolder = new ListItemHolder();
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();

            view = inflater.inflate(layoutResId, parent, false);

            listItemHolder.licenseName = (TextView) view.findViewById(licenseNameResId);
            listItemHolder.usedBy = (TextView) view.findViewById(usedByResId);

            view.setTag(listItemHolder);

        } else {
            listItemHolder = (ListItemHolder) view.getTag();
        }

        OpenSourceLicenseListItem openSourceLicenseListItem = (OpenSourceLicenseListItem) this.openSrcLibItemsList
                .get(position);

        // Show License Name in TextView
        listItemHolder.licenseName.setText(openSourceLicenseListItem.getLicenseName());

        // Set up list of items using License to show in TextView
        String[] usedByList = openSourceLicenseListItem.getUsedBy();
        StringBuilder usedByString = new StringBuilder();
        // Get preloaded text (IE "Used By")
        usedByString.append(listItemHolder.usedBy.getText().toString());

        if (usedByList != null && usedByList.length > 0) {
            usedByString.append(usedByList[0]);
            for (int i = 1; i < usedByList.length; i++) {
                usedByString.append(", ");
                usedByString.append(usedByList[i]);
            }
            listItemHolder.usedBy.setText(usedByString);
        }

        return view;
    }

    // A ViewHolder for ListItems
    private static class ListItemHolder {

        TextView licenseName;
        TextView usedBy;
    }

}
