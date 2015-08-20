package com.concur.mobile.platform.common.formfield;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * A Form object which contains n fields, deserialized through Gson
 *
 * @author OlivierB
 */
public class ConnectForm {

    @SerializedName("Fields") private List<ConnectFormField> fieldsList;
    @SerializedName("ID") private String id;

    public ConnectForm() {
        // No-args constructor.
    }

    public List<ConnectFormField> getFormFields() {
        return fieldsList;
    }

    public String getId() {
        return id;
    }

    public void add(ConnectFormField field) {
        if (fieldsList == null)
            fieldsList = new ArrayList<ConnectFormField>();
        fieldsList.add(field);

    }

    // Get Field  by Name
    public ConnectFormField getConnectFormFieldByName(ConnectFormField.NameType nameType) {
        List<ConnectFormField> connectFormFieldList = this.getFormFields();
        ConnectFormField connectFormField=null;
        for (int i = 0; i < connectFormFieldList.size(); i++) {
            if(connectFormFieldList.get(i).getName().equals(nameType.getValue())) {
                connectFormField = connectFormFieldList.get(i);
            }
        }
        return connectFormField;
    }
}
