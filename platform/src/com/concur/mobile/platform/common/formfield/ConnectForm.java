package com.concur.mobile.platform.common.formfield;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * A Form object which contains n fields, deserialized through Gson
 * 
 * @author OlivierB
 */
public class ConnectForm {

    @SerializedName("Items")
    private List<ConnectFormField> fieldsList;
    

    public ConnectForm() {
        // No-args constructor.
    }
    
    public List<ConnectFormField> getFormFields(){
        return fieldsList;
    }

    public void add(ConnectFormField field) {
        if (fieldsList == null)
            fieldsList = new ArrayList<ConnectFormField>();
        fieldsList.add(field);
        
    }
}
