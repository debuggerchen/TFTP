package tftp.io;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by 4P on 2017/7/1.
 */
public class WriteFile {
    private String filename;
    private File file;
    private FileOutputStream fos;

    public WriteFile(String filename) {
        this.filename = filename;
        file = new File(filename);
        if(!file.exists()){
//            file.mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            //throw new FileExisted();
        }
        try {
            fos = new FileOutputStream(file,false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] data, int length) throws IOException {
//            fos.write(data);
            fos.write(data,0,length);

    }

    public void delete(){
        file.delete();
    }

    public void close(){
        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
