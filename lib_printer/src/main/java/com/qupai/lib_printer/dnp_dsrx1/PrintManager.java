package com.qupai.lib_printer.dnp_dsrx1;

import static java.sql.Types.NULL;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.qupai.lib_printer.dnp_dsrx1.common.Common;
import com.qupai.lib_printer.dnp_dsrx1.ini.IniFileReader;
import com.qupai.lib_printer.dnp_dsrx1.log.LogCreator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import jp.co.dnp.photoprintlib.DNPPhotoPrint;

/**
 * 印刷処理を制御するクラス
 */
public class PrintManager {

    /** ログの書き込み先 */
    private LogCreator mLc;

    /**
     * コンストラクタ
     * @param lc ログの書き込み先
     */
    public PrintManager(LogCreator lc) {
        mLc = lc;
    }

    /**
     * 基本印刷を実行する
     * @param printData 印刷データ
     * @param ctx       コンテキスト
     */
    public void doBasicPrint(PrintData printData, int portNum, Context ctx) {
        DNPPhotoPrint dnpPhotoPrint =  Common.getDNPInstance();

        /* dnpPhotoPrint 関数呼び出し結果を格納 */
        boolean result;

        // イメージ展開に必要な設定項目の設定
        firstPrintSetting(dnpPhotoPrint, printData, portNum);

        if (printData.chkBoxPageLayout) {
            /* ページレイアウト関数使用時の処理 */

            // ページレイアウト開始
            writeLog("StartPageLayout");
            result = dnpPhotoPrint.StartPageLayout(portNum);
            writeLog("StartPageLayout:" + result);

            // バッファチェック
            if (!checkFreeBuffer(dnpPhotoPrint, portNum, ctx)) {
                LogUtils.e("设备端口"+portNum+"被占用");
                return;
            }

            // 印刷動作前に必要な設定項目の設定
            secondPrintSetting(dnpPhotoPrint, printData, portNum);

            // 画像1 のデータを送る
            switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 1);

            switch (printData.mediaSize) {
                case DNPPhotoPrint.CSP_PCx2:
                case DNPPhotoPrint.CSP_Lx2:
                case DNPPhotoPrint.CSP_6x4P5x2:
                case DNPPhotoPrint.CSP_8x4x2:
                case DNPPhotoPrint.CSP_8x5x2:
                case DNPPhotoPrint.CSP_8x6x2:
                case DNPPhotoPrint.CSP_A5x2:
                case DNPPhotoPrint.CSP_A4x5x2:
                    /* 用紙サイズに2画面割付No.が選択されていた時の処理 */
                    if (printData.imageWidth2 != 0) {
                        /* 2枚目の画像が選択されていた時の処理 */
                        // 画像2 のデータを送る
                        switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 2);

                    }
                    break;

                case DNPPhotoPrint.CSP_8x4x3:
                    /* 用紙サイズに3画面割付No.が選択されていた時の処理 */

                    if (printData.imageWidth2 != 0) {
                        /* 2枚目の画像が選択されていた時の処理 */
                        // 画像2 のデータを送る
                        switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 2);

                        if (printData.imageWidth3 != 0) {
                            /* 3枚目の画像が選択されていた時の処理 */
                            // 画像3 のデータを送る
                            switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 3);
                        }
                    }
                    break;
                default:
                    /* 用紙サイズに2画面割付, 3画面割付以外が選択されていた時の処理 */
                    break;

            }
            // ページレイアウト終了
            writeLog("EndPageLayout");
            result = dnpPhotoPrint.EndPageLayout(portNum);
            writeLog("EndPageLayout:" + result);


        } else {
            /* ページレイアウト関数使用しない */

            // バッファチェック
            if (!checkFreeBuffer(dnpPhotoPrint, portNum, ctx)) {
                return;
            }

            // 印刷動作前に必要な設定項目の設定
            secondPrintSetting(dnpPhotoPrint, printData, portNum);

            // 画像1 のデータを送る
            switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 1);

            switch (printData.mediaSize) {
                case DNPPhotoPrint.CSP_PCx2:
                case DNPPhotoPrint.CSP_Lx2:
                case DNPPhotoPrint.CSP_6x4P5x2:
                case DNPPhotoPrint.CSP_8x4x2:
                case DNPPhotoPrint.CSP_8x5x2:
                case DNPPhotoPrint.CSP_8x6x2:
                case DNPPhotoPrint.CSP_A5x2:
                case DNPPhotoPrint.CSP_A4x5x2:
                    /* 用紙サイズに2画面割付No.が選択されていた時の処理 */

                    if (printData.imageWidth2 != 0) {
                        /* 2枚目の画像が選択されていた時の処理 */
                        // 画像2 のデータを送る
                        switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 2);

                    } else {
                        /* 2枚目に "無し" が選択されていた時の処理 */
                        // PrintImageData で印刷を開始
                        writeLog("PrintImageData");
                        result = dnpPhotoPrint.PrintImageData(portNum);
                        writeLog("PrintImageData:" + result);

                    }
                    break;

                case DNPPhotoPrint.CSP_8x4x3:
                    /* 用紙サイズに3画面割付No.が選択されていた時の処理 */

                    if (printData.imageWidth2 != 0) {
                        /* 2枚目の画像が選択されていた時の処理 */
                        // 画像2 のデータを送る
                        switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 2);

                        if (printData.imageWidth3 != 0) {
                            /* 3枚目の画像が選択されていた時の処理 */
                            // 画像3 のデータを送る
                            switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 3);

                        } else {
                            /* 3枚目に "無し" が選択されていた時 */
                            // PrintImageData で印刷を開始
                            writeLog("PrintImageData");
                            result = dnpPhotoPrint.PrintImageData(portNum);
                            writeLog("PrintImageData:" + result);

                        }
                    } else {
                        /* 2枚目に "無し" が選択されていた時 */
                        // PrintImageData で印刷を開始
                        writeLog("PrintImageData");
                        result = dnpPhotoPrint.PrintImageData(portNum);
                        writeLog("PrintImageData:" + result);
                    }
                    break;

                default:
                    /* 用紙サイズに2画面割付, 3画面割付以外が選択されていた時 */
                    break;
            }
        }
        // ステータスと印刷枚数をログファイルに記載
        dumpStatusAndPQTYInLogFile(dnpPhotoPrint, portNum);

    }


    /**
     * 巻き戻し印刷を実行する
     * @param printData 印刷データ
     * @param ctx       コンテキスト
     */
    public void doRewindPrint(PrintData printData, int portNum, Context ctx) {
        /* リボンパネル使用状態格納変数 */
        int mediaCounterH = 0;

        /* dnpPhotoPrint 関数呼び出し結果を格納 */
        boolean result;

        DNPPhotoPrint dnpPhotoPrint = Common.getDNPInstance();

        switch (printData.mediaSize) {
            case DNPPhotoPrint.CSP_PC_REWIND:
            case DNPPhotoPrint.CSP_L_REWIND:
            case DNPPhotoPrint.CSP_6x4P5_REWIND:
            case DNPPhotoPrint.CSP_8x4_REWIND:
            case DNPPhotoPrint.CSP_8x5_REWIND:
            case DNPPhotoPrint.CSP_8x6_REWIND:
            case DNPPhotoPrint.CSP_A5_REWIND:
            case DNPPhotoPrint.CSP_A4x5_REWIND:
                /* 用紙サイズにリワインド用紙(CSP_XX_REWIND) 選択した時の処理 */

                /* 切り替えた2面割付用紙サイズNo.を格納する変数 */
                int tempMediaSize = 0;

                // リボンの使用状況を取得
                writeLog("GetMediaCounterH");
                mediaCounterH = dnpPhotoPrint.GetMediaCounterH(portNum);
                if(Common.TEST_MODE == 2){
                    // 単体テスト用の処理
                    mediaCounterH = 2;
                }
                writeLog("GetMediaCounterH:" + mediaCounterH);

                if (mediaCounterH % 2 == 0) {
                    /* リボンパネル未使用時の処理 */

                    // 解像度を設定
                    writeLog("SetResolution:" + printData.resolution);
                    result = dnpPhotoPrint.SetResolution(portNum, printData.resolution);
                    writeLog("SetResolution:" + result);

                    // メディアサイズを2面割付に切り替える
                    tempMediaSize = tempMediaSizeToTwoSides(printData.mediaSize);
                    // 2面割付用紙を設定
                    writeLog("SetMediaSize:" + tempMediaSize);
                    result = dnpPhotoPrint.SetMediaSize(portNum, tempMediaSize);
                    writeLog("SetMediaSize:" + result);

                    // バッファチェック
                    if (!checkFreeBuffer(dnpPhotoPrint, portNum, ctx)) {
                        return;
                    }

                    // 印刷動作前に必要な設定項目の設定
                    secondPrintSetting(dnpPhotoPrint, printData, portNum);

                    // 画像1 のデータを送る
                    switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 1);

                    if (printData.imageWidth2 != 0) {
                        /* 2枚目の画像が選択されていた時の処理 */
                        // 画像2 のデータを送る
                        switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 2);

                        if (printData.imageWidth3 != 0) {
                            /* 3枚目の画像が選択されていた時の処理 */
                            // リワインド用紙をセット
                            writeLog("SetMediaSize:" + printData.mediaSize);
                            result = dnpPhotoPrint.SetMediaSize(portNum, printData.mediaSize);
                            writeLog("SetMediaSize:" + result);

                            // バッファチェック
                            if (!checkFreeBuffer(dnpPhotoPrint, portNum, ctx)) {
                                return;
                            }

                            // 画像3 のデータを送る
                            switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 3);
                        }

                    } else{
                        /* 2枚目に "無し" が選択されていた時 */
                        // PrintImageData で印刷を開始
                        writeLog("PrintImageData");
                        result = dnpPhotoPrint.PrintImageData(portNum);
                        writeLog("PrintImageData:" + result);
                    }

                } else {
                    /* リボンパネル部分使用時の処理 */
                    // イメージ展開に必要な設定項目の設定
                    firstPrintSetting(dnpPhotoPrint, printData, portNum);

                    // バッファチェック
                    if (!checkFreeBuffer(dnpPhotoPrint, portNum, ctx)) {
                        return;
                    }

                    // 印刷動作前に必要な設定項目の設定
                    secondPrintSetting(dnpPhotoPrint, printData, portNum);

                    // 画像1 のデータを送る
                    switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 1);

                    if (printData.imageWidth2 != 0) {
                        /* 2枚目の画像が選択されていた時の処理 */
                        // メディアサイズを2面割付に切り替える
                        tempMediaSize = tempMediaSizeToTwoSides(printData.mediaSize);
                        // 2面割付用紙を設定
                        writeLog("SetMediaSize:" + tempMediaSize);
                        result = dnpPhotoPrint.SetMediaSize(portNum, tempMediaSize);
                        writeLog("SetMediaSize:" + result);

                        // バッファチェック
                        if (!checkFreeBuffer(dnpPhotoPrint, portNum, ctx)) {
                            return;
                        }

                        // 画像2 のデータを送る
                        switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 2);

                        if (printData.imageWidth3 != 0) {
                            /* 3枚目の画像が選択されていた時の処理 */
                            // 画像3 のデータを送る
                            switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 3);
                        } else{
                            /* 3枚目に "無し" が選択されていた時 */
                            // PrintImageData で印刷を開始
                            writeLog("PrintImageData");
                            result = dnpPhotoPrint.PrintImageData(portNum);
                            writeLog("PrintImageData:" + result);
                        }
                    }
                }
                // ステータスと印刷枚数をログファイルに記載
                dumpStatusAndPQTYInLogFile(dnpPhotoPrint, portNum);

                break;


            default:
                /* 用紙サイズにリワインド用紙以外を選択した時の処理 */

                // リワインドモード設定
                writeLog("SetRewindMode:" + Common.REWIND_MODE_ON);
                result = dnpPhotoPrint.SetRewindMode(portNum, Common.REWIND_MODE_ON);
                writeLog("SetRewindMode:" + result);

                // リボンの使用状況を取得
                writeLog("GetMediaCounterH");
                mediaCounterH = dnpPhotoPrint.GetMediaCounterH(portNum);
                if(Common.TEST_MODE == 2){
                    // 単体テスト用の処理
                    mediaCounterH = 2;
                }
                writeLog("GetMediaCounterH:" + mediaCounterH);
                if (mediaCounterH % 2 == 0) {
                    /* リボンパネル未使用時の処理 */
                    // イメージ展開に必要な設定項目の設定
                    firstPrintSetting(dnpPhotoPrint, printData, portNum);

                    // バッファチェック
                    if (!checkFreeBuffer(dnpPhotoPrint, portNum, ctx)) {
                        return;
                    }

                    // 印刷動作前に必要な設定項目の設定
                    secondPrintSetting(dnpPhotoPrint, printData, portNum);

                    // 画像1 のデータを送る
                    switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 1);

                    if (printData.imageWidth2 != 0) {
                        /* 2枚目の画像が選択されていた時の処理 */
                        // 画像2 のデータを送る
                        switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 2);

                        if (printData.imageWidth3 != 0) {
                            /* 3枚目の画像が選択されていた時の処理 */
                            // 用紙サイズを設定
                            writeLog("SetMediaSize:" + printData.mediaSize);
                            result = dnpPhotoPrint.SetMediaSize(portNum, printData.mediaSize);
                            writeLog("SetMediaSize:" + result);

                            // バッファチェック
                            if (!checkFreeBuffer(dnpPhotoPrint, portNum, ctx)) {
                                return;
                            }

                            // 画像3 のデータを送る
                            switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 3);

                            // PrintImageData で印刷を開始する
                            writeLog("PrintImageData");
                            result = dnpPhotoPrint.PrintImageData(portNum);
                            writeLog("PrintImageData:" + result);
                        }

                    } else{
                        /* 2枚目に "無し" が選択されていた時 */
                        // PrintImageData で印刷を開始
                        writeLog("PrintImageData");
                        result = dnpPhotoPrint.PrintImageData(portNum);
                        writeLog("PrintImageData:" + result);
                    }

                } else {
                    /* リボンパネル部分使用時の処理 */
                    // イメージ展開に必要な設定項目の設定
                    firstPrintSetting(dnpPhotoPrint, printData, portNum);

                    // バッファチェック
                    if (!checkFreeBuffer(dnpPhotoPrint, portNum, ctx)) {
                        return;
                    }

                    // 印刷動作前に必要な設定項目の設定
                    secondPrintSetting(dnpPhotoPrint, printData, portNum);

                    // 画像1 のデータを送る
                    switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 1);

                    // PrintImageData で印刷を開始する
                    writeLog("PrintImageData");
                    result = dnpPhotoPrint.PrintImageData(portNum);
                    writeLog("PrintImageData:" + result);

                    if (printData.imageWidth2 != 0) {
                        /* 2枚目の画像が選択されていた時の処理 */
                        // 用紙サイズを設定
                        writeLog("SetMediaSize:" + printData.mediaSize);
                        result = dnpPhotoPrint.SetMediaSize(portNum, printData.mediaSize);
                        writeLog("SetMediaSize:" + result);

                        // バッファチェック
                        if (!checkFreeBuffer(dnpPhotoPrint, portNum, ctx)) {
                            return;
                        }

                        // 画像2 のデータを送る
                        switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 2);

                        if (printData.imageWidth3 != 0) {
                            /* 3枚目の画像が選択されていた時の処理 */
                            // 画像3 のデータを送る
                            switchSendImageDataByImageNum(dnpPhotoPrint, printData, portNum, 3);
                        } else {
                            /* 3枚目に "無し" が選択されていた時 */
                            // PrintImageData で印刷を開始する
                            writeLog("PrintImageData");
                            result = dnpPhotoPrint.PrintImageData(portNum);
                            writeLog("PrintImageData:" + result);
                        }
                    }
                }

                // リワインドモード解除
                writeLog("SetRewindMode:" + Common.REWIND_MODE_OFF);
                result = dnpPhotoPrint.SetRewindMode(portNum, Common.REWIND_MODE_OFF);
                writeLog("SetRewindMode:" + result);

                // ステータスと印刷枚数をログファイルに記載
                dumpStatusAndPQTYInLogFile(dnpPhotoPrint, portNum);

                break;
        }
    }

    /**
     * イメージ展開に必要な設定項目の設定・ログに出力
     * @param dnpPhotoPrint dnpPhotoPrintクラスのインスタンス
     * @param printData     PrintDataクラスのインスタンス
     */
    private void firstPrintSetting(DNPPhotoPrint dnpPhotoPrint, PrintData printData, int portNum) {
        /* dnpPhotoPrint 関数呼び出し結果を格納 */
        boolean result;

        // 解像度
        writeLog("SetResolution:" + printData.resolution);
        result = dnpPhotoPrint.SetResolution(portNum, printData.resolution);
        writeLog("SetResolution:" + result);
        // 用紙サイズ
        writeLog("SetMediaSize:" + printData.mediaSize);
        result = dnpPhotoPrint.SetMediaSize(portNum, printData.mediaSize);
        writeLog("SetMediaSize:" + result);
    }

    /**
     * 印刷動作前に必要な設定項目の設定・ログに出力
     * @param dnpPhotoPrint dnpPhotoPrintクラスのインスタンス
     * @param printData     PrintDataクラスのインスタンス
     */
    private void secondPrintSetting(DNPPhotoPrint dnpPhotoPrint, PrintData printData, int portNum) {
        /* dnpPhotoPrint 関数呼び出し結果を格納 */
        boolean result;

        // 印刷枚数
        writeLog("SetPQTY:" + printData.pQTY);
        result = dnpPhotoPrint.SetPQTY(portNum, printData.pQTY);
        writeLog("SetPQTY:" + result);
        // カッターモード
        writeLog("SetCutterMode:" + printData.cutterMode);
        result = dnpPhotoPrint.SetCutterMode(portNum, printData.cutterMode);
        writeLog("SetCutterMode:" + result);
        // オーバーコート
        writeLog("SetOvercoatFinish:" + printData.overCoat);
        result = dnpPhotoPrint.SetOvercoatFinish(portNum, printData.overCoat);
        writeLog("SetOvercoatFinish:" + result);
        // リトライ印刷
        writeLog("SetRetryControl:" + printData.retryPrint);
        result = dnpPhotoPrint.SetRetryControl(portNum, printData.retryPrint);
        writeLog("SetRetryControl:" + result);
    }

    /**
     * 空きバッファ数をチェック・ログに出力し、bool値で結果を返す
     * @param dnpPhotoPrint dnpPhotoPrintクラスのインスタンス
     * @param portNum       チェック対象プリンターのポート番号
     * @param ctx           コンテキスト
     * @return true:空き有り　false:空き無し
     */
    private boolean checkFreeBuffer(DNPPhotoPrint dnpPhotoPrint, int portNum, final Context ctx) {
        writeLog("GetFreeBuffer");
        int freeBuf = dnpPhotoPrint.GetFreeBuffer(portNum);
        if(Common.TEST_MODE == 2 || Common.TEST_MODE == 3){
            // 単体テスト用の処理
            freeBuf = 2;
        }
        writeLog("GetFreeBuffer:" + freeBuf);
        if (freeBuf < 1) {
            Common.showToastUiThread(ctx, "设备被占用 portNum="+portNum);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 設定したリワインド用紙に対応した2画面割付用紙サイズNo.を返す
     * @param mediaSize 設定されているリワインド用紙
     * @return 切り替えた用紙サイズ
     */
    private int tempMediaSizeToTwoSides(int mediaSize) {
        int tempMediaSize = 0;

        switch (mediaSize) {
            case DNPPhotoPrint.CSP_PC_REWIND:
                tempMediaSize = DNPPhotoPrint.CSP_PCx2;
                break;
            case DNPPhotoPrint.CSP_L_REWIND:
                tempMediaSize = DNPPhotoPrint.CSP_Lx2;
                break;
            case DNPPhotoPrint.CSP_6x4P5_REWIND:
                tempMediaSize = DNPPhotoPrint.CSP_6x4P5x2;
                break;
            case DNPPhotoPrint.CSP_8x4_REWIND:
                tempMediaSize = DNPPhotoPrint.CSP_8x4x2;
                break;
            case DNPPhotoPrint.CSP_8x5_REWIND:
                tempMediaSize = DNPPhotoPrint.CSP_8x5x2;
                break;
            case DNPPhotoPrint.CSP_8x6_REWIND:
                tempMediaSize = DNPPhotoPrint.CSP_8x6x2;
                break;
            case DNPPhotoPrint.CSP_A5_REWIND:
                tempMediaSize = DNPPhotoPrint.CSP_A5x2;
                break;
            case DNPPhotoPrint.CSP_A4x5_REWIND:
                tempMediaSize = DNPPhotoPrint.CSP_A4x5x2;
                break;
            default:
                break;

        }
        return tempMediaSize;
    }


    /**
     * 画像選択番号によって SendImageData を使い分ける
     * また、関数呼び出しログを出力する
     * @param dnpPhotoPrint dnpPhotoPrintクラスのインスタンス
     * @param printData     PrintDataクラスのインスタンス
     * @param num           画像選択番号
     */
    private void switchSendImageDataByImageNum(DNPPhotoPrint dnpPhotoPrint, PrintData printData, int portNum, int num) {
        /* dnpPhotoPrint 関数呼び出し結果を格納 */
        boolean result;

        switch (num) {
            case 1:
                writeLog("SendImageData:" + printData.imageFileName1 + "," + 0 + "," + 0 + "," + printData.imageWidth1 + "," + printData.imageHeight1);
                result = dnpPhotoPrint.SendImageData(portNum,
                        printData.imageRGBData1,
                        0,
                        0,
                        printData.imageWidth1,
                        printData.imageHeight1);
                break;

            case 2:
                writeLog("SendImageData:" + printData.imageFileName2 + "," + 0 + "," + 0 + "," + printData.imageWidth2 + "," + printData.imageHeight2);
                result = dnpPhotoPrint.SendImageData(portNum,
                        printData.imageRGBData2,
                        0,
                        0,
                        printData.imageWidth2,
                        printData.imageHeight2);
                break;
            default:
                writeLog("SendImageData:" + printData.imageFileName3 + "," + 0 + "," + 0 + "," + printData.imageWidth3 + "," + printData.imageHeight3);
                result = dnpPhotoPrint.SendImageData(portNum,
                        printData.imageRGBData3,
                        0,
                        0,
                        printData.imageWidth3,
                        printData.imageHeight3);
                break;
        }
        writeLog("SendImageData:" + result);
    }

    /**
     * 1000ms 単位でステータス、残り印刷枚数を取得し、前回と比較して変わっていたらログに記載
     * @param dnpPhotoPrint dnpPhotoPrintクラスのインスタンス
     * @param portNum       Status, PQTY 取得対象のプリンターポート番号
     */
    private void dumpStatusAndPQTYInLogFile(final DNPPhotoPrint dnpPhotoPrint, final int portNum) {
        int statusBefore = 0;
        int pQtYBefore = 0;
        int statusNow = 0;
        int pQTYNow = 0;

        while (true){
            /* 現在のステータスを格納する */
            statusNow = dnpPhotoPrint.GetStatus(portNum);

            if (statusNow != statusBefore) {
                /* ステータスが変わっていた場合の処理 */
                writeLog("GetStatus:" + statusNow);

                /* 前回のステータスを更新 */
                statusBefore = statusNow;
            }

            /* 現在の残り印刷枚数を取得する */
            pQTYNow = dnpPhotoPrint.GetPQTY(portNum);
            if (pQTYNow != pQtYBefore) {
                /* ステータスが変わっていた場合の処理 */
                writeLog("GetPQTY:" + pQTYNow);

                /* 前回の残り印刷枚数を更新 */
                pQtYBefore = pQTYNow;
            }

            if (pQtYBefore <= 0) {
                /* 残り印刷枚数が 0 以下となった時、処理を終了 */
                return;
            }

            if(mLc.isFileClose()){
                /* ログファイルがクローズされている時、処理を終了 */
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Common.dumpLog("error", e);
            }
        }
    }

    /**
     * 印刷制御ファイルを基に実行する
     * @param iniData INIファイルから読み込んだデータ
     * @param portNum ポート番号
     * @param ctx     コンテキスト
     */
    public void doIniPrint(List<AbstractMap.SimpleEntry<String, ArrayList<AbstractMap.SimpleEntry<String, String[]>>>> iniData, int portNum, Context ctx) {
        DNPPhotoPrint dnpPhotoPrint = Common.getDNPInstance();

        /* dnpPhotoPrint 関数呼び出し結果を格納 */
        boolean resultBoor;
        int resultInt;

        for (AbstractMap.SimpleEntry<String, ArrayList<AbstractMap.SimpleEntry<String, String[]>>> sectionData : iniData) {
//            dumpLog("SECTION NAME : " + sectionData.getKey());
            try{
                // セクション名をログファイルに書き込み
                writeSection("[" + sectionData.getKey() + "]");

                List<AbstractMap.SimpleEntry<String, String[]>> paramList = sectionData.getValue();
                for (AbstractMap.SimpleEntry<String, String[]> param : paramList) {
                    // 関数名取得
                    String funcName = param.getKey();
                    // 関数パラメーター取得
                    String[] values = param.getValue();

//                dumpLog("  PARAM NAME : " + funcName);
//                for (String value : values) {
//                    dumpLog("    VALUE  : " + value);
//                }

                    // 各関数を呼び出す
                    if (funcName.equals("GetPrinterPortNum")) {

                        /* 接続プリンター情報を格納 */
                        int[][] printers = new int[10][2];

                        /* ログ出力文字列を格納 */
                        StringBuilder sb = new StringBuilder();

                        /* 接続プリンター台数を格納 */
                        int printerNums = 0;

                        /* 関数呼び出しログ */
                        writeLog(funcName);

                        /* 関数で検索 */
                        printerNums = dnpPhotoPrint.GetPrinterPortNum(printers);

                        /* ログ出力文字列を作成 */
                        for (int i = 0; printers[i][0] != 0; i++) {
                            sb.append(",");
                            sb.append(printers[i][1]);
                            sb.append("-");
                            sb.append(printers[i][0]);
                        }

                        /* 関数呼び出し結果ログ */
                        writeLog( funcName + ":" + printerNums + sb.toString());

                    } else if (funcName.equals("SetUSBTimeout")) {
                        int time = Integer.parseInt(values[0]);

                        writeLog("SetUSBTimeout:" + time);
                        resultBoor = dnpPhotoPrint.SetUSBTimeout(portNum, time);
                        writeLog("SetUSBTimeout:" + resultBoor);

                    } else if (funcName.equals("SetMediaSize")) {
                        int mediaSize = Integer.parseInt(values[0]);

                        writeLog("SetMediaSize:" + mediaSize);
                        resultBoor = dnpPhotoPrint.SetMediaSize(portNum, mediaSize);
                        writeLog("SetMediaSize:" + resultBoor);

                    } else if (funcName.equals("SendImageData")) {

                        String imagePath = Common.getImageDir() + "/" + values[0];

                        /* Bitmap のサイズを取得 */
                        Rect size = Common.getBitmapSize(imagePath);

                        /* 色変換後のRGBデータの取得 */
                        Common.dumpLog("image path : " + imagePath);
                        byte[] rGBData = Common.readAndConvertImageData(imagePath, ctx, size);
                        Common.dumpLog("image size : " + rGBData.length);

                        /* 出力先のX, Y座標取得 */
                        int xPos = Integer.parseInt(values[1]);
                        int yPos = Integer.parseInt(values[2]);

                        /* イメージの幅取得 */
                        int width = 0;
                        if (values[3].equals("W")) {
                            /* パラメーターが 'W' の場合画像の幅を取得する */
                            width = size.width();
                        } else {
                            width = Integer.parseInt(values[3]);
                        }

                        /* イメージの高さ取得 */
                        int height = 0;
                        if (values[4].equals("H")) {
                            /* パラメーターが 'H' の場合画像の幅を取得する */
                            height = size.height();
                        } else {
                            height = Integer.parseInt(values[4]);
                        }

                        writeLog("SendImageData:" + values[0] + "," + xPos + "," + yPos + "," + width + "," + height);
                        resultBoor = dnpPhotoPrint.SendImageData(portNum, rGBData, xPos, yPos, width, height);
                        writeLog("SendImageData:" + resultBoor);

                    } else if (funcName.equals("SetResolution")) {
                        int resolution = Integer.parseInt(values[0]);

                        writeLog("SetResolution:" + resolution);
                        resultBoor = dnpPhotoPrint.SetResolution(portNum, resolution);
                        writeLog("SetResolution:" + resultBoor);

                    } else if (funcName.equals("SetPQTY")) {
                        int pQTY = Integer.parseInt(values[0]);

                        writeLog("SetPQTY:" + pQTY);
                        resultBoor = dnpPhotoPrint.SetPQTY(portNum, pQTY);
                        writeLog("SetPQTY:" + resultBoor);

                    } else if (funcName.equals("PrintImageData")) {
                        writeLog("PrintImageData");
                        resultBoor = dnpPhotoPrint.PrintImageData(portNum);
                        writeLog("PrintImageData:" + resultBoor);

                    } else if (funcName.equals("StartPageLayout")) {
                        writeLog("StartPageLayout");
                        resultBoor = dnpPhotoPrint.StartPageLayout(portNum);
                        writeLog("StartPageLayout:" + resultBoor);

                    } else if (funcName.equals("EndPageLayout")) {
                        writeLog("EndPageLayout");
                        resultBoor = dnpPhotoPrint.EndPageLayout(portNum);
                        writeLog("EndPageLayout:" + resultBoor);

                    } else if (funcName.equals("GetFirmwVersion")) {
                        /* 受信バッファ */
                        char[] rBuf = new char[Common.RECEIVE_BUF_SIZE];

                        writeLog("GetFirmwVersion");
                        resultInt = dnpPhotoPrint.GetFirmwVersion(portNum, rBuf);
                        writeLog("GetFirmwVersion:" + resultInt + charAryToString(rBuf));

                    } else if (funcName.equals("GetMedia")) {
                        char[] rBuf = new char[Common.RECEIVE_BUF_SIZE];

                        writeLog("GetMedia");
                        resultInt = dnpPhotoPrint.GetMedia(portNum, rBuf);
                        writeLog("GetMedia:" + resultInt + charAryToString(rBuf));

                    } else if (funcName.equals("GetStatus")) {
                        writeLog("GetStatus");
                        resultInt = dnpPhotoPrint.GetStatus(portNum);
                        writeLog("GetStatus:" + resultInt);

                    } else if (funcName.equals("GetCounterL")) {
                        writeLog("GetCounterL");
                        resultInt = dnpPhotoPrint.GetCounterL(portNum);
                        writeLog("GetCounterL:" + resultInt);

                    } else if (funcName.equals("GetCounterA")) {
                        writeLog("GetCounterA");
                        resultInt = dnpPhotoPrint.GetCounterA(portNum);
                        writeLog("GetCounterA:" + resultInt);

                    } else if (funcName.equals("GetCounterB")) {
                        writeLog("GetCounterB");
                        resultInt = dnpPhotoPrint.GetCounterB(portNum);
                        writeLog("GetCounterB:" + resultInt);

                    } else if (funcName.equals("GetCounterP")) {
                        writeLog("GetCounterP");
                        resultInt = dnpPhotoPrint.GetCounterP(portNum);
                        writeLog("GetCounterP:" + resultInt);

                    } else if (funcName.equals("GetCounterMatte")) {
                        writeLog("GetCounterMatte");
                        resultInt = dnpPhotoPrint.GetCounterMatte(portNum);
                        writeLog("GetCounterMatte:" + resultInt);

                    } else if (funcName.equals("GetCounterM")) {
                        writeLog("GetCounterM");
                        resultInt = dnpPhotoPrint.GetCounterM(portNum);
                        writeLog("GetCounterM:" + resultInt);

                    } else if (funcName.equals("GetFreeBuffer")) {
                        writeLog("GetFreeBuffer");
                        resultInt = dnpPhotoPrint.GetFreeBuffer(portNum);
                        writeLog("GetFreeBuffer:" + resultInt);

                    } else if (funcName.equals("GetPQTY")) {
                        writeLog("GetPQTY");
                        resultInt = dnpPhotoPrint.GetPQTY(portNum);
                        writeLog("GetPQTY:" + resultInt);

                    } else if (funcName.equals("GetMediaCounter")) {
                        writeLog("GetMediaCounter");
                        resultInt = dnpPhotoPrint.GetMediaCounter(portNum);
                        writeLog("GetMediaCounter:" + resultInt);

                    } else if (funcName.equals("GetMediaLotNo")) {
                        char[] rBuf = new char[Common.RECEIVE_BUF_SIZE];

                        writeLog("GetMediaLotNo");
                        resultInt = dnpPhotoPrint.GetMediaLotNo(portNum, rBuf);
                        writeLog("GetMediaLotNo:" + resultInt + charAryToString(rBuf));

                    } else if (funcName.equals("GetSerialNo")) {
                        char[] rBuf = new char[Common.RECEIVE_BUF_SIZE];

                        writeLog("GetSerialNo");
                        resultInt = dnpPhotoPrint.GetSerialNo(portNum, rBuf);
                        writeLog("GetSerialNo:" + resultInt + charAryToString(rBuf));

                    } else if (funcName.equals("SetCutterMode")) {
                        int cutterMode = Integer.parseInt(values[0]);

                        writeLog("SetCutterMode:" + cutterMode);
                        resultBoor = dnpPhotoPrint.SetCutterMode(portNum, cutterMode);
                        writeLog("SetCutterMode:" + resultBoor);

                    } else if (funcName.equals("SetOvercoatFinish")) {
                        int overcoat = Integer.parseInt(values[0]);

                        writeLog("SetOvercoatFinish:" + overcoat);
                        resultBoor = dnpPhotoPrint.SetOvercoatFinish(portNum, overcoat);
                        writeLog("SetOvercoatFinish:" + resultBoor);

                    } else if (funcName.equals("SetRetryControl")) {
                        int retryControl = Integer.parseInt(values[0]);

                        writeLog("SetRetryControl:" + retryControl);
                        resultBoor = dnpPhotoPrint.SetRetryControl(portNum, retryControl);
                        writeLog("SetRetryControl:" + resultBoor);

                    } else if (funcName.equals("GetRfidMediaClass")) {
                        char[] rBuf = new char[Common.RECEIVE_BUF_SIZE];

                        writeLog("GetRfidMediaClass");
                        resultInt = dnpPhotoPrint.GetRfidMediaClass(portNum, rBuf);
                        writeLog("GetRfidMediaClass:" + resultInt + charAryToString(rBuf));

                    } else if (funcName.equals("GetInitialMediaCount")) {
                        writeLog("GetInitialMediaCount");
                        resultInt = dnpPhotoPrint.GetInitialMediaCount(portNum);
                        writeLog("GetInitialMediaCount:" + resultInt);

                    } else if (funcName.equals("SetUSBSerialEnable")) {
                        int enable = Integer.parseInt(values[0]);

                        writeLog("SetUSBSerialEnable:" + enable);
                        resultBoor = dnpPhotoPrint.SetUSBSerialEnable(portNum, enable);
                        writeLog("SetUSBSerialEnable:" + resultBoor);

                    } else if (funcName.equals("GetUSBSerialEnable")) {
                        writeLog("GetUSBSerialEnable");
                        resultInt = dnpPhotoPrint.GetUSBSerialEnable(portNum);
                        writeLog("GetUSBSerialEnable:" + resultInt);

                    } else if (funcName.equals("GetMediaCounterH")) {
                        writeLog("GetMediaCounterH");
                        resultInt = dnpPhotoPrint.GetMediaCounterH(portNum);
                        writeLog("GetMediaCounterH:" + resultInt);

                    } else if (funcName.equals("SetRewindMode")) {
                        int mode = Integer.parseInt(values[0]);

                        writeLog("SetRewindMode:" + mode);
                        resultBoor = dnpPhotoPrint.SetRewindMode(portNum, mode);
                        writeLog("SetRewindMode:" + resultBoor);

                    } else if (funcName.equals("GetRewindMode")) {
                        writeLog("GetRewindMode");
                        resultInt = dnpPhotoPrint.GetRewindMode(portNum);
                        writeLog("GetRewindMode:" + resultInt);

                    } else if (funcName.equals("sleep")) {
                        long millis = Long.parseLong(values[0]);
                        resultBoor = true;

                        writeLog("sleep:" + millis);
                        try {
                            Thread.sleep(millis);
                        } catch (InterruptedException e) {
                            Common.dumpLog("", e);
                            resultBoor = false;
                        } finally {
                            writeLog("sleep:" + resultBoor);
                        }
                    }
                }
            } catch (Exception e){
                Common.dumpLog("error", e);
            } finally {
                // セクションの終了で改行させる
                writeLog("");
            }
        }
    }

    /**
     * 文字配列を受け取って、文字列に変換する
     * @param chars 変換したい文字型配列
     * @return 変換した文字列
     */
    private String charAryToString(char[] chars) {
        String str;
        StringBuilder sb = new StringBuilder();

        if (chars[0] != NULL) {
            /* 文字列を作成 */
            sb.append(",");
            for (int i = 0; chars[i] != NULL; i++) {
                sb.append(chars[i]);
            }
        }
        str = sb.toString();

        return str;
    }


    /**
     * 非同期で基本印刷を開始する
     * @param ctx        コンテキスト
     * @param portNumber ポート番号
     * @param printData  印刷データ
     * @param printer    印刷対象のプリンター
     * @param solidId    プリンターの固体識別子
     */
    public static void doBasicPrintAsync(Context ctx, int portNumber, PrintData printData, EPrinter printer, int solidId) {
        LogUtils.i("doBasicPrintAsync-》调用开始打印方法 -"+printer.displayName);
        /* PrintThread クラスをインスタンス化 */
        PrintThread printThread = new PrintThread(ctx, portNumber, printData, printer, solidId);
        LogUtils.i("PrintThread-》创建打印线程 -"+printer.displayName);
        /* 非同期印刷を開始する */
        printThread.startBasicPrint();
        LogUtils.i("startBasicPrint-》开始打印-"+printer.displayName);
    }

    /**
     * 非同期で巻戻し印刷を開始する
     * @param ctx        コンテキスト
     * @param portNumber ポート番号
     * @param printData  印刷データ
     * @param printer    印刷対象のプリンター
     * @param solidId    プリンターの固体識別子
     */
    public static void doRewindPrintAsync(Context ctx, int portNumber, PrintData printData, EPrinter printer, int solidId) {
        /* PrintThread クラスをインスタンス化 */
        PrintThread printThread = new PrintThread(ctx, portNumber, printData, printer, solidId);

        /* 非同期印刷を開始する */
        printThread.startRewindPrint();
    }

    /**
     * 非同期で連続評価を開始する
     * @param ctx        コンテキスト
     * @param portNumber ポート番号
     * @param reader     INIファイルを読み込んだIniFileReader
     * @param printer    印刷対象のプリンター
     * @param solidId    プリンターの固体識別子
     */
    public static void doIniPrintAsync(Context ctx, int portNumber, IniFileReader reader, EPrinter printer, int solidId) {
        /* PrintThread クラスをインスタンス化 */
        PrintThread printThread = new PrintThread(ctx, portNumber, reader, printer, solidId);

        /* 非同期印刷を開始する */
        printThread.startControlPrint();
    }

    /**
     * 印刷スレッドクラス
     */
    static class PrintThread extends Thread {
        /** コンテキスト */
        private Context mCtx;
        /** ポート番号 */
        private int mPortNumber;
        /** 印刷設定データ */
        private PrintData mPrintData;
        /** INIファイルを読み込んだIniFileReader */
        private IniFileReader mIniFileReader;
        /** 印刷モード */
        private PrintMode mMode;
        /** 機種情報 */
        private EPrinter mPrinter;
        /** プリンターの固体識別子 */
        private int mSolidId;

        /**
         * コンストラクタ（基本印刷／巻き戻し印刷）
         * @param ctx        コンテキスト
         * @param portNumber ポート番号
         * @param printData  印刷データ
         * @param printer    印刷対象のプリンター
         * @param solidId    プリンターの固体識別子
         */
        PrintThread(Context ctx, int portNumber, PrintData printData, EPrinter printer, int solidId) {
            mCtx = ctx;
            mPortNumber = portNumber;
            mPrintData = printData;
            mPrinter = printer;
            mSolidId = solidId;
        }

        /**
         * コンストラクタ（連続評価）
         * @param ctx        コンテキスト
         * @param portNumber ポート番号
         * @param reader     INIファイルから読み込んだIniFileReader
         * @param printer    印刷対象のプリンター
         * @param solidId    プリンターの固体識別子
         */
        PrintThread(Context ctx, int portNumber, IniFileReader reader, EPrinter printer, int solidId) {
            mCtx = ctx;
            mPortNumber = portNumber;
            mIniFileReader = reader;
            mPrinter = printer;
            mSolidId = solidId;
        }

        /**
         * 非同期で実行されるメソッド
         */
        @Override
        public void run() {
            if (mMode == null) {
                return;
            }

            String iniFileName = mMode == PrintMode.CONTROL ? mIniFileReader.getFileName() : null;
            LogCreator lc = new LogCreator(mCtx, mMode, mPrinter, iniFileName, mSolidId);
            PrintManager printManager = new PrintManager(lc);
            try {
                // ログファイルオープン
                lc.openFile();
                // 印刷処理を実行
                switch (mMode) {
                    case BASIC:
                    case BASIC_P:
                        printManager.doBasicPrint(mPrintData, mPortNumber, mCtx);
                        break;
                    case REWIND:
                        printManager.doRewindPrint(mPrintData, mPortNumber, mCtx);
                        break;
                    case CONTROL:
                        printManager.doIniPrint(mIniFileReader.getData(), mPortNumber, mCtx);
                        break;
                }
            } catch (Exception e) {
                LogUtils.i("打印线程报错 "+mPrinter.displayName+"\n"+e.getMessage());
                Common.dumpLog("[PrintManager] error", e);
            } finally {
                // ログファイルクローズ
                lc.closeFile();
            }
        }

        /**
         * 非同期で基本印刷を実行する
         */
        void startBasicPrint() {
            mMode = mPrintData.chkBoxPageLayout ? PrintMode.BASIC_P : PrintMode.BASIC;
            this.start();
        }

        /**
         * 非同期で巻き戻し印刷を実行する
         */
        void startRewindPrint() {
            mMode = PrintMode.REWIND;
            this.start();
        }

        /**
         * 非同期で連続評価を実行する
         */
        void startControlPrint() {
            mMode = PrintMode.CONTROL;
            this.start();
        }
    }

    /**
     * 印刷モード
     */
    public enum PrintMode {
        /** 印刷モード：基本印刷 */
        BASIC("Basic"),
        /** 印刷モード：基本印刷(ページレイアウト関数を使用) */
        BASIC_P("BasicP"),
        /** 印刷モード：リボン巻戻し印刷 */
        REWIND("Rewind"),
        /** 印刷モード：印刷制御ファイル実行 */
        CONTROL(""),
        /** 印刷モード：プリンター検索 */
        SEARCH_PRINTER("Search");

        /** ログファイル名に設定する評価名 */
        public String testName;

        /**
         * コンストラクタ
         * @param testName 評価名
         */
        PrintMode(String testName) {
            this.testName = testName;
        }
    }

    /**
     * プリンターの enum
     */
    public enum EPrinter {
        /** プリンター：DS40 */
        CX_DEF(3, "DS40"),
        /** プリンター：DS80 */
        CXW_DEF(4, "DS80"),
        /** プリンター：DS-RX1 */
        CY_DEF(5, "DS-RX1"),
        /** プリンター：DP-DS620 */
        DS620_DEF(20, "DP-DS620"),
        /** プリンター：DP-WQ410 */
        QW410_DEF(35, "DP-WQ410"),
        /** プリンター：DP-DS820 */
        DS820_DEF(30, "DP-DS820");

        /** プリンター識別子 */
        public int id;

        /** UI の表示名称 */
        public String displayName;

        /**
         * コンストラクタ
         * @param id            プリンターの識別子
         * @param displayName   UI の表示名称
         */
        EPrinter(int id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
    }

    /**
     * ログの書き込みを行う
     */
    private void writeLog(String msg) {
        if (mLc != null) {
            if(msg.isEmpty()){
                mLc.write(msg, false);
            } else {
                mLc.write(msg, true);
            }

        }
    }

    /**
     * ログの書き込みを行う
     * (セクション用)
     */
    private void writeSection(String msg) {
        if (mLc != null) {
            mLc.write(msg, false);
        }
    }
}
