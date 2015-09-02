package com.concur.mobile.platform.ExpenseIt;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.ExpenseIt.test.VerifyExpenseItPostNoteResult;
import com.concur.mobile.platform.expenseit.ExpenseItNote;
import com.concur.mobile.platform.expenseit.PostExpenseItNoteAsyncTask;
import com.concur.mobile.platform.test.AsyncRequestTest;
import com.concur.mobile.platform.test.PlatformTestApplication;

/**
 * @author Elliott Jacobsen-Watts
 */
public class PostExpenseItNoteTaskTest extends AsyncRequestTest {

    public PostExpenseItNoteTaskTest(boolean useMockServer) {
        super(useMockServer);
    }

    @Override
    public void doTest() throws Exception {

        Context context = PlatformTestApplication.getApplication();

        BaseAsyncResultReceiver noteReceiver = new BaseAsyncResultReceiver(getHander());
        noteReceiver.setListener(new AsyncReplyListenerImpl());

        PostExpenseItNoteAsyncTask task = new PostExpenseItNoteAsyncTask(context,
                0, noteReceiver, getNote());

        VerifyExpenseItPostNoteResult verifier = new VerifyExpenseItPostNoteResult();
        runTest("expenseIt/PostExpenseItNoteResponse.json", task, verifier);
    }

    protected ExpenseItNote getNote() {

        String comment = "ConcurMobile";

        ExpenseItNote note = new ExpenseItNote();
        note.setComment(comment);

        return note;
    }
}
