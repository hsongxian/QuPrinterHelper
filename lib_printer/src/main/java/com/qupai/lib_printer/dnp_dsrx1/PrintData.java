package com.qupai.lib_printer.dnp_dsrx1;


import com.qupai.lib_printer.dnp_dsrx1.common.Common;

/**
 * 印刷設定データを管理するクラス
 */
public class PrintData {
    /** 画像1 のファイル名 */
    public String imageFileName1 = null;
    /** 画像1 のパス */
    public String imagePath1 = null;
    /** 画像1 のバイト配列 */
    public byte[] imageRGBData1 = null;
    /** 画像1 の幅 */
    public int imageWidth1 = 0;
    /** 画像1 の高さ */
    public int imageHeight1 = 0;

    /** 画像2 のファイル名 */
    public String imageFileName2 = null;
    /** 画像2 のパス */
    public String imagePath2 = null;
    /** 画像2 のバイト配列 */
    public byte[] imageRGBData2 = null;
    /** 画像2 の幅 */
    public int imageWidth2 = 0;
    /** 画像2 の高さ */
    public int imageHeight2 = 0;

    /** 画像3 のファイル名 */
    public String imageFileName3 = null;
    /** 画像3 のパス */
    public String imagePath3 = null;
    /** 画像3 のバイト配列 */
    public byte[] imageRGBData3 = null;
    /** 画像3 の幅 */
    public int imageWidth3 = 0;
    /** 画像3 の高さ */
    public int imageHeight3 = 0;

    /** 用紙サイズを格納 */
    public int mediaSize = 0;
    /** 解像度を格納 */
    public int resolution = 0;
    /** 印刷枚数を格納 */
    public int pQTY = 0;
    /** カッターモードを格納 */
    public int cutterMode = 0;
    /** オーバーコートを格納 */
    public int overCoat = 0;
    /** リトライ印刷を格納 */
    public int retryPrint = 0;
    /** ページレイアウトチェックを格納 */
    public boolean chkBoxPageLayout;

    /**
     * Debug用:現在の設定値を表示
     */
    public void displayDebug(){
        Common.dumpLog("現在の共通設定の設定値をログに表示します。" +
                "\n画像1 　　　　　：" + "横幅：" + imageWidth1 + "  高さ：" + imageHeight1 +
                "\n画像2 　　　　　：" + "横幅：" + imageWidth2 + "  高さ：" + imageHeight2 +
                "\n画像3 　　　　　：" + "横幅：" + imageWidth3 + "  高さ：" + imageHeight3 +
                "\n用紙サイズ　　　：" + mediaSize +
                "\n解像度　　　　　：" + resolution +
                "\n印刷枚数　　　　：" + pQTY +
                "\nカッターモード　：" + cutterMode +
                "\nオーバーコート　：" + overCoat +
                "\nリトライ印刷　　：" + retryPrint +
                "\nページレイアウト：" + chkBoxPageLayout);
    }
}
