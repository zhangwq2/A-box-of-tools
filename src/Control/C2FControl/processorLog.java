package Control.C2FControl;

import sun.security.util.Length;

import java.io.*;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * 用于处理 cdd 文本文件，得到 cdd 参数
 * Created by 跃峰 on 2016/1/27.
 */
public class ProcessorLog {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        long startTime=System.currentTimeMillis();
        File f = new File("D:\\SZ\\变频工作\\数据采集\\CDD\\20160122\\SZ01A.Log");
//        File f = new File("/Users/huangyuefeng/Downloads/CDD/SZ01A.log");
        ProcessorLog rc = new ProcessorLog();
        rc.ProcessorLog(f);
//        String[] ss = rc.readSingleLog(f,1);
//        rc.processorSingleLog(f);
        long endTime=System.currentTimeMillis();
        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
    }

    /**
     * Processor single log.
     *
     * @param logFile the log file
     */
    public HashMap ProcessorLog(File logFile) {
        HashMap<String,String[][]> contentHM = new HashMap<String, String[][]>();

//    public void processorSingleLog(File logFile){


        //logContent 数组存放读取到的文件，文件按照"<"分割的；一个 logContent 就是一个 OSS 指令块;
        String[] logContent = ReadFile.readSingleText(logFile).split("<");

        //用来记录有所行数，，因为各个 P 指令块是分开在 logContent 数组里面的；
        int lineNumber = 0;


//        System.out.println("logContent"+logContent.length);

        String bscName = "";
        String[][] bsc = new String[1][1];
        String[][] sectors = new String[1][6];
//            String RLDEPsector = "";
//            String lac = "";
//            String ci = "";
//            String bsic = "";
//            String bcch = "";
//            String band = "";

        String RLCFPsector = "";
        String[][] ch_group = new String[4][14];    //保存4个信道组，每个信道组可能有14 or 66个元素（ch_group、hsn、dchno1-12?64）

        // 遍历所有 OSS 指令块，一个一个处理；
        for (int i=0;i<logContent.length;i++){


            //将各个 OSS 指令块的内容按行分割开来处理；
            String[] ss = logContent[i].split("\\r|\\n");

            //打印各个 P 指令的内容的行数，也既是各个 P 指令的结尾行数；
//            System.out.println("ss"+ss.length);

            for (int j=0;j<ss.length;j++){  //遍历 this OSS 指令块分割后的行，j 为行号；
                //记行数；
                lineNumber++;
//                System.out.println(lineNumber);

                String lineContent = ss[j];

                //lineNumber定位处理到总文件的哪一行了，而 j 是 ss 元素的行数；
//                System.out.println("正在处理第："+lineNumber+"行");
//                System.out.println(lineContent);



                // 判断指令块是否为 Connected 块；
                if (lineContent.contains("Connected")){
                    //开始位置，并读取 bscName;
                    bscName = lineContent.substring(16,22);
                    bsc[0][0] = bscName;
                    contentHM.put("bsc",bsc);
//                    System.out.println("开始处理 "+ bscName +"\r\n"+ss[j]);
                    System.out.println("开始处理 "+ bsc[0][0] +"\r\n"+ss[j]);
                }

                // 判断指令块是否为 RLDEP 块；
                if (lineContent.contains("RLDEP")&&!lineContent.contains("EXT")){
                    // 读取小区基础信息：sector、lac、ci、bsic、bcch、band
//                    System.out.println(lineContent);
                    String head = "MSC\tBSC\tVendor\tSite\tLatitude\tLongitude\tSector\tID\tMaster\tLAC\tCI\tKeywords\tAzimuth\tBCCH frequency\tBSIC\tIntracell HO\tSynchronization group\tAMR HR Allocation\tAMR HR Threshold\tHR Allocation\tHR Threshold\tTCH allocation priority\tGPRS allocation priority\tRemote\tMCC\tMNC";

                    while (j<ss.length){
                        if (ss[j]!=null && ss[j].contains("CGI")){
                            sectors[0][0] = ss[j+1].substring(0,7);
                            sectors[0][1] = ss[j+1].substring(16,20);
                            sectors[0][2] = ss[j+1].substring(21,26).trim();
                            sectors[0][3] = ss[j+1].substring(30,32);
                            sectors[0][4] = ss[j+1].substring(36,43).trim();
                            sectors[0][5] = ss[j+4].substring(52,ss[j+4].length());
//                            System.out.println(sectors[0]+"-"+sectors[1]+"-"+sectors[2]+"-"+sectors[3]+"-"+sectors[4]+"-"+sectors[5]);
                            String line = "MSC\t"+bscName+"\tEricsson\t"+sectors[0][0].substring(0,6)+"\t22.68933\t113.77698\t"+sectors[0][0]+"\t"+sectors[0][1]+"\t"+sectors[0][2]+"\tNew\t50\t"+sectors[0][4]+"\t"+sectors[0][3]+"\tTRUE\tRXOTG\tTRUE\t20\tTRUE\t10\tRandom\tNo Preference";
                            contentHM.put("rldep",sectors);
                        }
//                        for (int k=0;k<lineContent.length;k++) {
//                            System.out.println(sectors[k]);
//                        }
                        j++;

                    }
//                    System.out.println("小区数： "+(ss.length-3)/9);

                }

                // 判断指令块是否为 RLCFP 块；
                if (lineContent.contains("RLCFP")){
                    // 读取信道信息
                    // RLCFP_CELL 按“CELL”分列后存入数组 RLCFP_CELL[] 中，每个 RLCFP_CELL 就是一个小区的数据；
                    String[] RLCFP_CELL = logContent[i].split("CELL");
//                    System.out.println(RLCFP_CELL[RLCFP_CELL.length-1]);

                    String head = "Sector\tChannel Group\tSubcell\tBand\tExtended\tHopping method\tContains BCCH\tHSN\tDTX\tPower control\tSubcell Signal Threshold\tSubcell Tx Power\t# TRXs\t# SDCCH TSs\t# Fixed GPRS TSs\tPriority\tTCH 1\tTCH 2\tTCH 3\tTCH 4\tTCH 5\tTCH 6\tTCH 7\tTCH 8\tTCH 9\tTCH 10\tTCH 11\tTCH 12\tTCH 13\tTCH 14\tTCH 15\tTCH 16\tTCH 17\tTCH 18\tTCH 19\tTCH 20\tTCH 21\tTCH 22\tTCH 23\tTCH 24\tTCH 25\tTCH 26\tTCH 27\tTCH 28\tTCH 29\tTCH 30\tTCH 31\tTCH 32\tTCH 33\tTCH 34\tTCH 35\tTCH 36\tTCH 37\tTCH 38\tTCH 39\tTCH 40\tTCH 41\tTCH 42\tTCH 43\tTCH 44\tTCH 45\tTCH 46\tTCH 47\tTCH 48\tTCH 49\tTCH 50\tTCH 51\tTCH 52\tTCH 53\tTCH 54\tTCH 55\tTCH 56\tTCH 57\tTCH 58\tTCH 59\tTCH 60\tTCH 61\tTCH 62\tTCH 63\tTCH 64\tMAIO 1\tMAIO 2\tMAIO 3\tMAIO 4\tMAIO 5\tMAIO 6\tMAIO 7\tMAIO 8\tMAIO 9\tMAIO 10\tMAIO 11\tMAIO 12\tMAIO 13\tMAIO 14\tMAIO 15\tMAIO 16";
//                    String line = "01DA011\t0\tUL\t1800\tN/A\tNon hopping\tTRUE\t3\tDownlink and Uplink\tDownlink and Uplink\tN/A\t47\t4\t1\t1\tNormal\t567\t575\t627\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A";
//                    System.out.println(head);

                    // 遍历各个小区的数据，一个 k 就是一个小区块；
                    for (int k=3;k<RLCFP_CELL.length;k++) {
                        for (int x=0;x<ch_group.length;x++){
                            //给 ch_group 二维数组赋初值，因为取不到参数的时候，需要标记为 N/A；
                            for (int y=0;y<ch_group[x].length;y++){
                                ch_group[x][y]="N/A";
                            }
                        }

                        // 再将各个小区的 RLCFP_CELL 按行分割后存入数组 RLCFP_CELL_LINE[]；
                        String[] RLCFP_CELL_LINE = RLCFP_CELL[k].split("\\r|\\n");
                        // 各个小区的信道组号统计
                        int CHGR_COUNT = 0;
                        // 各个小区的信道组号所在行数
                        int[] CHGR_LINE = new int[4];

                        // 循环各个 RLCFP_CELL_LINE[]；
                        for (int l=0;l<RLCFP_CELL_LINE.length;l++){

                            // 取得  RLCFP_CELL_LINE 表头所在行，其实如果没有错误的话，一般都是第三行；
                            if (RLCFP_CELL_LINE[l]!=null && RLCFP_CELL_LINE[l].contains("SDCCHAC")){
                                RLCFPsector = RLCFP_CELL_LINE[l-2].substring(0,7);
//                                System.out.println(RLCFPsector);

                                //开始从 RLCFP_CELL_LINE 表头所在行 以后 的行，循环取得其他参数；
                                for (int m=l+1;m<RLCFP_CELL_LINE.length;m++){
//                                    if (RLCFP_CELL_LINE[m].length()==1){
//                                        System.out.println("空行： "+RLCFP_CELL_LINE[m]);
//                                    }
//                                    System.out.println("第一行："+RLCFP_CELL_LINE[0]);
//                                    System.out.println("第三行："+RLCFP_CELL_LINE[3]);
//                                    System.out.println("最后行："+RLCFP_CELL_LINE[RLCFP_CELL_LINE.length-1]);

                                    // 取得“信道组号”所在行，只能取三位出来对比，因为整个 RLCFP_CELL 指令最后有个三位的“END”标记；
                                    if (RLCFP_CELL_LINE[m]!=null && RLCFP_CELL_LINE[m].substring(0,3).trim().length()==1) {
//                                        System.out.println("CHGR_COUNT: "+CHGR_COUNT);
//                                        System.out.println("CHGR: "+RLCFP_CELL_LINE[m]);
                                        CHGR_COUNT++;
                                        if (CHGR_COUNT==1){
                                            //找到第一个信道组所在行
                                            CHGR_LINE[0] = m;
                                        }
                                        if (CHGR_COUNT==2) {
                                            //找到第二个信道组所在行
                                            CHGR_LINE[1] = m;
                                        }
                                        if (CHGR_COUNT==3) {
                                            //找到第三个信道组所在行
                                            CHGR_LINE[2] = m;
                                        }
                                        if (CHGR_COUNT==4) {
                                            //找到第四个信道组所在行
                                            CHGR_LINE[3] = m;
                                        }
                                    }
                                }
//                                System.out.println("CHGR_COUNT: "+CHGR_COUNT);
//                                System.out.println("CHGR_LINE: "+CHGR_LINE[CHGR_COUNT-1]);
//                                for (int x=0;x<4;x++){
//                                    for (int y=0;y<14;y++){
//                                        System.out.println(ch_group[x][y]);
//                                    }
//                                }
                            }
                        }

                        // 循环各个 RLCFP_CELL_LINE[]；
                        for (int l=0;l<RLCFP_CELL_LINE.length;l++) {
                            // 取得  RLCFP_CELL_LINE 表头所在行，其实如果没有错误的话，一般都是第三行；
                            if (RLCFP_CELL_LINE[l]!=null && RLCFP_CELL_LINE[l].contains("SDCCHAC")) {
                                //开始从 RLCFP_CELL_LINE 表头所在行 以后 的行，循环取得其他参数；
                                for (int m = l + 1; m < RLCFP_CELL_LINE.length; m++) {
                                    if (RLCFP_CELL_LINE[m] != null && RLCFP_CELL_LINE[m].substring(0, 3).trim() != "END") {
                                        if (CHGR_COUNT == 1) {
                                            //取出第一个信道组数据
                                            ch_group[0][0] = RLCFP_CELL_LINE[CHGR_LINE[0]].substring(0, 3).trim();    //CHGR
                                            ch_group[0][1] = RLCFP_CELL_LINE[CHGR_LINE[0]].substring(49, 53).trim();    //hsn
//
                                            for (int n = 0; n < RLCFP_CELL_LINE.length - CHGR_LINE[0]; n++) {
                                                if (RLCFP_CELL_LINE[CHGR_LINE[0] + n].length()>59) {
                                                    ch_group[0][2 + n] = RLCFP_CELL_LINE[CHGR_LINE[0] + n].substring(60).trim();    //dchno1-dchno64
                                                }
                                            }
                                            contentHM.put("rlcfp",ch_group);
//                                            System.out.println("信道组数"+CHGR_COUNT);
//                                            String line = RLCFPsector+"\t0\tUL\t1800\tN/A\tNon hopping\tTRUE\t3\tDownlink and Uplink\tDownlink and Uplink\tN/A\t47\t4\t1\t1\tNormal\t567\t575\t627\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A\tN/A";
//                                            System.out.println(line);

                                        } else if (CHGR_COUNT == 2) {
                                            //取出第一个信道组数据
                                            ch_group[0][0] = RLCFP_CELL_LINE[CHGR_LINE[0]].substring(0, 3).trim();    //CHGR
                                            ch_group[0][1] = RLCFP_CELL_LINE[CHGR_LINE[0]].substring(49, 53).trim();    //hsn

                                            for (int n = 0; n < CHGR_LINE[1] - CHGR_LINE[0]; n++) {
                                                if (RLCFP_CELL_LINE[CHGR_LINE[0] + n].length()>59) {
                                                    ch_group[0][2 + n] = RLCFP_CELL_LINE[CHGR_LINE[0] + n].substring(60).trim();    //dchno1-dchno64
                                                }
                                            }

                                            //取出第二个信道组数据
                                            ch_group[1][0] = RLCFP_CELL_LINE[CHGR_LINE[1]].substring(0, 3).trim();    //CHGR
                                            ch_group[1][1] = RLCFP_CELL_LINE[CHGR_LINE[1]].substring(49, 53).trim();    //hsn

                                            for (int n = 0; n < RLCFP_CELL_LINE.length - CHGR_LINE[1]; n++) {
                                                if (RLCFP_CELL_LINE[CHGR_LINE[1] + n].length()>59) {
                                                    ch_group[1][2 + n] = RLCFP_CELL_LINE[CHGR_LINE[1] + n].substring(60).trim();    //dchno1-dchno64
                                                }
                                            }
                                            contentHM.put("rlcfp",ch_group);

//                                            System.out.println("有二个信道组");
                                        } else if (CHGR_COUNT == 3) {
                                            //取出第一个信道组数据
                                            ch_group[0][0] = RLCFP_CELL_LINE[CHGR_LINE[0]].substring(0, 3).trim();    //CHGR
                                            ch_group[0][1] = RLCFP_CELL_LINE[CHGR_LINE[0]].substring(49, 53).trim();    //hsn

                                            for (int n = 0; n < CHGR_LINE[1] - CHGR_LINE[0]; n++) {
                                                if (RLCFP_CELL_LINE[CHGR_LINE[0] + n].length()>59) {
                                                    ch_group[0][2 + n] = RLCFP_CELL_LINE[CHGR_LINE[0] + n].substring(60).trim();    //dchno1-dchno64
                                                }
                                            }


                                            //取出第二个信道组数据
                                            ch_group[1][0] = RLCFP_CELL_LINE[CHGR_LINE[1]].substring(0, 3).trim();    //CHGR
                                            ch_group[1][1] = RLCFP_CELL_LINE[CHGR_LINE[1]].substring(49, 53).trim();    //hsn

                                            for (int n = 0; n < CHGR_LINE[2] - CHGR_LINE[1]; n++) {
                                                if (RLCFP_CELL_LINE[CHGR_LINE[1] + n].length()>59) {
                                                    ch_group[1][2 + n] = RLCFP_CELL_LINE[CHGR_LINE[1] + n].substring(60).trim();    //dchno1-dchno64
                                                }
                                            }


                                            //取出第三个信道组数据
                                            ch_group[2][0] = RLCFP_CELL_LINE[CHGR_LINE[2]].substring(0, 3).trim();    //CHGR
                                            ch_group[2][1] = RLCFP_CELL_LINE[CHGR_LINE[2]].substring(49, 53).trim();    //hsn

                                            for (int n = 0; n < RLCFP_CELL_LINE.length - CHGR_LINE[2]; n++) {
                                                if (RLCFP_CELL_LINE[CHGR_LINE[2] + n].length()>59) {
                                                    ch_group[2][2 + n] = RLCFP_CELL_LINE[CHGR_LINE[2] + n].substring(60).trim();    //dchno1-dchno64
                                                }
                                            }
                                            contentHM.put("rlcfp",ch_group);

//                                            System.out.println("有三个信道组");
                                        } else if (CHGR_COUNT == 4) {
                                            //取出第一个信道组数据
                                            ch_group[0][0] = RLCFP_CELL_LINE[CHGR_LINE[0]].substring(0, 3).trim();    //CHGR
                                            ch_group[0][1] = RLCFP_CELL_LINE[CHGR_LINE[0]].substring(49, 53).trim();    //hsn

                                            for (int n = 0; n < CHGR_LINE[1] - CHGR_LINE[0]; n++) {
                                                if (RLCFP_CELL_LINE[CHGR_LINE[0] + n].length()>59) {
                                                    ch_group[0][2 + n] = RLCFP_CELL_LINE[CHGR_LINE[0] + n].substring(60).trim();    //dchno1-dchno64
                                                }
                                            }

                                            //取出第二个信道组数据
                                            ch_group[1][0] = RLCFP_CELL_LINE[CHGR_LINE[1]].substring(0, 3).trim();    //CHGR
                                            ch_group[1][1] = RLCFP_CELL_LINE[CHGR_LINE[1]].substring(49, 53).trim();    //hsn

                                            for (int n = 0; n < CHGR_LINE[2] - CHGR_LINE[1]; n++) {
                                                if (RLCFP_CELL_LINE[CHGR_LINE[1] + n].length()>59) {
                                                    ch_group[1][2 + n] = RLCFP_CELL_LINE[CHGR_LINE[1] + n].substring(60).trim();    //dchno1-dchno64
                                                }
                                            }

                                            //取出第三个信道组数据
                                            ch_group[2][0] = RLCFP_CELL_LINE[CHGR_LINE[2]].substring(0, 3).trim();    //CHGR
                                            ch_group[2][1] = RLCFP_CELL_LINE[CHGR_LINE[2]].substring(49, 53).trim();    //hsn

                                            for (int n = 0; n < CHGR_LINE[3] - CHGR_LINE[2]; n++) {
                                                if (RLCFP_CELL_LINE[CHGR_LINE[2] + n].length()>59) {
                                                    ch_group[2][2 + n] = RLCFP_CELL_LINE[CHGR_LINE[2] + n].substring(60).trim();    //dchno1-dchno64
                                                }
                                            }

                                            //取出第四个信道组数据
                                            ch_group[3][0] = RLCFP_CELL_LINE[CHGR_LINE[3]].substring(0, 3).trim();    //CHGR
                                            ch_group[3][1] = RLCFP_CELL_LINE[CHGR_LINE[3]].substring(49, 53).trim();    //hsn

                                            for (int n = 0; n < RLCFP_CELL_LINE.length - CHGR_LINE[3]; n++) {
                                                if (RLCFP_CELL_LINE[CHGR_LINE[3] + n].length()>59) {
                                                    ch_group[3][2 + n] = RLCFP_CELL_LINE[CHGR_LINE[3] + n].substring(60).trim();    //dchno1-dchno64
                                                }
                                            }
                                            contentHM.put("rlcfp",ch_group);
//                                            System.out.println("有四个信道组");
                                        }
                                    }

                                }

                            }

                        }

//                        for (int x=0;x<4;x++){
//                            for (int y=0;y<ch_group[x].length;y++){
//                                System.out.print(ch_group[x][y]+" ");;
//                            }
//                            System.out.println();
//                        }
//
//                        System.out.println("处理完： " + RLCFPsector);
                    }
                }

                Iterator it = contentHM.keySet().iterator();
                while(it.hasNext()) {
                    String key = (String)it.next();
                    System.out.println("key:" + key);
                    System.out.println("value:" + contentHM.get(key));
                }


                // 判断指令块是否为 RLNRP 块；
                if (lineContent.contains("RLNRP")){
                    String[] rlnrp = new String[33];
                    // RLNRP_CELL 按“CELL”分列后存入数组 RLNRP_CELL[] 中，每个 RLNRP_CELL 就是一个小区的数据；
                    String[] RLNRP_CELL = logContent[i].split("CELL\n");

//                    System.out.println("------------1----------");
//                    System.out.println(RLNRP_CELL[206]);
//                    System.out.println("------------2----------");

                    for (int k=0;k<RLNRP_CELL.length;k++){
                        for (int l=1;l<rlnrp.length;l++){
                            rlnrp[l] = "N/A";
                        }

                        String[] RLNRP_line = RLNRP_CELL[k].split("\\r|\\n");

                        int temp = 0;
                        if (!RLNRP_line[0].contains("RLNRP")) {
                            rlnrp[0] = RLNRP_line[0].substring(0,7);

                            for (int l=1;l<(RLNRP_line.length-4)/12;l++){
                                rlnrp[l] = RLNRP_line[3 + temp].substring(0,7);
                                temp += 12;
                            }
                        }

                        // 测试，遍历数组 rlnrp[]
//                        for (int l=0;l<rlnrp.length;l++){
//                            System.out.print(rlnrp[l]+" ");
//                        }
//                        System.out.println();

                    }
                }

                // 判断指令块是否为 结束块；
                if (lineContent.contains("Disconnected")){
//                    System.out.println("处理最后一行： "+lineNumber+"\r\n"+ss[j]);
                }
            }
        }

//    }
    return contentHM;
    }

    public void createForteFile(){

    }
}
