package com.qupai.lib_printer.dnp_dsrx1.PrintOptions;


import com.qupai.lib_printer.dnp_dsrx1.common.Common;

import jp.co.dnp.photoprintlib.DNPPhotoPrint;

/**
     * 解像度の enum 定義
     */
public enum EResolution {
        /**
         * 解像度：300 x 300 dpi
         */
        RESO300(DNPPhotoPrint.RESOLUTION300, "300:RESOLUTION300"),
        /**
         * 解像度：300 x 600 dpi
         */
        RESO600(DNPPhotoPrint.RESOLUTION600, "600:RESOLUTION600"),
        /**
         * 解像度：SDKに渡す数値を任意指定
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
        EResolution(int value, String displayName) {
            mValue = value;
            mDisplayName = displayName;
        }
    }