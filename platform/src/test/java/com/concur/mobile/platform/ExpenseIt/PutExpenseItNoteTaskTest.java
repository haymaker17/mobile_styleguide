package com.concur.mobile.platform.ExpenseIt;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.ExpenseIt.test.VerifyExpenseItPutNoteResult;
import com.concur.mobile.platform.expenseit.ExpenseItNote;
import com.concur.mobile.platform.expenseit.PutExpenseItNoteAsyncTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;

/**
 * @author Elliott Jacobsen-Watts
 */
public class PutExpenseItNoteTaskTest extends AsyncRequestTest {

    private Long expenseId;

    public void setExpenseId(Long id) {
        this.expenseId = id;
    }

    public Long getExpenseId() { return this.expenseId; }

    public PutExpenseItNoteTaskTest(boolean useMockServer) {
        super(useMockServer);
    }

    @Override
    public void doTest() throws Exception {

        Context context = PlatformTestApplication.getApplication();

        BaseAsyncResultReceiver noteReceiver = new BaseAsyncResultReceiver(getHander());
        noteReceiver.setListener(new AsyncReplyListenerImpl());

        PutExpenseItNoteAsyncTask task = new PutExpenseItNoteAsyncTask(context,
                0, noteReceiver, getNote());

        VerifyExpenseItPutNoteResult verifier = new VerifyExpenseItPutNoteResult();
        runTest("expenseIt/PutExpenseItNoteResponse.json", task, verifier);
    }

    protected ExpenseItNote getNote() {

        String comment = "ConcurMobile";

        ExpenseItNote note = new ExpenseItNote();
        note.setInfo(comment, expenseId);

        return note;
    }
}
