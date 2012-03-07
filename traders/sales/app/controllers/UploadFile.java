package controllers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import play.Play;
import play.mvc.Controller;
import util.FileUploadUtil;

public class UploadFile  extends Controller {

	/**
	 * 上传文件
	 *  
	 * @param imgFile
	 */
	public static void uploadJson(File imgFile) {
		//文件保存目录路径
		String savePath =  Play.configuration.getProperty("upload.imagepath", "false");
		//定义允许上传的文件扩展名
		String[] fileTypes = (Play.configuration.getProperty("newsImg.fileTypes", "false")).trim().toString().split(",");
		//最大文件大小
		long maxSize = 1000000;
		if (imgFile != null) {
			//检查目录
			File uploadDir = new File(savePath);
			if(!uploadDir.isDirectory()){
				getError("上传目录不存在。");
				return;
			}

			//检查目录写权限
			if(!uploadDir.canWrite()){
				getError("上传目录没有写权限。");
				return;
			}

			//创建文件夹
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String ymd = sdf.format(new Date());
			savePath += ymd + "/";
			File dirFile = new File(savePath);

			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}

			//检查文件大小
			if(imgFile.length() > maxSize){
				getError("上传文件大小超过限制。");
				return;
			}

			//检查扩展名
			String fileExt = imgFile.getName().substring(imgFile.getName().lastIndexOf(".") + 1).toLowerCase();
			System.out.println("fileExt"+fileExt);
			if(!Arrays.<String>asList(fileTypes).contains(fileExt)){
				getError("上传文件扩展名是不允许的扩展名。");
				return;
			}
			String newname=ymd+"."+fileExt;
			//上传文件
			new FileUploadUtil().storeImage(imgFile, savePath, newname);
			try {
				Map map = new HashMap();
				map.put("error", 0);
				map.put("url",  savePath + newname);

				renderJSON(map );
			} catch (Exception e) {
				getError("上传失败");
				return;
			}

		}else{
			getError("请选择文件。");
			return;
		}
	}

	public static void fileManagerJson() {
		//文件保存目录路径
		String rootPath =  Play.configuration.getProperty("upload.imagepath", "false");
		//定义允许上传的文件扩展名
		String[] fileTypes = (Play.configuration.getProperty("newsImg.fileTypes", "false")).trim().toString().split(",");

		//根据path参数，设置各路径和URL
		String path = request.params.get("path") != null ? request.params.get("path") : "";
		String currentPath = rootPath + path;
		String currentDirPath = path;
		String moveupDirPath = "";
		if (!"".equals(path)) {
			String str = currentDirPath.substring(0, currentDirPath.length() - 1);
			moveupDirPath = str.lastIndexOf("/") >= 0 ? str.substring(0, str.lastIndexOf("/") + 1) : "";
		}

		//不允许使用..移动到上一级目录
		if (path.indexOf("..") >= 0) {
			getError("Access is not allowed.");
			return;
		}
		//最后一个字符不是/
		if (!"".equals(path) && !path.endsWith("/")) {
			getError("Parameter is not valid.");
			return;
		}
		//目录不存在或不是目录
		File currentPathFile = new File(currentPath);
		if(!currentPathFile.isDirectory()){
			getError("Directory does not exist.");
			return;
		}

		//遍历目录取的文件信息
		List<Hashtable> fileList = new ArrayList<Hashtable>();
		if(currentPathFile.listFiles() != null) {
			for (File file : currentPathFile.listFiles()) {
				Hashtable<String, Object> hash = new Hashtable<String, Object>();
				String fileName = file.getName();
				if(file.isDirectory()) {
					hash.put("is_dir", true);
					hash.put("has_file", (file.listFiles() != null));
					hash.put("filesize", 0L);
					hash.put("is_photo", false);
					hash.put("filetype", "");
				} else if(file.isFile()){
					String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
					hash.put("is_dir", false);
					hash.put("has_file", false);
					hash.put("filesize", file.length());
					hash.put("is_photo", Arrays.<String>asList(fileTypes).contains(fileExt));
					hash.put("filetype", fileExt);
				}
				hash.put("filename", fileName);
				hash.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file.lastModified()));
				fileList.add(hash);
			}
		}

		Map map = new HashMap();
		map.put("moveup_dir_path", moveupDirPath);
		map.put("current_dir_path", currentDirPath);

		renderJSON(map );
	}

	public class NameComparator implements Comparator {
		public int compare(Object a, Object b) {
			Hashtable hashA = (Hashtable)a;
			Hashtable hashB = (Hashtable)b;
			if (((Boolean)hashA.get("is_dir")) && !((Boolean)hashB.get("is_dir"))) {
				return -1;
			} else if (!((Boolean)hashA.get("is_dir")) && ((Boolean)hashB.get("is_dir"))) {
				return 1;
			} else {
				return ((String)hashA.get("filename")).compareTo((String)hashB.get("filename"));
			}
		}
	}
	public class SizeComparator implements Comparator {
		public int compare(Object a, Object b) {
			Hashtable hashA = (Hashtable)a;
			Hashtable hashB = (Hashtable)b;
			if (((Boolean)hashA.get("is_dir")) && !((Boolean)hashB.get("is_dir"))) {
				return -1;
			} else if (!((Boolean)hashA.get("is_dir")) && ((Boolean)hashB.get("is_dir"))) {
				return 1;
			} else {
				if (((Long)hashA.get("filesize")) > ((Long)hashB.get("filesize"))) {
					return 1;
				} else if (((Long)hashA.get("filesize")) < ((Long)hashB.get("filesize"))) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	}
	public class TypeComparator implements Comparator {
		public int compare(Object a, Object b) {
			Hashtable hashA = (Hashtable)a;
			Hashtable hashB = (Hashtable)b;
			if (((Boolean)hashA.get("is_dir")) && !((Boolean)hashB.get("is_dir"))) {
				return -1;
			} else if (!((Boolean)hashA.get("is_dir")) && ((Boolean)hashB.get("is_dir"))) {
				return 1;
			} else {
				return ((String)hashA.get("filetype")).compareTo((String)hashB.get("filetype"));
			}
		}
	}

	private static void getError(String message) {
		Map map = new HashMap();
		map.put("error", 1);
		map.put("message", message);
		renderJSON(map );
	}
}
