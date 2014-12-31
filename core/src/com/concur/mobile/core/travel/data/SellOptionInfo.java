package com.concur.mobile.core.travel.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.ControlType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.DataType;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.InputType;
import com.concur.mobile.core.view.SpinnerItem;
import com.concur.mobile.platform.util.Parse;

public class SellOptionInfo implements Serializable {

    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = -1410921672115892902L;

    private String instructions;
    private List<SellOptionGroup> optionGroups = new ArrayList<SellOptionGroup>();
    private SellOptionGroup optionGroup;
    // private List<SellOptionItem> optionItems;
    // private SellOptionItem optionItem;
    // private List<String> remarks;
    private boolean inRemarks;
    private StringBuilder remarkStrBldr;

    // for select list items
    private boolean inSelectItem;
    private List<TravelCustomFieldValueSpinnerItem> spnItems;
    // private List<SpinnerItem> spnItems;
    private TravelCustomFieldValueSpinnerItem spnItem;
    // private SpinnerItem spnItem;

    private SellOptionField tcf;
    private List<SellOptionField> tcfs;

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<SellOptionGroup> getOptionGroups() {
        return optionGroups;
    }

    public void setOptionGroups(List<SellOptionGroup> optionGroups) {
        this.optionGroups = optionGroups;
    }

    public void startTag(String tag) {
        if (tag.equalsIgnoreCase("OptionGroup")) {
            optionGroup = new SellOptionGroup();
        } else if (tag.equalsIgnoreCase("OptionItems")) {
            // optionItems = new ArrayList<SellOptionItem>();
            tcfs = new ArrayList<SellOptionField>();
        } else if (tag.equalsIgnoreCase("OptionItem")) {
            tcf = new SellOptionField();
            tcf.setInputType(InputType.USER);
            // optionItem = new SellOptionItem();
        } else if (tag.equalsIgnoreCase("Remarks")) {
            inRemarks = true;
            // remarks = new ArrayList<String>();
            remarkStrBldr = new StringBuilder();
        } else if (tag.equalsIgnoreCase("SelectList")) {
            spnItems = new ArrayList<TravelCustomFieldValueSpinnerItem>();
            spnItems.add(new TravelCustomFieldValueSpinnerItem("", ""));
        } else if (tag.equalsIgnoreCase("SelectItem")) {
            inSelectItem = true;
            spnItem = new TravelCustomFieldValueSpinnerItem("", "");
        }
    }

    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("Instructions")) {
            instructions = cleanChars;
        } else if (localName.equalsIgnoreCase("ID")) {
            // optionItem.setId(cleanChars);
            // tcf.setId(cleanChars);
        } else if (localName.equalsIgnoreCase("Optional")) {
            // optionItem.setOptional();
            tcf.setRequired(!Parse.safeParseBoolean(cleanChars));
        } else if (localName.equalsIgnoreCase("Visible")) {
            // optionItem.setVisible();
            if (Parse.safeParseBoolean(cleanChars)) {
                tcf.setAccessType(AccessType.RW);
            } else {
                tcf.setAccessType(AccessType.HD);
            }
        } else if (localName.equalsIgnoreCase("Name")) {
            // optionItem.setName(cleanChars);
        } else if (localName.equalsIgnoreCase("OptionItemId")) {
            // optionItem.setOptionItemId(cleanChars);
            tcf.setId(cleanChars);
        } else if (localName.equalsIgnoreCase("Selected")) {
            // optionItem.setSelected(FormatUtil.safeParseBoolean(cleanChars));
        } else if (localName.equalsIgnoreCase("TextFieldValue")) {
            // optionItem.setValue(cleanChars);
            tcf.setValue(cleanChars);
        } else if (localName.equalsIgnoreCase("UIInputType")) {
            // optionItem.setUiType(cleanChars); // TODO - have an enum
            if (cleanChars != null) {
                if (cleanChars.equals("SELECT_LIST")) {
                    tcf.setControlType(ControlType.PICK_LIST);
                } else if (cleanChars.equals("TEXT_FIELD")) {
                    tcf.setControlType(ControlType.EDIT);
                } else {
                    // default to text field
                    tcf.setControlType(ControlType.EDIT);
                }
                // TODO - handle here all the cases
            }
            tcf.setDataType(DataType.VARCHAR); // TODO - handle here all the cases
        } else if (inRemarks && localName.equalsIgnoreCase("Remark")) {
            // remarks.add(cleanChars);
            remarkStrBldr.append(cleanChars);
        } else if (inSelectItem) {
            // if (localName.equalsIgnoreCase("name")) {
            // spnItem.name = cleanChars;
            // spnItem.optionText = cleanChars;
            // spnItem.value = cleanChars;
            // } else
            if (localName.equalsIgnoreCase("displayValue")) {
                spnItem.name = cleanChars;
                spnItem.optionText = cleanChars;
                spnItem.value = cleanChars;
            } else if (localName.equalsIgnoreCase("realValue")) {
                spnItem.id = cleanChars;
                spnItem.valueId = cleanChars;
                // tcf.setLiKey(cleanChars);
            }
        }
    }

    public void endTag(String tag) {
        if (tag.equalsIgnoreCase("Remark")) {
            inRemarks = false;
            // optionItem.setRemarks(remarks);
            tcf.setLabel(remarkStrBldr.toString());
        } else if (tag.equalsIgnoreCase("OptionItem")) {
            // optionItems.add(optionItem);
            tcfs.add(tcf);
        } else if (tag.equalsIgnoreCase("OptionItems")) {
            // optionGroup.setOptionItems(optionItems);
            optionGroup.setSellOptionFields(tcfs);
        } else if (tag.equalsIgnoreCase("OptionGroup")) {
            optionGroups.add(optionGroup);
        } else if (tag.equalsIgnoreCase("SelectItem")) {
            inSelectItem = false;
            spnItems.add(spnItem);
        } else if (tag.equalsIgnoreCase("SelectList")) {
            tcf.setFieldValues(spnItems);
            tcf.setStaticList((SpinnerItem[]) tcf.getFieldValues().toArray(new TravelCustomFieldValueSpinnerItem[0]));
            // tcf.setStaticList(spnItems.toArray(new SpinnerItem[spnItems.size()]));
        }
    }

    // retrieve the travel custom fields in this sell option info
    public List<SellOptionField> getSellOptionFields() {
        List<SellOptionField> tcfsInOptionInfo = new ArrayList<SellOptionField>();
        for (SellOptionGroup optGroup : optionGroups) {
            tcfsInOptionInfo.addAll(optGroup.getSellOptionFields());
        }
        return tcfsInOptionInfo;
    }
}
