package navigation;

import java.util.LinkedList;
import java.util.List;

import play.Logger;
import play.Play;
import play.Play.Mode;
import play.PlayPlugin;
import play.db.jpa.JPAPlugin;
import play.vfs.VirtualFile;


/**
 * Initialize and reload the navigation structure.
 * 
 * TODO: 重命名为RBACPlugin.
 */
public class NavigationPlugin extends PlayPlugin {

    // Timestamp the navigation was last loaded
    long lastLoaded = -1;
    List<VirtualFile> navigationFiles;
    
    @Override
    public void afterApplicationStart() {
        navigationFiles = new LinkedList<VirtualFile>();
        navigationFiles.add(VirtualFile.fromRelativePath("conf/rbac.xml"));
        detectChange();
    }

    @Override
    public void detectChange() {
        if (Play.mode == Mode.PROD && lastLoaded > 0) {
            return;
        }

        JPAPlugin.startTx(false);
        try {
            boolean reload = false;
            for (VirtualFile navigationFile : navigationFiles) {
                if (navigationFile.lastModified() > lastLoaded) {
                    lastLoaded = navigationFile.lastModified();
                    reload = true;
                }
            }

            if (reload) {
                Logger.info("Reloading navigation file");
                // TODO: Support multiple files
                RbacLoader.init(navigationFiles.get(0));
                
                // 初始化菜单名称
                NavigationHandler.initNamedMenus();
            }
        } finally {
            JPAPlugin.closeTx(false);
        }
    }

    @Override
    public void beforeInvocation() {
        NavigationHandler.clearMenuContext();
    }
}