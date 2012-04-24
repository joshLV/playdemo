package controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import play.Play;
import play.mvc.Controller;

public class AssetPackage extends Controller {

    private static final String JS_ROOT = "/public/javascripts";

    private static final String CSS_ROOT = "/public/stylesheets";
    
    private static String GENERATE_DIR = Play.configuration.getProperty("asset.generate.dir");
    
    public static void js(String path) {
        String file_ext = "js";
        String contentType = "application/javascript";
        
        System.out.println(path);
        
        mergeRequestPathFile(path, JS_ROOT, file_ext, contentType);
    }


    public static void css(String path) {
        String file_ext = "css";
        String contentType = "text/css";
        
        System.out.println(path);
        
        mergeRequestPathFile(path, CSS_ROOT, file_ext, contentType);
    }    
    
    private static void mergeRequestPathFile(String path, String fileRoot, String file_ext,
            String contentType) {
        String requestPath = "/" + path;
        
        String pathName = path.replace('/', '_');
        String targetName = path.substring(path.lastIndexOf('/') + 1);
        System.out.println("targetName=" + targetName);
        File targetFile= new File(GENERATE_DIR + "/" + file_ext + "/" + pathName, targetName);
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        
        if (!targetFile.exists()) {
            try {
                BufferedWriter targetWrite = new BufferedWriter(new FileWriter(targetFile));
                String[] jsPath = requestPath.split(file_ext);
                
                for (String js : jsPath) {
                    File jsFile = new File(Play.applicationPath, fileRoot + js + file_ext);
                    System.out.println("js=" + jsFile.getPath());
                    if (jsFile.exists()) {
                        BufferedReader breader = new BufferedReader(new FileReader(jsFile));
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
        
        try {
            renderBinary(new FileInputStream(targetFile), targetName, contentType, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            error(404, "指定文件不存在");
        }
    }


}