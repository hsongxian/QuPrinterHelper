package com.qupai.lib_printer.dnp_dsrx1.PrintOptions;


import com.qupai.lib_printer.dnp_dsrx1.common.Common;

import jp.co.dnp.photoprintlib.DNPPhotoPrint;

public enum ECutterMode {
    /**
     * カッターモード：通常のカット動作
     */
    MODE_STANDARD(DNPPhotoPrint.CUTTER_MODE_STANDARD, "0:通常のカット動作"),
    /**
     * カッターモード：カットくず無し設定
     */
    MODE_NONSCRAP(DNPPhotoPrint.CUTTER_MODE_NONSCRAP, "1:カットくず無し設定"),
    /**
     * カッターモード：シングルカット
     */
    MODE_SINGLE(2, "2:シングルカット"),
    /**
     * カッターモード：2インチカット指定
     */
    MODE_2INCHCUT(DNPPhotoPrint.CUTTER_MODE_2INCHCUT, "120:2インチカット指定"),
    /**
     * カッターモード：Lカードサイズ2画面連続印刷
     */
    MODE_LCARD(130, "130:Lカードサイズ2画面連続印刷"),
    /**
     * カッターモード：SDKに渡す数値を任意指定
     */
    FREE(0, Common.ANY_SELECT);

    /**
     * 印刷制御ライブラリに渡す値
     */
    public int mValue;

    /**
     * UI の表示名称
     */
    public String mDisplayName;

    /**
     * コンストラクタ
     *
     * @param value       印刷制御ライブラリで定義している値
     * @param displayName UIの表示名称
     */
    ECutterMode(int value, String displayName) {
        mValue = value;
        mDisplayName = displayName;
    }
}