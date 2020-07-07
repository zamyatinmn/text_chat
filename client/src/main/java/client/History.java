package client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class History {
    private String filePath;
    private String login;
    private RandomAccessFile raf;
    private FileOutputStream toFile;
    public static boolean exists = false;

    public History(String login) {
        exists = true;
        this.login = login;
        filePath = "C:/Java/javapro_180620/client/src/main/resources/history_" + login + ".txt";
    }

    public void createFile(){
        File history = new File(filePath);
        if (!history.exists()){
            try {
                history.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readFile(){
        try {
            raf = new RandomAccessFile(filePath, "r");
            String line = raf.readLine();
            ArrayList<String> his = new ArrayList<>();
            while (line != null){
                String utf8 = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                his.add(utf8);
                line = raf.readLine();
            }
            int LAST_LINES = 100;
            StringBuilder sb = new StringBuilder();
            if (his.size() < LAST_LINES){
                for (String s: his) {
                    sb.append(s).append(System.lineSeparator());
                }
            }else {
                for (int i = his.size() - LAST_LINES; i < his.size(); i++) {
                    sb.append(his.get(i)).append(System.lineSeparator());
                }
            }
            return sb.toString();
        } catch (IOException e){
            return "";
        }
    }

    public void writeToFile(String str){
        try {
            toFile = new FileOutputStream(filePath, true);
            toFile.write(str.getBytes());
            toFile.write("\n".getBytes());
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeFile(){
        try {
            raf.close();
            toFile.close();
        } catch (IOException | NullPointerException e) {
        }
    }
}
