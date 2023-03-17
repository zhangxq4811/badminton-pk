package com.zxq.utils;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.aspose.cells.License;
import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * excel工具类
 */
@Slf4j
public class ExcelUtil {

    /**
     * 获取license
     * @return
     */
    private static boolean getLicense() {
        boolean result = false;
        try {
            InputStream license = ResourceUtil.getStream("license.xml");
            License aposeLic = new License();
            aposeLic.setLicense(license);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void writeExcel(File excelFile, HttpServletResponse response) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(excelFile);
        // 写excel文件
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        response.setHeader("Content-Disposition","attachment;filename=test.xls");
        ServletOutputStream outputStream = response.getOutputStream();
        // 创建缓冲区
        int len = 0;
        byte[] buffer = new byte[1024];
        // 将文件读入到文件输入流中，再写入Http响应输出流里
        while ((len = fileInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        // 关闭流
        IoUtil.close(fileInputStream);
        IoUtil.close(outputStream);
    }

    public static void excel2Pdf(File excelFile, OutputStream outputStream) {
        long old = System.currentTimeMillis();
        // 验证License
        if (!getLicense()) {
            return;
        }
        FileInputStream inputStream = null;
        try {
            if (excelFile.exists()) {
                inputStream = new FileInputStream(excelFile);
                Workbook workbook = new Workbook(inputStream);
                workbook.save(outputStream, SaveFormat.PDF);
                long now = System.currentTimeMillis();
                log.info("生成PDF共耗时：" + ((now - old) / 1000.0) + "秒");
            } else {
                log.warn(excelFile.getName() + "不存在");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
