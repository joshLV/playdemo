package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUploadUtil {

	/**
	 * 上传图片
	 * 
	 * @param Image
	 * @param desc
	 */
	public static void storeImage(File file,String storePath) {
		//取得文件名
		String filename=file.getName();
		FileInputStream in;
		try {
			in = new FileInputStream(file);
			// 将该数据流写入到指定文件中
			FileOutputStream out;

			out = new FileOutputStream(storePath+filename);
			byte[] buffer = new byte[1024]; // To hold file contents
			int bytes_read;
			while ((bytes_read = in.read(buffer)) != -1){
				out.write(buffer, 0, bytes_read);
			}
            in.close();
            out.close();
        } catch (IOException e1) {
			e1.printStackTrace();
		} 
	}
}
