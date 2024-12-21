package com.qupai.lib_printer.dnp_dsrx1.PrintOptions;


import com.qupai.lib_printer.dnp_dsrx1.common.Common;

import jp.co.dnp.photoprintlib.DNPPhotoPrint;

/**
 * オーバーコートの enum 定義
 */
public enum EOverCoat {
    /**
     * オーバーコート仕上げ：光沢 (デフォルト)
     */
    GLOSSY(DNPPhotoPrint.OVERCOAT_FINISH_GLOSSY, "0:光沢 (デフォルト)"),
    /**
     * オーバーコート仕上げ：マット1
     */
    MATTE1(DNPPhotoPrint.OVERCOAT_FINISH_MATTE1, "1:マット1"),
    /**
     * オーバーコート仕上げ：マット2
     */
//    MATTE2(DNPPhotoPrint.OVERCOAT_FINISH_MATTE2, "2:マット2"),
    /**
     * オーバーコート仕上げ：マット3
     */
//    MATTE3(DNPPhotoPrint.OVERCOAT_FINISH_MATTE3, "3:マット3"),
    /**
     * オーバーコート仕上げ：ファインマット
     */
    FINEMATTE(DNPPhotoPrint.OVERCOAT_FINISH_FINEMATTE, "21:ファインマット"),
    /**
     * オーバーコート仕上げ：ラスター
     */
    LUSTER(DNPPhotoPrint.OVERCOAT_FINISH_LUSTER, "22:ラスター"),
    /**
     * オーバーコート仕上げ：部分マット (マット)
     */
    PMATTE11(DNPPhotoPrint.OVERCOAT_FINISH_PMATTE11, "101:部分マット (マット)"),
    /**
     * オーバーコート仕上げ：部分マット (ファインマット)
     */
    PMATTE12(DNPPhotoPrint.OVERCOAT_FINISH_PMATTE12, "121:部分マット (ファインマット)"),
    /**
     * オーバーコート仕上げ：部分マット (ラスター)
     */
    PMATTE13(DNPPhotoPrint.OVERCOAT_FINISH_PMATTE13, "122:部分マット (ラスター)"),
    /**
     * オーバーコート仕上げ：SDKに渡す数値を任意指定
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
    EOverCoat(int value, String displayName) {
        mValue = value;
        mDisplayName = displayName;
    }
}