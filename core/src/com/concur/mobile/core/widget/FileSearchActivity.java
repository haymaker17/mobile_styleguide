package com.concur.mobile.core.widget;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.util.Const;

/**
 * 
 */

/**
 * @author HaroldF
 * 
 */
public class FileSearchActivity extends ActionBarActivity {

    private ListView lstSearchResults = null;
    private OnItemSelectedListener mOnItemSelectedListener = null;
    private FileSearchListAdapter mFileSearchListAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_search);

        initializeGlobals();
        setupListeners();
        setupViews();
    }

    private void initializeGlobals() {
        mFileSearchListAdapter = new FileSearchListAdapter(this);
        mFileSearchListAdapter.addSearchPattern(".pdf");
    }

    private void setupListeners() {
        mOnItemSelectedListener = new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
                Intent intent = null;

                intent = new Intent();
                intent.putExtra(Const.EXTRA_FILEPATH, ((TextView) view).getText());
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        };
    }

    private void setupViews() {
        lstSearchResults = (ListView) findViewById(R.id.lstSearchResults);
        lstSearchResults.setAdapter(mFileSearchListAdapter);
        lstSearchResults.setOnItemSelectedListener(mOnItemSelectedListener);
    }

    private class FileSearchListAdapter extends BaseAdapter {

        ArrayList<String> arySearchPatterns = null;
        ArrayList<String> aryResults = null;
        ArrayList<File> arySearchDirectories = null;

        public FileSearchListAdapter(Context context) {
            arySearchPatterns = new ArrayList<String>();
            arySearchDirectories = new ArrayList<File>();
            aryResults = new ArrayList<String>();
        }

        public void addSearchPattern(String string) {
            if (!arySearchPatterns.contains(string)) {
                arySearchPatterns.add(string);
            }

            startSearch(Environment.getExternalStorageDirectory());
        }

        @Override
        public int getCount() {
            return aryResults.size();
        }

        @Override
        public Object getItem(int position) {
            return aryResults.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewholder;
            String strFileName = null;

            if (convertView == null) {
                convertView = LayoutInflater.from(FileSearchActivity.this).inflate(
                        R.layout.listitem_file_search_results_list, null);
                viewholder = new ViewHolder(convertView);

                strFileName = (String) getItem(position);
                strFileName = strFileName.substring(strFileName.lastIndexOf('/') + 1);
                viewholder.txtvFileName.setText(strFileName);
                viewholder.txtvFilePath.setText((String) getItem(position));

                convertView.setTag(viewholder);
            } else {
                viewholder = (ViewHolder) convertView.getTag();
            }

            return convertView;
        }

        private void startSearch(File file) {
            arySearchDirectories.add(file);
            while (arySearchDirectories.size() > 0) {
                findFiles(arySearchDirectories.remove(0));
            }
        }

        public void findFiles(File fileStartPath) {
            for (File file : fileStartPath.listFiles()) {
                if (file.isDirectory()) {
                    // This cannot be recursive due to possible stack overflow
                    arySearchDirectories.add(file);
                } else if (file.isFile() && file.getPath().endsWith(".pdf")) {
                    aryResults.add(file.toString());
                }
            }
        }
    }

    private class ViewHolder {

        public final TextView txtvFileName;
        public final TextView txtvFilePath;

        public ViewHolder(final View view) {
            txtvFileName = (TextView) view.findViewById(R.id.listitem_file_search_results_list_txtvFileName);
            txtvFileName.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mOnItemSelectedListener.onItemSelected(null, v, 0, 0);
                    return;
                }
            });
            txtvFilePath = (TextView) view.findViewById(R.id.listitem_file_search_results_list_txtvFilePath);
        }
    }
}
