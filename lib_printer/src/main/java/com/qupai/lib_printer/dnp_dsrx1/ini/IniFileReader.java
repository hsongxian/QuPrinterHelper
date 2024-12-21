package com.qupai.lib_printer.dnp_dsrx1.ini;


import static com.qupai.lib_printer.dnp_dsrx1.common.Common.dumpLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;


/**
 * INIファイルを読み込むクラス
 */
public class IniFileReader {
    /** INIファイルから読み込んだデータ */
    private List<AbstractMap.SimpleEntry<String, ArrayList<AbstractMap.SimpleEntry<String, String[]>>>> mReadData;

    /** INIファイルの名称 */
    private String mFileName;

    /**
     * コンストラクタ
     */
    public IniFileReader() {
        mReadData = new ArrayList<>();
    }

    /**
     * INIファイルを読み込む
     * @param filePath ファイルパス
     */
    public void readFile(String filePath) {

        String[] paramData;
        String sectionName;
        File iniFile = new File(filePath);
        mFileName = iniFile.getName();
        try {
            // １行単位で読み込みを行うために、BufferedReader を用意する
            FileInputStream fis = new FileInputStream(iniFile);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            // １行単位で読み込みを行う
            String readLine;
            while ((readLine = br.readLine()) != null) {
                // 先頭／終端の空白を取り除く
                readLine.trim();

                if(readLine.isEmpty()) {
                    // 空行は飛ばす
                    continue;
                } else if(readLine.charAt(0) == '#') {
                    // コメント行は飛ばす
                    continue;
                } else if((sectionName = getSectionName(readLine)) != null) {
                    // セクションのとき
                    dumpLog(sectionName);

                    // セクション生成
                    AbstractMap.SimpleEntry<String, ArrayList<AbstractMap.SimpleEntry<String, String[]>>> section
                            = new AbstractMap.SimpleEntry<>(
                                    sectionName,    // セクション名
                                    new ArrayList<AbstractMap.SimpleEntry<String, String[]>>()); // パラメーターの List
                    mReadData.add(section);
                } else if((paramData = getParamData(readLine)) != null) {
                    // パラメーターのとき
                    dumpLog("KEY : '" + paramData[0] + "' VALUE : '" + paramData[1] + "'");

                    // セクションをまだ読み込んでいない場合は処理をスキップする
                    if(mReadData.size() == 0) {
                        continue;
                    }

                    // Value を "," で分割する
                    String[] values = paramData[1].split(",");

                    // 各パラメータの余分なスペースを取り除く
                    for(int i = 0; i < values.length; i++) {
                        values[i] = values[i].trim();
                    }

                    // List の 末尾 の セクション にパラメーターを追加する
                    ArrayList<AbstractMap.SimpleEntry<String, String[]>> paramList = mReadData.get(mReadData.size() - 1).getValue();
                    paramList.add(new AbstractMap.SimpleEntry<>(paramData[0], values));
                }
            }
            br.close();
            isr.close();
            fis.close();
        } catch (Exception e) {
            dumpLog("[IniFileReader] error", e);
        }
    }

    /**
     * 引数のデータからセクション名を取得する
     * @param line INIファイルの行データ
     * @return 配列の[0]番目：KEY、配列の1番目：VALUE
     *         引数のデータがセクション行でない場合は null を返却する
     */
    private String getSectionName(String line) {

        if(line.length() <= 2) {
            return null;
        }

        char firstChar = line.charAt(0);
        char lastChar = line.charAt(line.length() - 1);
        if(firstChar == '[' && lastChar == ']') {
            return line.substring(1, line.length() - 1);
        }
        return null;
    }

    /**
     * 引数のデータからパラメーターをKEYとVALUEの配列で取得する
     * @param line INIファイルの行データ
     * @return 配列の[0]番目：KEY、配列の1番目：VALUE
     *         引数のデータがパラメーター行でない場合は null を返却する
     */
    private String[] getParamData(String line) {

        if(line.length() < 3) {
            return null;
        }

        if(line.contains("=")) {
            // キーと値のペアのとき
            String[] param = line.split("=");
            // '=' で分割した要素数を確認
            if(param.length == 2) {
                param[0] = param[0].trim();
                param[1] = param[1].trim();
                return param;
            }
        } else {
            // キーのみのとき
            return new String[] {line, ""};
        }
        return null;
    }

    /**
     * INIファイルから読み込んだデータを返却する
     * @return INIファイルから読み込んだデータ
     */
    public List<AbstractMap.SimpleEntry<String, ArrayList<AbstractMap.SimpleEntry<String, String[]>>>> getData() {
        return mReadData;
    }

    /**
     * INIファイルのファイル名を返却する
     * @return INIファイルのファイル名
     */
    public String getFileName() {
        return mFileName;
    }

    /**
     * [デバッグ用]
     * 読み込んだデータをLog出力する
     */
    private void dumpReadData() {
        for(AbstractMap.SimpleEntry<String, ArrayList<AbstractMap.SimpleEntry<String, String[]>>> sectionData : mReadData) {
            dumpLog("SECTION NAME : " + sectionData.getKey());

            List<AbstractMap.SimpleEntry<String, String[]>> paramList = sectionData.getValue();
            for(AbstractMap.SimpleEntry<String, String[]> param: paramList) {
                dumpLog("  PARAM NAME : " + param.getKey());
                String[] values = param.getValue();

                for (String value : values) {
                    dumpLog("    VALUE  : " + value);
                }

            }
        }
    }
}
