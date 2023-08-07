package com.app.freya.bean;

import org.litepal.crud.LitePalSupport;

/**
 * 笔记本的bean
 * Created by Admin
 * Date 2023/1/10
 * @author Admin
 */
public class NoteBookBean extends LitePalSupport {

    /**日期 yyyy-MM-dd格式**/
    private String noteDate;

    /**时间戳 long 毫秒**/
    private long noteTimeLong;

    /**保存时间 yyyy-MM-dd HH:mm:ss格式，用于查询**/
    private String saveTime;

    /**标题**/
    private String noteTitle;


    /**内容**/
    private String noteContent;


    public String getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(String saveTime) {
        this.saveTime = saveTime;
    }

    public String getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(String noteDate) {
        this.noteDate = noteDate;
    }

    public long getNoteTimeLong() {
        return noteTimeLong;
    }

    public void setNoteTimeLong(long noteTimeLong) {
        this.noteTimeLong = noteTimeLong;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }
}
