package com.qupai.lib_printer.dnp_dsrx1.PrintOptions;


import com.qupai.lib_printer.dnp_dsrx1.common.Common;

import jp.co.dnp.photoprintlib.DNPPhotoPrint;

public enum ERetryPrint {
    /**
     * リトライ印刷：リトライ印刷 [無効]
     */
    OFF(DNPPhotoPrint.PRINT_RETRY_OFF, "0:PRINT_RETRY_OFF"),
    /**
     * リトライ印刷：リトライ印刷 [有効]
     */
    ON(DNPPhotoPrint.PRINT_RETRY_ON, "1:PRINT_RETRY_ON"),
    /**
     * リトライ印刷：SDKに渡す数値を任意指定
     */
    FREE(10, Common.ANY_SELECT);

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
    ERetryPrint(int value, String displayName) {
        mValue = value;
        mDisplayName = displayName;
    }
}