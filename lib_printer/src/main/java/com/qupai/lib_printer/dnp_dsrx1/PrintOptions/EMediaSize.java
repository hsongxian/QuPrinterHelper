package com.qupai.lib_printer.dnp_dsrx1.PrintOptions;


import com.qupai.lib_printer.dnp_dsrx1.common.Common;

import jp.co.dnp.photoprintlib.DNPPhotoPrint;

public enum EMediaSize {
    /**
     * 用紙サイズ：CSP_L
     */
    CSP_L(DNPPhotoPrint.CSP_L, "1:CSP_L"),
    /**
     * 用紙サイズ：CSP_2L
     */
    CSP_2L(DNPPhotoPrint.CSP_2L, "2:CSP_2L"),
    /**
     * 用紙サイズ：CSP_PC
     */
    CSP_PC(DNPPhotoPrint.CSP_PC, "3:CSP_PC"),
    /**
     * 用紙サイズ：CSP_A5
     */
    CSP_A5(DNPPhotoPrint.CSP_A5, "4:CSP_A5"),
    /**
     * 用紙サイズ：CSP_A5W
     */
    CSP_A5W(DNPPhotoPrint.CSP_A5W, "5:CSP_A5W"),
    /**
     * 用紙サイズ：CSP_A5W
     */
    CSP_PCx2(DNPPhotoPrint.CSP_PCx2, "6:CSP_PCx2"),
    /**
     * 用紙サイズ：CSP_Lx2
     */
    CSP_Lx2(DNPPhotoPrint.CSP_Lx2, "7:CSP_Lx2"),
    /**
     * 用紙サイズ：CSP_PC_REWIND
     */
    CSP_PC_REWIND(DNPPhotoPrint.CSP_PC_REWIND, "8:CSP_PC_REWIND"),
    /**
     * 用紙サイズ：CSP_L_REWIND
     */
    CSP_L_REWIND(DNPPhotoPrint.CSP_L_REWIND, "9:CSP_L_REWIND"),
    /**
     * 用紙サイズ：CSP_5x5
     */
    CSP_5x5(DNPPhotoPrint.CSP_5x5, "10:CSP_5x5"),
    /**
     * 用紙サイズ：CSP_6x6
     */
    CSP_6x6(DNPPhotoPrint.CSP_6x6, "11:CSP_6x6"),
    /**
     * 用紙サイズ：CSP_6x4P5
     */
    CSP_6x4P5(DNPPhotoPrint.CSP_6x4P5, "12:CSP_6x4P5"),
    /**
     * 用紙サイズ：CSP_6x4P5x2
     */
    CSP_6x4P5x2(DNPPhotoPrint.CSP_6x4P5x2, "13:CSP_6x4P5x2"),
    /**
     * 用紙サイズ：CSP_6x4P5_REWIND
     */
    CSP_6x4P5_REWIND(DNPPhotoPrint.CSP_6x4P5_REWIND, "14:CSP_6x4P5_REWIND"),
    /**
     * 用紙サイズ：CSP_8x10
     */
    CSP_8x10(DNPPhotoPrint.CSP_8x10, "31:CSP_8x10"),
    /**
     * 用紙サイズ：CSP_8x12
     */
    CSP_8x12(DNPPhotoPrint.CSP_8x12, "32:CSP_8x12"),
    /**
     * 用紙サイズ：CSP_8x4
     */
    CSP_8x4(DNPPhotoPrint.CSP_8x4, "33:CSP_8x4"),
    /**
     * 用紙サイズ：CSP_8x5
     */
    CSP_8x5(DNPPhotoPrint.CSP_8x5, "34:CSP_8x5"),
    /**
     * 用紙サイズ：CSP_8x6
     */
    CSP_8x6(DNPPhotoPrint.CSP_8x6, "35:CSP_8x6"),
    /**
     * 用紙サイズ：CSP_8x8
     */
    CSP_8x8(DNPPhotoPrint.CSP_8x8, "36:CSP_8x8"),
    /**
     * 用紙サイズ：CSP_8x4x2
     */
    CSP_8x4x2(DNPPhotoPrint.CSP_8x4x2, "37:CSP_8x4x2"),
    /**
     * 用紙サイズ：CSP_8x5x2
     */
    CSP_8x5x2(DNPPhotoPrint.CSP_8x5x2, "38:CSP_8x5x2"),
    /**
     * 用紙サイズ：CSP_8x6x2
     */
    CSP_8x6x2(DNPPhotoPrint.CSP_8x6x2, "39:CSP_8x6x2"),
    /**
     * 用紙サイズ：CSP_8x5_8x4
     */
    CSP_8x5_8x4(DNPPhotoPrint.CSP_8x5_8x4, "40:CSP_8x5_8x4"),
    /**
     * 用紙サイズ：CSP_8x6_8x4
     */
    CSP_8x6_8x4(DNPPhotoPrint.CSP_8x6_8x4, "41:CSP_8x6_8x4"),
    /**
     * 用紙サイズ：CSP_8x6_8x5
     */
    CSP_8x6_8x5(DNPPhotoPrint.CSP_8x6_8x5, "42:CSP_8x6_8x5"),
    /**
     * 用紙サイズ：CSP_8x8_8x4
     */
    CSP_8x8_8x4(DNPPhotoPrint.CSP_8x8_8x4, "43:CSP_8x8_8x4"),
    /**
     * 用紙サイズ：CSP_8x4x3
     */
    CSP_8x4x3(DNPPhotoPrint.CSP_8x4x3, "44:CSP_8x4x3"),
    /**
     * 用紙サイズ：CSP_A4_LENGTH
     */
    CSP_A4_LENGTH(DNPPhotoPrint.CSP_A4_LENGTH, "45:CSP_A4_LENGTH"),
    /**
     * 用紙サイズ：CSP_8x7
     */
    CSP_8x7(DNPPhotoPrint.CSP_8x7, "46:CSP_8x7"),
    /**
     * 用紙サイズ：CSP_8x9
     */
    CSP_8x9(DNPPhotoPrint.CSP_8x9, "47:CSP_8x9"),
    /**
     * 用紙サイズ：CSP_8x4_REWIND
     */
    CSP_8x4_REWIND(DNPPhotoPrint.CSP_8x4_REWIND, "48:CSP_8x4_REWIND"),
    /**
     * 用紙サイズ：CSP_8x5_REWIND
     */
    CSP_8x5_REWIND(DNPPhotoPrint.CSP_8x5_REWIND, "49:CSP_8x5_REWIND"),
    /**
     * 用紙サイズ：CSP_8x6_REWIND
     */
    CSP_8x6_REWIND(DNPPhotoPrint.CSP_8x6_REWIND, "50:CSP_8x6_REWIND"),
    /**
     * 用紙サイズ：CSP_A5FORMAT
     */
    CSP_A5FORMAT(DNPPhotoPrint.CSP_A5FORMAT, "71:CSP_A5FORMAT"),
    /**
     * 用紙サイズ：CSP_A5x2
     */
    CSP_A5x2(DNPPhotoPrint.CSP_A5x2, "72:CSP_A5x2"),
    /**
     * 用紙サイズ：CSP_A5_REWIND
     */
    CSP_A5_REWIND(DNPPhotoPrint.CSP_A5_REWIND, "73:CSP_A5_REWIND"),
    /**
     * 用紙サイズ：CSP_A4x5
     */
    CSP_A4x5(DNPPhotoPrint.CSP_A4x5, "74:CSP_A4x5"),
    /**
     * 用紙サイズ：CSP_A4x6
     */
    CSP_A4x6(DNPPhotoPrint.CSP_A4x6, "75:CSP_A4x6"),
    /**
     * 用紙サイズ：CSP_A4x8
     */
    CSP_A4x8(DNPPhotoPrint.CSP_A4x8, "76:CSP_A4x8"),
    /**
     * 用紙サイズ：CSP_A4x10
     */
    CSP_A4x10(DNPPhotoPrint.CSP_A4x10, "77:CSP_A4x10"),
    /**
     * 用紙サイズ：CSP_A4FORMAT
     */
    CSP_A4FORMAT(DNPPhotoPrint.CSP_A4FORMAT, "78:CSP_A4FORMAT"),
    /**
     * 用紙サイズ：CSP_A4x5x2
     */
    CSP_A4x5x2(DNPPhotoPrint.CSP_A4x5x2, "79:CSP_A4x5x2"),
    /**
     * 用紙サイズ：CSP_A4x5_REWIND
     */
    CSP_A4x5_REWIND(DNPPhotoPrint.CSP_A4x5_REWIND, "80:CSP_A4x5_REWIND"),
    //添加410打印机支持尺寸
    CSP_4X4(DNPPhotoPrint.CSP_4x4, "81:CSP_4x4"),
    CSP_4X6(DNPPhotoPrint.CSP_4x6, "81:CSP_4x6"),
    /**
     * 用紙サイズ：SDKに渡す数値を任意指定
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
    EMediaSize(int value, String displayName) {
        mValue = value;
        mDisplayName = displayName;
    }
}
