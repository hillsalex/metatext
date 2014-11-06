package com.hillsalex.metatext.database;

import android.database.AbstractCursor;
import android.database.Cursor;
import android.support.v4.util.Pair;

/**
 * Created by alex on 11/6/2014.
 */
public class SortMultipleCursor {

    private final Cursor[] mCursors;
    private final int[] mColumns;
    private final String columnType;
    private final boolean mAscending;

    private static final String INT_TYPE = "INT_TYPE";
    private static final String LONG_TYPE = "LONG_TYPE";
    private static final String SHORT_TYPE = "SHORT_TYPE";
    private static final String DOUBLE_TYPE = "DOUBLE_TYPE";
    private static final String FLOAT_TYPE = "FLOAT_TYPE";
    /**
     * This assumes all cursors are not null, and have also had moveToFirst() called on them.
     * @param cursors       A list of cursors to "join" together
     * @param sortColumnIndices   A list of columns indices to sort over, must be the same length as cursors
     * @param cType         The type of the column to grab, must be one of (int, long, short, double, float)
     * @param ascending     Whether the cursors should be sorted in ascending order or not
     */
    public SortMultipleCursor(Cursor[] cursors, int[] sortColumnIndices, Class<?> cType, boolean ascending) throws InvalidMultiCursorArgumentsException {
        if (cursors.length != sortColumnIndices.length) throw new InvalidMultiCursorArgumentsException("Number of cursors and columns must match");
        for (int i=0;i<cursors.length;i++)
        {
            Cursor c = cursors[i];
            if (c==null)
                throw new InvalidMultiCursorArgumentsException("Some cursors are null!");
            if (c.getColumnCount() <= sortColumnIndices[i])
                throw new InvalidMultiCursorArgumentsException(
                        "You done messed up, cursor: " +
                        i + " has fewer columns than you're asking for (" +
                        sortColumnIndices[i] + ")"
                );

        }
        mCursors = cursors;
        mColumns = sortColumnIndices;
        mAscending = ascending;
        if (cType==double.class || cType == Double.class){
            columnType = DOUBLE_TYPE;
        }
        else if (cType==int.class || cType == Integer.class){
            columnType = INT_TYPE;
        }
        else if (cType==float.class || cType == Float.class){
            columnType = FLOAT_TYPE;
        }
        else if (cType==short.class || cType == Short.class){
            columnType = SHORT_TYPE;
        }
        else{
            columnType = LONG_TYPE;
        }
    }

    public Pair<Cursor,Integer> next(){
        int toReturnIndex = -1;

        float currentFloat=Float.MIN_VALUE;
        float tempFloat;
        double currentDouble=Double.MIN_VALUE;
        double tempDouble;
        short currentShort=Short.MIN_VALUE;
        short tempShort;
        int currentInt=Integer.MIN_VALUE;
        int tempInt;
        long currentLong=Long.MIN_VALUE;
        long tempLong;
        if (!mAscending)
        {
            currentFloat=Float.MAX_VALUE;
            currentDouble=Double.MAX_VALUE;
            currentShort=Short.MAX_VALUE;
            currentInt=Integer.MAX_VALUE;
            currentLong=Long.MAX_VALUE;
        }
        for (int i=0;i<mCursors.length;i++){
            switch(columnType){
                case LONG_TYPE:
                    tempLong=mCursors[i].getLong(mColumns[i]);
                    if (mAscending && tempLong>currentLong) {
                        currentLong=tempLong;
                        toReturnIndex=i;
                    }
                    if (!mAscending && tempLong<currentLong) {
                        currentLong=tempLong;
                        toReturnIndex=i;
                    }
                    break;
                case SHORT_TYPE:
                    tempShort=mCursors[i].getShort(mColumns[i]);
                    if (mAscending && tempShort>currentShort) {
                        currentShort=tempShort;
                        toReturnIndex=i;
                    }
                    if (!mAscending && tempShort<currentShort) {
                        currentShort=tempShort;
                        toReturnIndex=i;
                    }
                    break;
                case FLOAT_TYPE:
                    tempFloat=mCursors[i].getFloat(mColumns[i]);
                    if (mAscending && tempFloat>currentFloat) {
                        currentFloat=tempFloat;
                        toReturnIndex=i;
                    }
                    if (!mAscending && tempFloat<currentFloat) {
                        currentFloat=tempFloat;
                        toReturnIndex=i;
                    }
                    break;
                case DOUBLE_TYPE:
                    tempDouble=mCursors[i].getDouble(mColumns[i]);
                    if (mAscending && tempDouble>currentDouble) {
                        currentDouble=tempDouble;
                        toReturnIndex=i;
                    }
                    if (!mAscending && tempDouble<currentDouble) {
                        currentDouble=tempDouble;
                        toReturnIndex=i;
                    }
                    break;
                case INT_TYPE:
                    tempInt=mCursors[i].getInt(mColumns[i]);
                    if (mAscending && tempInt>currentInt) {
                        currentInt=tempInt;
                        toReturnIndex=i;
                    }
                    if (!mAscending && tempInt<currentInt) {
                        currentInt=tempInt;
                        toReturnIndex=i;
                    }
                    break;
            }
        }
        return new Pair<Cursor,Integer>(mCursors[toReturnIndex],toReturnIndex);
    }

    public boolean isAfterLast(){
        boolean toReturn = true;
        for (Cursor c : mCursors) if (!c.isAfterLast()) {toReturn=false;break;}
        return toReturn;
    }



    public void closeAll(){
        for (Cursor c : mCursors){
            c.close();
        }
    }







    public class InvalidMultiCursorArgumentsException extends Throwable{
        public InvalidMultiCursorArgumentsException(String s){
            super(s);
        }
    }
}
