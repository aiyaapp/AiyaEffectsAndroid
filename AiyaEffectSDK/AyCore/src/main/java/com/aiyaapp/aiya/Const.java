package com.aiyaapp.aiya;

/**
 * 常量类，AiyaEffects SDK相关常量
 * @author wuwang
 */

public class Const {

    /**
     * 初始化消息
     */
    public static int MSG_TYPE_INIT=0x0001;

    /**
     * 认证消息
     */
    public static int MSG_TYPE_AUTH=0x0002;

    /**
     * SDK内部线程释放
     */
    public static int MSG_STAT_LOOP_EXIT=0x0101;

    /**
     * 功能被禁止
     */
    public static int MSG_ERR_FUNC_FORBIDDEN=0xFE000010;


    /**
     * 消息分类为信息
     */
    public static int MSG_TYPE_INFO=4;

    /**
     * 消息分类为错误
     */
    public static int MSG_TYPE_ERROR=6;

    /**
     * 以下为native返回的Error
     */
   public static final int MSG_ERROR_INVALID_ENUM=-0x0500;
   public static final int MSG_ERROR_INVALID_VALUE=-0x0501;
   public static final int MSG_ERROR_INVALID_OPERATION=-0x0502;
   public static final int MSG_ERROR_OUT_OF_MEMORY=-0x0505;
   public static final int MSG_ERROR_UNSUPPORT_FORMAT=-0x0510;
   public static final int MSG_ERROR_READFILE_FAIL=-0x0511;
   public static final int MSG_ERROR_READSKLT_FAIL=-0x0512;
   public static final int MSG_ERROR_NULL_PTR=-0x0513;
   public static final int MSG_ERROR_FBO_INCOMPLETE=-0x0514;
   public static final int MSG_ERROR_RB_FAIL=-0x0515;
   public static final int MSG_ERROR_DB_FAIL=-0x0516;
   public static final int MSG_ERROR_DRAWBG_FAIL=-0x0517;
   public static final int MSG_ERROR_DRAW3D_FAIL=-0x0518;
   public static final int MSG_ERROR_DRAWSK_FAIL=-0x0519;
   public static final int MSG_ERROR_NOT_LOADED=-0x051A;
   public static final int MSG_ERROR_INVALID_PATH=-0x051B;
   public static final int MSG_ERROR_PARSE_JSON_FAIL=-0x051C;
   public static final int MSG_ERROR_UNKNOW_TYPE=-0x051D;
   public static final int MSG_ERROR_NOT_INITED=-0x051E;
   public static final int MSG_ERROR_NO_TRACK=-0x051F;
   public static final int MSG_ERROR_BEAUTY_FAIL=-0x0520;
   public static final int MSG_ERROR_INVALID_SHADER=-0x0521;
   public static final int MSG_ERROR_DRAWST_FAIL=-0x0522;
   public static final int MSG_ERROR_READMTL_FAIL=-0x0523;
   public static final int MSG_ERROR_MTLTEX_NOT_MATCH=-0x0524;
   public static final int MSG_ERROR_ARCHIVE_NOT_EXIST=-0x0600;
   public static final int MSG_ERROR_ARCHIVE_INVSIZE=-0x0601;
   public static final int MSG_ERROR_ARCHIVE_BAD=-0x0602;
   public static final int MSG_ERROR_RES_NOT_EXIST=-0x0603;
   public static final int MSG_ERROR_NO_AUTH=-0x0604;

}
