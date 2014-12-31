package com.concur.mobile.platform.expense.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.concur.mobile.platform.provider.ClearSQLiteDatabase;

/**
 * Provides an extension of <code>SQLiteOpenHelper</code> to provide access to and handle upgrades for the Expense provider SQLite
 * database.
 * 
 * @author andrewk
 */
public class ClearExpenseDBHelper extends SQLiteOpenHelper {

    /**
     * Constructs an instance of <code>ClearExpenseDBHelper</code> with an application context.
     * 
     * @param context
     *            contains a reference to the application context.
     */
    public ClearExpenseDBHelper(Context context) {
        super(context, ExpenseDBSchema.DATABASE_NAME, null, ExpenseDBSchema.DATABASE_VERSION);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        ExpenseDBSchema.onCreate(new ClearSQLiteDatabase(db));
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ExpenseDBSchema.onUpgrade(new ClearSQLiteDatabase(db), oldVersion, newVersion);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = super.getReadableDatabase();
        new ClearSQLiteDatabase(db).enableForeignKeySupport();
        return db;

    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        new ClearSQLiteDatabase(db).enableForeignKeySupport();
        return db;
    }

}
