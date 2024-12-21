package com.wingyo.utils;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;

public class FileCleaner {
 
    public static void main(String[] args) {
        // 指定要清理的目录和前缀/后缀
        String directoryPath = "/path/to/your/directory";
        String prefixToRemove = "temp_"; // 指定前缀
        String suffixToRemove = ".tmp"; // 指定后缀
 
        cleanFiles(directoryPath, prefixToRemove, suffixToRemove);
    }
 
    public static void cleanFiles(String directoryPath, String prefix, String suffix) {
        File directory = new File(directoryPath);
        // 检查路径是否是一个目录
        if (directory.isDirectory()) {
            File[] filesList = directory.listFiles();
            if (filesList != null) {
                for (File file : filesList) {
                    // 判断是否以指定前缀或后缀结尾
                    if (file.isFile() && (file.getName().startsWith(prefix) || file.getName().endsWith(suffix))) {
                        // 删除文件
                        if (file.delete()) {
                            LogUtils.e("删除文件成功: " + file.getName());
                        } else {
                            LogUtils.e("删除文件失败: " + file.getName());
                        }
                    }
                }
            } else {
                LogUtils.e("No files found in the directory.");
            }
        } else {
            LogUtils.e("Invalid directory path.");
        }
    }
}