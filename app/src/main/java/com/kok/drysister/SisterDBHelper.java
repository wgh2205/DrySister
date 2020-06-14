package com.kok.drysister;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

/**
 * DB增删改查操作及辅助函数
 */
public class SisterDBHelper {
    private static final String TAG = "SisterDBHelper";

    private static SisterDBHelper dbHelper;
    private SisterOpenHelp sisterOpenHelp;
    private SQLiteDatabase db;
    private Context mcontext;

    private SisterDBHelper(Context context) {
        this.mcontext = context.getApplicationContext();
        sisterOpenHelp = new SisterOpenHelp(mcontext);
    }

    /**
     * 单例
     * @return
     */
    public static SisterDBHelper getInstance(Context context) {
        if (dbHelper == null) {
            synchronized (SisterDBHelper.class) {
                if (dbHelper == null) {
                    dbHelper = new SisterDBHelper(context.getApplicationContext());
                }
            }
        }
        return dbHelper;
    }

    /**
     * 插入一个妹子数据
     * @param sister
     */
    public void insertSister(Sister sister) {
        db = getWriteableDB();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TableDefine.COLUMN_FULI_ID,sister.get_id());
        contentValues.put(TableDefine.COLUMN_FULI_CREATEAT,sister.getCreateAt());
        contentValues.put(TableDefine.COLUMN_FULI_DESC, sister.getDesc());
        contentValues.put(TableDefine.COLUMN_FULI_PUBLISHEDAT,sister.getPublishedAt());
        contentValues.put(TableDefine.COLUMN_FULI_SOURCE,sister.getSource());
        contentValues.put(TableDefine.COLUMN_FULI_TYPE,sister.getType());
        contentValues.put(TableDefine.COLUMN_FULI_URL,sister.getUrl());
        contentValues.put(TableDefine.COLUMN_FULI_USED,sister.getUsed());
        contentValues.put(TableDefine.COLUMN_FULI_WHO,sister.getWho());
        db.insert(TableDefine.TABLE_FULI, null, contentValues);
        closeIO(null);
    }

    /**
     * 使用事务插入多个妹子数据
     * @param sisters
     */
    public void insertSisters(ArrayList<Sister> sisters) {
        db = getWriteableDB();
        db.beginTransaction();
        try {
            for (Sister sister : sisters) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TableDefine.COLUMN_FULI_ID,sister.get_id());
                contentValues.put(TableDefine.COLUMN_FULI_CREATEAT,sister.getCreateAt());
                contentValues.put(TableDefine.COLUMN_FULI_DESC, sister.getDesc());
                contentValues.put(TableDefine.COLUMN_FULI_PUBLISHEDAT,sister.getPublishedAt());
                contentValues.put(TableDefine.COLUMN_FULI_SOURCE,sister.getSource());
                contentValues.put(TableDefine.COLUMN_FULI_TYPE,sister.getType());
                contentValues.put(TableDefine.COLUMN_FULI_URL,sister.getUrl());
                contentValues.put(TableDefine.COLUMN_FULI_USED,sister.getUsed());
                contentValues.put(TableDefine.COLUMN_FULI_WHO,sister.getWho());
                db.insert(TableDefine.TABLE_FULI, null, contentValues);
            }
            db.setTransactionSuccessful();
        }finally {
            if (db != null && db.isOpen()) {
                db.endTransaction();
                closeIO(null);
            }
        }
    }

    /**
     * 根据Id删除妹子数据
     * @param _id
     */
    public void deleteSister(String _id) {
        db=getWriteableDB();
        db.delete(TableDefine.TABLE_FULI, "_id=?", new String[]{_id});
        closeIO(null);
    }

    /**
     * 删除所有妹子
     */
    public void deleteAllSisters() {
        db = getWriteableDB();
        db.delete(TableDefine.TABLE_FULI, null, null);
        closeIO(null);
    }

    /**
     * 根据_id更新妹子数据
     * @param _id
     * @param sister
     */
    public void updateSister(String _id, Sister sister) {
        db = getWriteableDB();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TableDefine.COLUMN_FULI_ID,sister.get_id());
        contentValues.put(TableDefine.COLUMN_FULI_CREATEAT,sister.getCreateAt());
        contentValues.put(TableDefine.COLUMN_FULI_DESC, sister.getDesc());
        contentValues.put(TableDefine.COLUMN_FULI_PUBLISHEDAT,sister.getPublishedAt());
        contentValues.put(TableDefine.COLUMN_FULI_SOURCE,sister.getSource());
        contentValues.put(TableDefine.COLUMN_FULI_TYPE,sister.getType());
        contentValues.put(TableDefine.COLUMN_FULI_URL,sister.getUrl());
        contentValues.put(TableDefine.COLUMN_FULI_USED,sister.getUsed());
        contentValues.put(TableDefine.COLUMN_FULI_WHO,sister.getWho());
        db.insert(TableDefine.TABLE_FULI, null, contentValues);
        closeIO(null);
    }

    /**
     * 查询当前表中有多少妹子
     * @return
     */
    public int getSisterCount() {
        db = getReadableDB();
        Cursor cursor = db.rawQuery("SELECT COUNT (*) FROM" + TableDefine.TABLE_FULI, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        Log.d(TAG, "count:"+count);
        closeIO(cursor);
        return count;
    }

    /**
     * 分页查询妹子，参数为当前页和每一页的数量，页数从0开始
     * @param curPage
     * @param limit
     * @return
     */
    public List<Sister> getSistersLimit(int curPage,int limit) {
        db = getReadableDB();
        List<Sister> sisters = new ArrayList<>();
        String startPos = String.valueOf(curPage * limit);//数据开始的位置
        if (db != null) {
            Cursor cursor = db.query(TableDefine.TABLE_FULI, new String[]{
                    TableDefine.COLUMN_FULI_ID,TableDefine.COLUMN_FULI_CREATEAT,
                    TableDefine.COLUMN_FULI_DESC,TableDefine.COLUMN_FULI_PUBLISHEDAT,
                    TableDefine.COLUMN_FULI_SOURCE,TableDefine.COLUMN_FULI_TYPE,
                    TableDefine.COLUMN_FULI_URL,TableDefine.COLUMN_FULI_USED,
                    TableDefine.COLUMN_FULI_WHO},null,null,null,
                    null,TableDefine.COLUMN_ID,startPos+","+limit);
            while (cursor.moveToNext()) {
                Sister sister=new Sister();
                sister.set_id(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_ID)));
                sister.setCreateAt(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_CREATEAT)));
                sister.setDesc(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_DESC)));
                sister.setPublishedAt(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_PUBLISHEDAT)));
                sister.setSource(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_SOURCE)));
                sister.setType(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_TYPE)));
                sister.setUrl(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_URL)));
                sister.setUsed(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_USED)) =="true"?true:false);
                sister.setWho(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_WHO)));
                sisters.add(sister);
            }
            closeIO(cursor);
        }
        return sisters;
    }

    /**
     * 查询所有妹子到数据
     * @return
     */
    public List<Sister> getAllSisters() {
        db = getReadableDB();
        List<Sister> sisters = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TableDefine.TABLE_FULI, null);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            Sister sister=new Sister();
            sister.set_id(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_ID)));
            sister.setCreateAt(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_CREATEAT)));
            sister.setDesc(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_DESC)));
            sister.setPublishedAt(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_PUBLISHEDAT)));
            sister.setSource(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_SOURCE)));
            sister.setType(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_TYPE)));
            sister.setUrl(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_URL)));
            sister.setUsed(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_USED)) =="true"?true:false);
            sister.setWho(cursor.getString(cursor.getColumnIndex(TableDefine.COLUMN_FULI_WHO)));
            sisters.add(sister);
        }
        closeIO(cursor);
        return sisters;
    }

    /**
     * 获取可读数据库
     * @return
     */
    private SQLiteDatabase getReadableDB() {
        return sisterOpenHelp.getReadableDatabase();
    }

    /**
     * 关闭cursor和数据库链接
     * @param cursor
     */
    private void closeIO(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
        if (db != null) {
            db.close();
        }
    }

    /**
     * 获取可写数据库
     * @return
     */
    private SQLiteDatabase getWriteableDB() {
        return sisterOpenHelp.getWritableDatabase();
    }

}
