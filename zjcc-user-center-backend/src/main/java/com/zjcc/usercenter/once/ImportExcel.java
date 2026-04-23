package com.zjcc.usercenter.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 导入Excel
 */
public class ImportExcel {
    public static void main(String[] args) {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: EasyExcel 3.0.0-beta1
        // Excel数据文件放在自己电脑上，能够找到的路径
        String fileName = "E:\\development\\Java\\codefather-projects\\user-center\\zjcc-user-center\\zjcc-user-center-backend\\src\\main\\resources\\testExcel.xlsx";

        // Test
        // readByListener(fileName);
        // synchronousRead(fileName);

        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuTableUserInfo> userInfoList = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        System.out.println("总数 = " + userInfoList.size());
        Map<String, List<XingQiuTableUserInfo>> listMap = userInfoList.stream()
                .filter(userInfo -> StringUtils.isNotEmpty(userInfo.getUserName()))
                .collect(Collectors.groupingBy(XingQiuTableUserInfo::getUserName));
        for (Map.Entry<String, List<XingQiuTableUserInfo>> listEntry : listMap.entrySet()) {
            if (listEntry.getValue().size() > 1) {
                // 找到重复 username
                System.out.println("username = " + listEntry.getKey());
                System.out.println("====");
            }
        }
        System.out.println("不重复昵称数 = " + listMap.keySet().size());
    }

    /**
     * 监听器读取
     * @param fileName
     */
    private static void readByListener(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里每次会读取100条数据 然后返回过来 直接调用使用数据就行
        EasyExcel.read(fileName,XingQiuTableUserInfo.class,new TableListener()).sheet().doRead();
    }

    /**
     * 同步读
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */
    public static void synchronousRead(String fileName) {
        List<XingQiuTableUserInfo> list = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo tableUserInfo : list) {
            System.out.println(tableUserInfo);
        }
    }
}
