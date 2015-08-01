package com.example.winnabuska.jpokeri.CardLock.Evaluation;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.winnabuska.jpokeri.Card;
import com.example.winnabuska.jpokeri.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by WinNabuska on 19.7.2015.
 */
public class CardDataBaseAdapter {
    private SQLiteDatabase db;
    private Context context;
    private MySQLiteHelper dbHelper;

    public static final String DATABASE_NAME = "card_database";
    public static final String TABLE_NAME = "card_table";
    public static final String ID = "_id";
    public static final String CARD_0 = "card_0";
    public static final String CARD_1 = "card_1";
    public static final String CARD_2 = "card_2";
    public static final String CARD_3 = "card_3";
    public static final String CARD_4 = "card_4";
    public static final int EXPECTED_ROWS_SIZE = 1914;

    protected CardDataBaseAdapter(Context context){
        Log.i("info", "in database contructor");
        this.context = context;
        dbHelper = new MySQLiteHelper(context);
    }

    /*Class SQLiteOpenHelper*/

    protected class MySQLiteHelper extends SQLiteOpenHelper {

        private static final String TABLE_CONSTUCTOR =
                "create table if not exists "+TABLE_NAME+
                        " ("+ID+" integer primary key autoincrement, " +
                        CARD_0+" INTEGER null, "+
                        CARD_1+" INTEGER null, "+
                        CARD_2+" INTEGER null, "+
                        CARD_3+" INTEGER null, "+
                        CARD_4+" INTEGER null);";

        public MySQLiteHelper(Context context){
            super(context, DATABASE_NAME, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_CONSTUCTOR);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + TABLE_NAME);
            onCreate(db);
        }
    }

    /*End of SQLiteOpenHelper*/

    protected void open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
        }catch (SQLiteException e){
            db = dbHelper.getReadableDatabase();
            Log.i("info", "COULD NOT OPEN WRITEBLEDATABASE, READABLE DB -->");
        }
        db.execSQL(MySQLiteHelper.TABLE_CONSTUCTOR);
    }

    protected void insertDatabaseLinesFromRawFile(){
        Log.i("info", "in inserting");
        dbHelper.onUpgrade(db, 0, 0);
        InputStream inSteam = context.getResources().openRawResource(R.raw.all_exceptional_cardpatterns);
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(inSteam));
        String line;
        try {
            while ((line = bufReader.readLine()) != null) {
                String[]data = line.split(";");
                insertEntry(data);
            }
            inSteam.close();
            bufReader.close();
        } catch (IOException e) {e.printStackTrace(); Log.i("info", "fail in inserting");}
    }

    protected long insertEntry(String [] data){
        ContentValues newRow = new ContentValues();
        newRow.put(CARD_0, data[0]);
        newRow.put(CARD_1, data[1]);
        newRow.put(CARD_2, data[2]);
        newRow.put(CARD_3, data[3]);
        newRow.put(CARD_4, data[4]);
        return db.insert(TABLE_NAME, null, newRow);
    }

    protected void close(){
        db.close();
    }

    protected boolean matchesExceptionRow(Card[] hand){
        int[]sortedIDs = cardsToSortedIDs(hand);
        int count= db.query(TABLE_NAME, null,
                CARD_0 + "=" + sortedIDs[0] + " AND " +
                CARD_1 + "=" + sortedIDs[1] + " AND " +
                CARD_2 + "=" + sortedIDs[2] + " AND " +
                CARD_3 + "=" + sortedIDs[3] + " AND " +
                CARD_4 + "=" + sortedIDs[4],
                null, null, null, null).getCount();
        if(count>0) {
            Log.i("info", "MATCH = " + count);
            return true;
        }
        else return false;
    }

    private int [] cardsToSortedIDs(Card[]hand){
        int [] ids = new int[5];
        for(int i = 0; i<hand.length; i++)
            ids[i] = hand[i].getID();
        boolean sorted = false;
        while(!sorted){
            sorted = true;
            for(int i = 0; i<4; i++){
                if(ids[i]>ids[i+1]){
                    sorted = false;
                    int placeHolder = ids[i];
                    ids[i] = ids[i+1];
                    ids[i+1] = placeHolder;
                }
            }
        }
        return ids;
    }

    protected int countRows(){
        return db.query(TABLE_NAME, null, null, null, null, null, null).getCount();
    }
}