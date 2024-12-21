package com.qupai.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class UsbUtils {
    public static void resetUsbOtg() {
//        UsbUtils.saveDataToFile("150 0"); //usb otg关
//        UsbUtils.saveDataToFile("153 0"); //usb otg关
//        sleep(1000);
//        UsbUtils.saveDataToFile("150 1"); //usb otg开
//        UsbUtils.saveDataToFile("153 1"); //usb otg开
    }

    private static void saveDataToFile(String data) {
        BufferedWriter writer = null;
        File file = new File("/proc/proembed/gpio");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
