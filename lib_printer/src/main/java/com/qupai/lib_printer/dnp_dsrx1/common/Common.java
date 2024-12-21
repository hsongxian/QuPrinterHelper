package com.qupai.lib_printer.dnp_dsrx1.common;



import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.qupai.lib_printer.QuPrinter;
import com.qupai.lib_printer.dnp_dsrx1.PrintManager;
import com.qupai.lib_printer.dnp_dsrx1.PrintManager.EPrinter;
import com.qupai.lib_printer.dnp_dsrx1.PrinterDnp;
import com.qupai.lib_printer.dnp_dsrx1.log.LogCreator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import jp.co.dnp.photocolorcnvlib.DNPPhotoColorCnv;
import jp.co.dnp.photoprintlib.DNPPhotoPrint;


/**
 * 汎用処理を定義するクラス
 */
public class Common {
    /**
     * 単体テスト用：評価モードを定義
     */
    public final static int TEST_MODE = 4;

    /**
     * ログに出力される日付フォーマットを定義
     */
    public final static String DUMPLOG_DATEFORMAT = "yyyy/MM/dd HH:mm:ss.SSS";

    /**
     * ログファイル名に設定する日付のフォーマットを定義
     */
    public final static String FILENAME_DATEFORMAT = "yyyyMMdd_HHmmss";

    /**
     * ログファイルの MIMEタイプ を定義
     */
    public final static String MIME_TYPE = "text/plain";

    /**
     * ログファイルの拡張子を定義
     */
    public final static String LOG_EXT = ".txt";

    /**
     * ログファイル格納先パスを定義
     */
    public final static String LOG_PATH = "/DNPPhotoPrintSample/Log/";

    /**
     * UI に表示する画像ファイルの拡張子を定義
     */
    public final static String IMAGE_EXT = ".bmp";

    /**
     * 画像ファイル参照先パスを定義
     */
    public final static String IMAGE_PATH = "/DNPPhotoPrintSample/Image";

    /**
     * UI に表示するINIファイルの拡張子を定義
     */
    public final static String INI_EXT = ".ini";

    /**
     * INIファイル参照先パスを定義
     */
    public final static String INI_PATH = "/DNPPhotoPrintSample/Ini";

    /**
     * 各 Spinner 設定する、任意指定項目の表示名を定義
     */
    public final static String ANY_SELECT = "N:任意指定";

    /**
     * 画像データバッファのサイズを定義
     */
    public final static int IMAGE_BUF_SIZE = 1024;

    /**
     * 画像データバッファのサイズを定義
     */
    public final static int BMP_HEADER_SIZE = 0x36;

    /**
     * SDK に渡す、受信バッファのサイズを定義
     */
    public final static int RECEIVE_BUF_SIZE = 256;

    /**
     * SDK関数 SetRewindMode に渡す、リワインドモード [ON] を定義
     */
    public final static int REWIND_MODE_ON = 1;

    /**
     * SDK関数 SetRewindMode に渡す、リワインドモード [OFF] を定義
     */
    public final static int REWIND_MODE_OFF = 0;

    /**
     * 出力ログのタグ名を定義
     */
    private final static String LOG_TAG = "TestApp";

    /**
     * Logcat にログをダンプする
     *
     * @param msg 出力メッセージ
     */
    public static void dumpLog(String msg) {
        Log.d(LOG_TAG, msg);
    }

    /**
     * Logcat にログをダンプする
     *
     * @param msg       出力メッセージ
     * @param exception Exceptionのインスタンス
     */
    public static void dumpLog(String msg, Exception exception) {
        LogUtils.e(LOG_TAG, msg, exception);
    }

