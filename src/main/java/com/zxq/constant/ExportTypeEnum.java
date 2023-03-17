package com.zxq.constant;

/**
 * 导出的文件类型
 */
public enum ExportTypeEnum {

    Excel("excel", "excel文件"),
    Pdf("pdf", "pdf文件");

    ExportTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * 文件类型
     */
    private String type;

    /**
     * 类型描述
     */
    private String name;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static boolean isExcelType(String type) {
        return Excel.type.equals(type);
    }
}
