﻿package Control.C2FControl;

import java.io.*;
import java.util.HashMap;


/**
 * 用于读取、处理 cdd 文本文件，得到 cdd 参数
 * Created by 跃峰 on 2016/1/27.
 */
public class ReadCdd {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        File f = new File("D:\\SZ\\变频工作\\数据采集\\CDD\\20160122\\SZ01A.Log");
        ReadCdd rc = new ReadCdd();
//        String[] ss = rc.readSingleLog(f,1);
        rc.processorSingleLog(f,1);
    }

    /**
     * Instantiates a new Read cdd.
     */
    public ReadCdd(){
//        cdd = new File("/Users/huangyuefeng/Downloads/SZ01A.Log");
//        cddContent = readSingleLog(cdd);
    }

    /**
     * 读取单个 cdd-log 文件，返回一个 HashMap<Integer,String>
     *
     * @param logFile cdd-log 文件路径
     * @return 返回一个 HashMap<Integer,String>
     */
    public HashMap readSingleLog(File logFile){
        int len = 0;
        String line = "";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        HashMap<Integer,String> logContent = new HashMap<Integer, String>();

        try {
            fis = new FileInputStream(logFile);
            isr= new InputStreamReader(fis);
            br = new BufferedReader(isr);

            while((line=br.readLine())!=null){
                len++;
                logContent.put(len,line);
            }
//            System.out.println(len);
//            System.out.println(logContent.size());
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (br != null) br.close();
                if (isr != null) isr.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return logContent;
    }

    /**
     * 失败的案例
     *
     * @param logFile the log file
     */
    public void processorSingleLog(File logFile){
        HashMap<Integer,String> logContent = readSingleLog(logFile);
//        Iterator iter = logContent.entrySet().iterator();
        int mapSize = logContent.size();

        long startTime=System.currentTimeMillis();  //获取开始时间

        String bscName = "";

        int i = 1;
        while (i<mapSize){
            String s = logContent.get(i);
            if (s.contains("Connected")){
                bscName = s.substring(17,23);
            }
            i++;
            if (s.contains("RLDEP")&&!s.contains("EXT")){
            }
        }

//        for (int i=1;i<=mapSize;i++){
////            System.out.println(logContent.get(i));
//            String s = logContent.get(i);
//            if (s.contains("Connected")){
//                bscName = s.substring(17,23);
//            }else if (s.contains("RLDEP")&&!s.contains("EXT")){
//            }
//        }

//        while (iter.hasNext()){
//            HashMap.Entry entry = (HashMap.Entry) iter.next();
////            Object key = entry.getKey();
////            Object val = entry.getValue();
////            System.out.println(entry.getValue());
//        }

//        System.out.println(logContent.get(mapSize));

        long endTime=System.currentTimeMillis();    //获取结束时间
        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
    }

    /**
     * 读取单个 cdd-log 文件，通过“<”分列文件，并返回一个数组
     *
     * @param logFile cdd-log 文件路径
     * @param a       随便
     * @return 返回一个数组 string [ ]
     */
    public String[] readSingleLog(File logFile,int a) {
        FileInputStream fis = null;
        Long filelength = logFile.length();    //获取文件长度
        String fileName = logFile.getName();   //文件名
        byte[] fileContent = new byte[filelength.intValue()];
        try {
            fis = new FileInputStream(logFile);
            fis.read(fileContent);

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String s = new String(fileContent);
        String[] ss = s.split("<");
        return ss;
    }

    /**
     * Processor single log.
     *
     * @param logFile the log file
     * @param a       the a
     */
    public void processorSingleLog(File logFile,int a){
        //logContent 数组存放读取到的文件，文件是按照"<"分割的；
        String[] logContent = readSingleLog(logFile,1);

        //用来记录有所行数，，因为各个 P 指令块是分开在 logContent 数组里面的；
        int lineNumber = 0;

        String bscName = "";
        String RLDEPsector = "";
        String lac = "";
        String ci = "";
        String bsic = "";
        String bcch = "";
        String band = "";
        String RLCFPsector = "";
        String[][] ch_group = new String[4][14];    //保存四个信道，每个信道有14个元素（ch_group、hsn、dchno1-12）

        for (int x=0;x<4;x++){
            //给 ch_group 二维数组赋初值，因为取不到参数的时候，需要标记为 N/A；
            for (int y=0;y<14;y++){
                ch_group[x][y]="N/A";
            }
        }

//        System.out.println("logContent"+logContent.length);
        for (int i=0;i<logContent.length;i++){
            //将各个 P 指令的内容按行分割开来处理；
            String[] ss = logContent[i].split("\\r|\\n");

            //打印各个 P 指令的内容的行数，也既是各个 P 指令的结尾行数；
//            System.out.println("ss"+ss.length);

            for (int j=0;j<ss.length;j++){  //遍历所有 P 指令块分割后的行，j 为行号；
                //记行数；
                lineNumber++;

                String lineContent = ss[j];

                //lineNumber定位处理到总文件的哪一行了，而 j 是 ss 元素的行数；
//                System.out.println("正在处理第："+lineNumber+"行");
//                System.out.println(lineContent);

                if (lineContent.contains("Connected")){
                    //开始位置，并读取 bscName;
                    bscName = lineContent.substring(16,22);
//                    System.out.println("开始处理 "+bscName+"\r\n"+ss[j]);
                }

                if (lineContent.contains("RLDEP")&&!lineContent.contains("EXT")){
                    // 读取小区基础信息：sector、lac、ci、bsic、bcch、band
//                    System.out.println(lineContent);

                    while (j<ss.length){
                        if (ss[j]!=null && ss[j].contains("CGI")){
                            RLDEPsector = ss[j+1].substring(0,7);
                            lac = ss[j+1].substring(16,20);
                            ci = ss[j+1].substring(21,26).trim();
                            bsic = ss[j+1].substring(30,32);
                            bcch = ss[j+1].substring(36,43).trim();
                            band = ss[j+4].substring(52,ss[j+4].length());
//                            System.out.println(RLDEPsector+"-"+lac+"-"+ci+"-"+bsic+"-"+bcch+"-"+band);
                        }
                        j++;
                    }
                }

                if (lineContent.contains("RLCFP")){
                    // 读取信道信息
                    while (j<ss.length){
//                        System.out.println("j "+j);
                        if (ss[j]!=null && ss[j].contains("CHGR")){
                            if (ss[j].contains("FAULT")){
                                System.out.println("cdd-log 存在故障码："+"\r\n"+ss[j]);
                            }
                            RLCFPsector = ss[j-2].substring(0,7);
//                            System.out.println("j1"+j);

                            String cellEndLineNumber = ss[j].substring(0,5).concat("CELL");
                            int switchNumber = 0;
                            if (ss[j].substring(0,5).trim().length()>=1) {
//                                            System.out.println(ss[j-2]);
                                System.out.println(ss[j]+" "+j);
                            }
                            switch (cellEndLineNumber){
                                case "CELL": break;
                                default:
                                    for (int l=0; l<4;l++) {

                                        ch_group[l][0] = ss[j+1].substring(0,5).trim();    //CHGR
                                        ch_group[l][1] = ss[j+1].substring(49,53).trim();    //hsn
                                        ch_group[l][2] = ss[j+1].substring(60,ss[j+1].length()).trim();    //dchno1
//                                        ch_group[l][3] = ss[j+2].substring(60,ss[j+1].length()).trim();    //dchno2
//                                        ch_group[l][4] = ss[j+3].substring(60,ss[j+1].length()).trim();
//                                        ch_group[l][5] = ss[j+4].substring(60,ss[j+1].length()).trim();
//                                        ch_group[l][6] = ss[j+5].substring(60,ss[j+1].length()).trim();
//                                        ch_group[l][7] = ss[j+6].substring(60,ss[j+1].length()).trim();
//                                        ch_group[l][8] = ss[j+7].substring(60,ss[j+1].length()).trim();
//                                        ch_group[l][9] = ss[j+8].substring(60,ss[j+1].length()).trim();
//                                        ch_group[l][10] = ss[j+9].substring(60,ss[j+1].length()).trim();
//                                        ch_group[l][11] = ss[j+10].substring(60,ss[j+1].length()).trim();
//                                        ch_group[l][12] = ss[j+11].substring(60,ss[j+1].length()).trim();
//                                        ch_group[l][13] = ss[j+12].substring(60,ss[j+1].length()).trim();
//                                        System.out.println("this is"+ss[j+2].substring(0,5).trim().length());
                                    }
//                                    for (int m=0;m<14;m++) {
//                                        System.out.print(ch_group[1][m]+" ");
//                                    }
//                                    System.out.println();
                                    switchNumber++;
                            }
//                            System.out.println(RLCFPsector);
                            j += switchNumber;

                        }
                        j++;
                    }
                }

                if (lineContent.contains("Disconnected")){
//                    System.out.println("处理最后一行： "+lineNumber+"\r\n"+ss[j]);
                }
            }
        }

    }


    /**
     * 读取文件夹
     *
     * @param folderPath the folder path
     * @return string string
     */
    public String textFolderToString(String folderPath) {
        File[] files = new File(folderPath).listFiles();
        String connect ="";
        if (files.length>0){
            for(File file:files){
                System.out.println(file.getName());
                connect += readSingleLog(file);
            }
//            for (int i=0;i<files.length;i++){
//                System.out.println(files[i].getName());
//                connect += textToString(files[i]);
//            }
        }else {
            System.out.println("未找到文件");
        }
        return connect;
    }

}