    /**
     * 受け取った文字列でトーストを出力する
     *
     * @param ctx コンテキスト
     * @param msg 表示したい文字列
     */
    public static void showToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * UIスレッドでトースト表示を行う
     *
     * @param ctx コンテキスト
     * @param msg 表示したい文字列
     */
    public static void showToastUiThread(final Context ctx, final String msg) {

        if (ctx == null) {
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                showToast(ctx, msg);
            }
        });
    }

    /**
     * チェックされたプリンターの position(ポート番号) をリストにして返す
     *
     * @param listViewPinter 発見したプリンター一覧
     * @return 選択されたプリンターのポート番号リスト
     */
    public static List<Integer> getCheckedPrinterList(ListView listViewPinter) {
        List<Integer> portNumList = new ArrayList<>();

        for (int i = 0; i < listViewPinter.getCount(); i++) {
            if (listViewPinter.isItemChecked(i)) {
                portNumList.add(i);
            }
        }

        return portNumList;
    }

    /**
     * 画像データを読み込む
     *
     * @param filePath 画像ファイルのパス
     * @return 画像データのバイト配列(ヘッダ部を除く)
     */
    public static byte[] readImageData(String filePath, byte[] header) {

        File file = new File(filePath);
        byte[] buffer = new byte[Common.IMAGE_BUF_SIZE];
        //byte[] buffer = new byte[1024*1024];
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bs_bmp = new ByteArrayOutputStream();

            // ビットマップのヘッダ分をスキップ
            if (header != null) {
                fis.read(header);
            } else {
                fis.skip(BMP_HEADER_SIZE);
            }

            while (true) {
                int len = fis.read(buffer);

                if (len < 0) {
                    break;
                }
                bs_bmp.write(buffer, 0, len);
            }
            byte[] ret = bs_bmp.toByteArray();
            bs_bmp.close();
            fis.close();
            return ret;
        } catch (Exception e) {
            dumpLog("[readImageData] error", e);
        }

        return null;
    }

    /**
     * 画像データを読み込み、色変換後のデータを返却する
     *
     * @param filePath 画像ファイルのパス
     * @param size     画像のサイズ
     * @param context  コンテキスト
     * @return 色変換後の画像データ
     */
    public static byte[] readAndConvertImageData(String filePath, Context context, Rect size) {
        // 画像ファイルを読み込み
        byte[] header = new byte[BMP_HEADER_SIZE];
        byte[] imageData = readImageData(filePath, header);

        try {
            // ルックアップテーブルを取得
            LogUtils.i("DNP调色名称：" + QuPrinter.INSTANCE.getDnpMixColors());
            InputStream is_lut = context.getResources().openRawResource(QuPrinter.INSTANCE.getDnpMixColorsRaw());
            ByteArrayOutputStream bs_lut = new ByteArrayOutputStream();
            byte[] buffer = new byte[IMAGE_BUF_SIZE];
            while (true) {
                int len = is_lut.read(buffer);
                if (len < 0) {
                    break;
                }
                bs_lut.write(buffer, 0, len);
            }
            byte[] lut = bs_lut.toByteArray();

            // 色変換
            DNPPhotoColorCnv.ColorConvert(size.width(), size.height(), imageData, lut, true);
            //dumpBitmap(header, imageData, context);
            return imageData;
        } catch (Exception e) {
            Common.dumpLog("[readAndConvertImageData] error", e);
        }
        return null;
    }

    /**
     * ビットマップファイルをダウンロードフォルダにダンプする(デバッグ用)
     *
     * @param bmpHeader ビットマップヘッダ
     * @param data      ビットマップイメージ部
     * @param context   コンテキスト
     */
    public static void dumpBitmap(byte[] bmpHeader, byte[] data, Context context) {

        if (bmpHeader != null && data != null) {
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/test.bmp";
            File convertFile = new File(filePath);
            try {
                // ファイル出力
                FileOutputStream fos = new FileOutputStream(convertFile);
                fos.write(bmpHeader);
                fos.write(data);
                fos.close();
                // ファイルスキャン
                scanFile(filePath, context, "ima/bmp");
            } catch (Exception e) {
                dumpLog("Bitmap 出力失敗", e);
            }
            dumpLog("Bitmap 出力完了 : " + filePath);
        }
    }

    /**
     * 画像ファイルのディレクトリ（絶対パス）を取得する
     *
     * @return 画像ファイルのディレクトリ
     */
    public static String getImageDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + Common.IMAGE_PATH;
    }

    /**
     * Bitmap のサイズ(幅／高さを取得する)
     *
     * @param filePath 画像ファイルのパス
     * @return 画像サイズ(幅 ／ 高さ)
     */
    public static Rect getBitmapSize(String filePath) {

        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;  // 画像は読み込まずにサイズのみ取得する
            BitmapFactory.decodeStream(fis, null, options);
            fis.close();
            return new Rect(0, 0, options.outWidth, options.outHeight);
        } catch (Exception e) {
            dumpLog("[getBitmapSize] error", e);
        }

        return new Rect();
    }

    /**
     * スキャンファイルを実行しWindowsのエクスプローラー等にすぐにファイルが表示されるようにする
     *
     * @param filePath ファイルパス
     */
    public static void scanFile(String filePath, Context ctx, String mimeType) {
        String[] paths = {filePath};
        String[] mimeTypes = {mimeType};
        MediaScannerConnection.scanFile(ctx,
                paths,
                mimeTypes,
                null);
    }

    /**
     * MainFragment で検索したプリンターを取得する
     *
     * @return 検索で見つかったプリンターのHashMap
     */
    public static LinkedHashMap<Integer, PrintManager.EPrinter> getPrinterList() {

        int[][] printers = new int[10][2];
        int result = 0;
        String searchFuncName = "GetPrinterPortNum";

        LogCreator lc = new LogCreator(Utils.getApp(), PrintManager.PrintMode.SEARCH_PRINTER);
        lc.openFile();
        lc.write(searchFuncName, true);
        if (Common.TEST_MODE == 4) {
            // 関数で検索
            result = getDNPInstance().GetPrinterPortNum(printers);
        } else {
            result = 5;
            // [デバッグ用データ-S]
            printers[0][0] = PrintManager.EPrinter.CX_DEF.id;
            printers[0][1] = 1;
            printers[1][0] = PrintManager.EPrinter.CXW_DEF.id;
            printers[1][1] = 2;
            printers[2][0] = EPrinter.CY_DEF.id;
            printers[2][1] = 3;
            printers[3][0] = EPrinter.DS620_DEF.id;
            printers[3][1] = 4;
            printers[4][0] = EPrinter.DS820_DEF.id;
            printers[4][1] = 5;
            printers[5][0] = EPrinter.QW410_DEF.id;
            printers[5][1] = 6;
            // [デバッグ用データ-E]
        }
        String dumpStr = "";
        for (int i = 0; i < result; i++) {
            int machineId = printers[i][0];
            int individualId = printers[i][1];
            dumpStr += "," + individualId + "-" + machineId;
        }
        lc.write(searchFuncName + ":" + result + dumpStr, true);
        lc.closeFile();

        LogUtils.i(dumpStr);
        LogUtils.i(result);
        EPrinter[] ePrinters = EPrinter.values();

        // 固体識別子をKey、プリンター情報をValueとするHashMapを生成する
        LinkedHashMap<Integer, EPrinter> printerHashMap = new LinkedHashMap<>();
        // 検索結果で見つかったプリンターを走査
        for (int i = 0; i < result; i++) {
            int id = printers[i][0];
            // プリンターのenumを走査
            for (EPrinter ePrinter : ePrinters) {
                if (id == ePrinter.id) {
                    int solidId = printers[i][1];
                    printerHashMap.put(solidId, ePrinter);
                }
            }
        }

        return printerHashMap;
    }

    /**
     * ライブラリのインスタンスを取得する
     *
     * @return DNPPhotoPrint のインスタンス
     */
    public static DNPPhotoPrint getDNPInstance() {
        return PrinterDnp.INSTANCE.getDnpPhotoPrint();
    }
}
