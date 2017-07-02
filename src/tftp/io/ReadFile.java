package tftp.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by 4P on 2017/7/1.
 */
public class ReadFile {
    public static final int MAX_SIZE = 512;
    private String filename;
    File file;
    FileInputStream fis;
    int offset = 0;
    int ans;
    long filelength;
    public ReadFile(String filename) throws FileNotFoundException{
        this.filename = filename;
        offset = 0;
        file = new File(filename);
        filelength = file.length();
        fis = new FileInputStream(file);
    }
    public int Next(byte[] b){
//        byte[] ret = new byte[MAX_SIZE];
        ans = 0;
        try {
            ans = fis.read(b,0,MAX_SIZE);
            offset+=ans;
            return ans;
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(ans);
        return -1;
    }
    public int hasNext(){
        long det = filelength-offset;
        if(det==MAX_SIZE) return 0;
        else if(det<MAX_SIZE) return 1;
        return 2;
//        return filelength-offset;
//        if(offset==filelength && ans!=MAX_SIZE)return 0;


//        return true;
    }
    public static void main(String[] args){

    }
}
