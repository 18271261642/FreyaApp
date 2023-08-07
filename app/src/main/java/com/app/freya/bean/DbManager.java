package com.app.freya.bean;

import com.google.gson.Gson;

import org.litepal.LitePal;

import java.util.List;

import timber.log.Timber;

/**
 * Created by Admin
 * Date 2023/1/10
 * @author Admin
 */
public class DbManager {


    public static   volatile DbManager dbManager = null;

    private final Gson gson = new Gson();

    private static final String commWhere = "userId = ? and deviceMac = ? and saveDay = ?";

    public synchronized static DbManager getInstance(){
        synchronized (DbManager.class){
            if(dbManager == null){
                dbManager = new DbManager();
            }
        }
        return dbManager;
    }

    private DbManager() {
    }


    /**
     * 保存或者修改记事本信息
     * @param noteBookBean 记事本对象
     */
    public  boolean saveOrUpdateData(NoteBookBean noteBookBean){
        boolean isSaved = queryNoteBookByTime(noteBookBean.getSaveTime()) == null;
        boolean saveStatus;
        if(isSaved){
            saveStatus = noteBookBean.save();
        }else{
            saveStatus = noteBookBean.saveOrUpdate("saveTime = ?",noteBookBean.getSaveTime());
        }
        return saveStatus;
    }


    /**
     * 查询数据
     * @param timeStr yyyy-MM-dd HH:mm:ss格式
     * @return 是否有数据
     */
    public  NoteBookBean queryNoteBookByTime(String timeStr){
        List<NoteBookBean> list = LitePal.where("saveTime = ?",timeStr).find(NoteBookBean.class);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询所有的数据
     * @return 所有的数据
     */
    public  List<NoteBookBean> queryAllNoteBook(){
        List<NoteBookBean> list = LitePal.findAll(NoteBookBean.class);
        return list == null || list.isEmpty() ? null : list;
    }


    /**
     * 删除对应的笔记
     * @param timeStr
     */
    public void deleteNotebook(String timeStr){
        int code = LitePal.deleteAll(NoteBookBean.class,"saveTime = ?",timeStr);
        Timber.e("--------删除="+code);
    }
}
