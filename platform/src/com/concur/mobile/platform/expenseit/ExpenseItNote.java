package com.concur.mobile.platform.expenseit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class ExpenseItNote implements Serializable {

    private static final long serialVersionUID = 5217251769624626888L;

    @SerializedName("expense")
    private ExpenseNote expense;

    public ExpenseNote getNote() {
        return expense;
    }

    public void setComment(String note) {
        ExpenseNote exNote = new ExpenseNote();
        exNote.setNote(note);
        this.expense = exNote;
    }

    public class ExpenseNote {

        @SerializedName("id")
        private String id;

        @SerializedName("note")
        private String note;

        public ExpenseNote() {
            long milliSecs = new Date().getTime();
            id = Long.toString(milliSecs);
            note = "";
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
