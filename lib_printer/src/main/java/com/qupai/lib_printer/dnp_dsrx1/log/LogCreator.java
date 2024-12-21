package com.qupai.lib_printer.dnp_dsrx1.log;

import android.content.Context;
import android.os.Environment;

import com.qupai.lib_printer.dnp_dsrx1.PrintManager;
import com.qupai.lib_printer.dnp_dsrx1.PrintManager.EPrinter;
import com.qupai.lib_printer.dnp_dsrx1.PrintManager.PrintMode;
import com.qupai.lib_printer.dnp_dsrx1.common.Common;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * ログ生成クラス
 */
public class LogCreator {
    /**
     * コンテキスト
     */
    private Context mCtx;
    /**
     * 印刷モード
     */
    private PrintManager.PrintMode mPrintMode;
    /**
     * ファイル出力ストリーム
     */
    private FileOutputStream mFos;
    /**
     * ファイルパス
     */
    private String mFilePath;
    /**
     * 機種情報
     */
    private EPrinter mPrinter;
    /**
     * プリンターの個体識別子
     */
    private int mSolidId;
    /**
     * INIファイルの名称
     */
    private String mIniFileName;


    /**
     * 印刷評価用のコンストラクタ
     *
     * @param ctx  コンテキスト
     * @param mode 印刷モード
     */
    public LogCreator(Context ctx, PrintMode mode, EPrinter printer, String iniFileName, int solidId) {
        mCtx = ctx;
        mPrintMode = mode;
        mPrinter = printer;
        mIniFileName = iniFileName;
        mSolidId = solidId;
    }

    /**
     * プリンター検索用のコンストラクタ
     *
     * @param ctx  コンテキスト
     * @param mode 印刷モード
     */
    public LogCreator(Context ctx, PrintMode mode) {
        mCtx = ctx;
        mPrintMode = mode;
    }

    /**
     * ログファイルをオープンする
     * 本メソッド呼び出し以降に、writeメソッドで書き込み可能となる
     */
    public boolean openFile() {

//        mFilePath = getFilePath();
//        File file = new File(mFilePath);
//        Common.dumpLog("FileName : " + mFilePath);
//        try {
//            mFos = new FileOutputStream(file);
//            return true;
//        } catch (Exception e) {
//            Common.dumpLog("[openFile] error", e);
//            return false;
//        }
        return true;
    }

    /**
     * ログファイルに書き込む
     *
     * @param msg メッセージ
     */
    public boolean write(String msg, boolean isDate) {
//        try {
//            String log = null;
//
//            if(isDate){
//                // 日付文字列を生成
//                Calendar calendar = Calendar.getInstance();
//                SimpleDateFormat sdf = new SimpleDateFormat(Common.DUMPLOG_DATEFORMAT);
//                // メッセージ生成
//                log = sdf.format(calendar.getTime()) + " " + msg + "\n";
//            } else {
//                log = msg + "\n";
//            }
//
//            // ファイルに書き込み
//            mFos.write(log.getBytes());
//            return true;
//        } catch (Exception e) {
//            Common.dumpLog("[write] error", e);
//        }
        return false;
    }

    /**
     * ログファイルをクローズする
     * 本メソッド呼び出し以降の書き込みはできない
     */
    public void closeFile() {
//        try {
//            mFos.flush();
//            mFos.close();
//            mFos = null;
//            Common.scanFile(mFilePath, mCtx, Common.MIME_TYPE);
//        } catch (Exception e) {
//            Common.dumpLog("[closeFile] error", e);
//        }
    }

    /**
     * ログファイルの出力パスを生成する
     *
     * @return ログファイルの出力パス（ファイル名を含む）
     */
    private String getFilePath() {
        // 日付文字列を生成
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(Common.FILENAME_DATEFORMAT);
        String dateStr = sdf.format(calendar.getTime());

        // ファイルパスを生成
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + Common.LOG_PATH;
        if (mPrintMode == PrintMode.SEARCH_PRINTER) {
            filePath += dateStr + "_" + mPrintMode.testName + Common.LOG_EXT;
        } else {
            String testName = mPrintMode == PrintMode.CONTROL ? mIniFileName : mPrintMode.testName;
            filePath += dateStr + "_" + testName + "_" + mPrinter.displayName + "_" + mSolidId + Common.LOG_EXT;
        }

        return filePath;
    }

    /**
     * ファイルがクローズされているかどうか返却する
     *
     * @return true:クローズされている、false:オープンされている
     */
    public boolean isFileClose() {
        return mFos == null;
    }
}
