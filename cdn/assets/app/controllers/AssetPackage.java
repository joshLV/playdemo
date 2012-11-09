package controllers;

import ext.AssetTag;
import play.Play;
import play.mvc.Controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AssetPackage extends Controller {

    private static final String JS_ROOT = "/public/javascripts";

    private static final String CSS_ROOT = "/public/stylesheets";
    
    private static String GENERATE_DIR = Play.configuration.getProperty("asset.generate.dir") + "/assets_" + AssetTag.PROD_VERSION;
    
    public static void js(String path) {
        String file_ext = "js";
        String contentType = "application/javascript";
        
//        System.out.println(path);
        
        mergeRequestPathFile(path, JS_ROOT, file_ext, contentType);
    }


    public static void css(String path) {
        String file_ext = "css";
        String contentType = "text/css";
        
//        System.out.println(path);
        
        mergeRequestPathFile(path, CSS_ROOT, file_ext, contentType);
    }    
    
    private static void mergeRequestPathFile(String path, String fileRoot, String file_type,
            String contentType) {
        String requestPath = "/" + path;
        String file_ext = "." + file_type;
        String[] assetPath = requestPath.split(file_ext);
        
        String pathName = path.replace('/', '_');
        String targetName = path.substring(path.lastIndexOf('/') + 1);
//        System.out.println("targetName=" + targetName);
        File targetFile= null;
        
        if (assetPath.length > 1) {    
            targetFile= new File(GENERATE_DIR + "/" + file_type + "/" + pathName, targetName);
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            if(!targetFile.exists()) { 
                try {
                    BufferedWriter targetWrite = new BufferedWriter(new FileWriter(targetFile));
                    
                    for (String assetName : assetPath) {
                        File sourceFile = new File(Play.applicationPath, fileRoot + assetName + file_ext);
//                        System.out.println("js=" + sourceFile.getPath());
                        if (sourceFile.exists()) {
                            BufferedReader breader = new BufferedReader(new FileReader(sourceFile));
                            int c;
                            while ((c = breader.read()) != -1) {
                                targetWrite.write(c);
                            }
                            targetWrite.write('\n');
                        }
                    }
                    targetWrite.flush();
                    targetWrite.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            targetFile = new File(Play.applicationPath, fileRoot + assetPath[0] + file_ext);
        }
        
        try {
            renderBinary(new FileInputStream(targetFile), targetName, contentType, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            error(404, "指定文件不存在");
        }
    }


}