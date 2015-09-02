package com.concur.mobile.platform.expenseit;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ExpenseItNote implements Serializable {

    private static final long serialVersionUID = 5217251769624626888L;

    @SerializedName("expense")
    private ExpenseNote expense;

    public ExpenseNote getNote() {
        return expense;
    }

    public void setInfo(String note, Long id) {
        ExpenseNote exNote = new ExpenseNote();
        exNote.setNote(note);
        exNote.setId(id);
        this.expense = exNote;
    }

    public class ExpenseNote {

        @SerializedName("id")
        private Long id;

        @SerializedName("note")
        private String note;

        public ExpenseNote() {
            id = 0L;
            note = "";
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
