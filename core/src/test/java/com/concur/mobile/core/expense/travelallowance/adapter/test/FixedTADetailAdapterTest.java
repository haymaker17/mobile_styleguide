package com.concur.mobile.core.expense.travelallowance.adapter.test;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.adapter.FixedTADetailAdapter;
import com.concur.mobile.core.expense.travelallowance.datamodel.ICode;
import com.concur.mobile.core.expense.travelallowance.datamodel.MealProvision;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import testconfig.CoreTestApplication;
import testconfig.CoreTestRunner;

/**
 * Created by d049515 on 17.10.2015.
 */
@Config(application = CoreTestApplication.class, manifest = "AndroidManifest.xml", sdk = 21)
@RunWith(CoreTestRunner.class)
public class FixedTADetailAdapterTest extends TestCase {


    private FixedTADetailAdapter adapter;

    private RecyclerView recyclerView;


    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        recyclerView = new RecyclerView(RuntimeEnvironment.application);
        recyclerView.setLayoutManager(new LinearLayoutManager(RuntimeEnvironment.application));
    }


    @Test
    public void testSwitch() {
        FixedTADetailAdapter.ValueHolder vh = new FixedTADetailAdapter.ValueHolder();
        vh.tag = "test";
        vh.rowType = FixedTADetailAdapter.RowType.SWITCH;
        vh.isChecked = true;
        vh.label = "test label";
        List<FixedTADetailAdapter.ValueHolder> values = new ArrayList<>();
        values.add(vh);

        adapter = new FixedTADetailAdapter(RuntimeEnvironment.application, values);
        recyclerView.setAdapter(adapter);
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);

        FixedTADetailAdapter.ViewHolder viewHolder = (FixedTADetailAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(0);
        assertNotNull(viewHolder);
        assertTrue(viewHolder.spinner.getVisibility() == View.GONE);
        assertTrue(viewHolder.label.getVisibility() == View.GONE);
        assertTrue(viewHolder.readOnlyValue.getVisibility() == View.GONE);
        assertTrue(viewHolder.icon.getVisibility() == View.GONE);
        assertTrue(viewHolder.switchView.getVisibility() == View.VISIBLE);

        assertTrue(viewHolder.switchView.isChecked());
        assertEquals("test label", viewHolder.switchView.getText());
        assertEquals("test", viewHolder.switchView.getTag());
        assertTrue(viewHolder.switchView.isEnabled());

        assertEquals(RuntimeEnvironment.application.getString(R.string.general_yes), viewHolder.switchView.getTextOn());
        assertEquals(RuntimeEnvironment.application.getString(R.string.general_no), viewHolder.switchView.getTextOff());
    }

    @Test
    public void testReadOnlySwitch() {
        FixedTADetailAdapter.ValueHolder vh = new FixedTADetailAdapter.ValueHolder();
        vh.tag = "test";
        vh.rowType = FixedTADetailAdapter.RowType.SWITCH;
        vh.isChecked = true;
        vh.label = "test label";
        vh.isReadOnly = true;
        List<FixedTADetailAdapter.ValueHolder> values = new ArrayList<>();
        values.add(vh);

        adapter = new FixedTADetailAdapter(RuntimeEnvironment.application, values);
        recyclerView.setAdapter(adapter);
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);

        FixedTADetailAdapter.ViewHolder viewHolder = (FixedTADetailAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(0);
        assertNotNull(viewHolder);
        assertTrue(viewHolder.spinner.getVisibility() == View.GONE);
        assertTrue(viewHolder.label.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.readOnlyValue.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.icon.getVisibility() == View.GONE);
        assertTrue(viewHolder.switchView.getVisibility() == View.GONE);


        assertEquals(RuntimeEnvironment.application.getString(R.string.general_yes), viewHolder.readOnlyValue.getText());
        assertEquals("test label", viewHolder.label.getText());
        assertEquals("test", viewHolder.switchView.getTag());
    }

    @Test
    public void testReadOnlySpinner() {
        List<ICode> codeList = new ArrayList<>();
        codeList.add(new MealProvision("meal1", "meal 1 provision"));
        codeList.add(new MealProvision("meal2", "meal 2 provision"));
        codeList.add(new MealProvision("meal3", "meal 3 provision"));
        FixedTADetailAdapter.ValueHolder vh = new FixedTADetailAdapter.ValueHolder();
        vh.tag = "test";
        vh.rowType = FixedTADetailAdapter.RowType.SPINNER;
        vh.label = "test label";
        vh.spinnerValues = codeList;
        vh.selectedSpinnerPosition = 1;
        vh.isReadOnly = true;
        List<FixedTADetailAdapter.ValueHolder> values = new ArrayList<>();
        values.add(vh);

        adapter = new FixedTADetailAdapter(RuntimeEnvironment.application, values);
        recyclerView.setAdapter(adapter);
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);

        FixedTADetailAdapter.ViewHolder viewHolder = (FixedTADetailAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(0);
        assertNotNull(viewHolder);
        assertTrue(viewHolder.spinner.getVisibility() == View.GONE);
        assertTrue(viewHolder.label.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.readOnlyValue.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.icon.getVisibility() == View.GONE);
        assertTrue(viewHolder.switchView.getVisibility() == View.GONE);

        assertEquals("test label", viewHolder.label.getText());
        assertEquals("test", viewHolder.spinner.getTag());

        assertEquals("meal 2 provision", viewHolder.readOnlyValue.getText());
    }

    @Test
    public void testSpinner() {
        List<ICode> codeList = new ArrayList<>();
        codeList.add(new MealProvision("meal1", "meal 1 provision"));
        codeList.add(new MealProvision("meal2", "meal 2 provision"));
        codeList.add(new MealProvision("meal3", "meal 3 provision"));
        FixedTADetailAdapter.ValueHolder vh = new FixedTADetailAdapter.ValueHolder();
        vh.tag = "test";
        vh.rowType = FixedTADetailAdapter.RowType.SPINNER;
        vh.label = "test label";
        vh.spinnerValues = codeList;
        vh.selectedSpinnerPosition = 1;
        List<FixedTADetailAdapter.ValueHolder> values = new ArrayList<>();
        values.add(vh);

        adapter = new FixedTADetailAdapter(RuntimeEnvironment.application, values);
        recyclerView.setAdapter(adapter);
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);

        FixedTADetailAdapter.ViewHolder viewHolder = (FixedTADetailAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(0);
        assertNotNull(viewHolder);
        assertTrue(viewHolder.spinner.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.label.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.readOnlyValue.getVisibility() == View.GONE);
        assertTrue(viewHolder.icon.getVisibility() == View.GONE);
        assertTrue(viewHolder.switchView.getVisibility() == View.GONE);

        assertEquals("test label", viewHolder.label.getText());
        assertEquals("test", viewHolder.spinner.getTag());

        assertTrue(viewHolder.spinner.getSelectedItemPosition() == 1);
        assertEquals(new MealProvision("meal2", "meal 2 provision"), (MealProvision)viewHolder.spinner.getSelectedItem());
        assertEquals(3, viewHolder.spinner.getAdapter().getCount());
    }

    @Test
    public void testSpinnerMultiSelect() {
        List<ICode> codeList = new ArrayList<>();
        codeList.add(new MealProvision("meal1", "meal 1 provision"));
        codeList.add(new MealProvision("meal2", "meal 2 provision"));
        codeList.add(new MealProvision("meal3", "meal 3 provision"));
        FixedTADetailAdapter.ValueHolder vh = new FixedTADetailAdapter.ValueHolder();
        vh.tag = "test";
        vh.rowType = FixedTADetailAdapter.RowType.SPINNER;
        vh.label = "test label";
        vh.spinnerValues = codeList;
        vh.selectedSpinnerPosition = 1;
        vh.multiValuesSelected = true;
        List<FixedTADetailAdapter.ValueHolder> values = new ArrayList<>();
        values.add(vh);

        adapter = new FixedTADetailAdapter(RuntimeEnvironment.application, values);
        recyclerView.setAdapter(adapter);
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);

        FixedTADetailAdapter.ViewHolder viewHolder = (FixedTADetailAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(0);
        assertNotNull(viewHolder);
        assertTrue(viewHolder.spinner.getVisibility() == View.GONE);
        assertTrue(viewHolder.label.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.readOnlyValue.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.icon.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.switchView.getVisibility() == View.GONE);

        assertEquals("test label", viewHolder.label.getText());
        assertEquals(RuntimeEnvironment.application.getString(R.string.ta_multiple_values), viewHolder.readOnlyValue.getText());

    }

    @Test
    public void testMultiSelectSwitch() {
        FixedTADetailAdapter.ValueHolder vh = new FixedTADetailAdapter.ValueHolder();
        vh.tag = "test";
        vh.rowType = FixedTADetailAdapter.RowType.SWITCH;
        vh.isChecked = true;
        vh.label = "test label";
        vh.multiValuesSelected = true;
        List<FixedTADetailAdapter.ValueHolder> values = new ArrayList<>();
        values.add(vh);

        adapter = new FixedTADetailAdapter(RuntimeEnvironment.application, values);
        recyclerView.setAdapter(adapter);
        recyclerView.measure(0, 0);
        recyclerView.layout(0, 0, 100, 10000);

        FixedTADetailAdapter.ViewHolder viewHolder = (FixedTADetailAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(0);
        assertNotNull(viewHolder);
        assertTrue(viewHolder.spinner.getVisibility() == View.GONE);
        assertTrue(viewHolder.label.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.readOnlyValue.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.icon.getVisibility() == View.VISIBLE);
        assertTrue(viewHolder.switchView.getVisibility() == View.GONE);


        assertEquals(RuntimeEnvironment.application.getString(R.string.ta_multiple_values), viewHolder.readOnlyValue.getText());
        assertEquals("test label", viewHolder.label.getText());
        assertEquals("test", viewHolder.switchView.getTag());
    }

}
