package yabo.extension.base;

import play.Play;
import yabo.extension.annotation.ExtensionPoint;
import yabo.extension.annotation.IgnoreExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: tanglq
 * Date: 13-3-4
 * Time: 下午4:11
 */
public class ExtensionHandler {

    public static final Map<String, List<BusinessExtension>> extensionMap = new HashMap<>();

    /**
     * 从上下文初始化指定的BusinessExtension.
     */
    public static void initExtensions() {
        List<Class> clazzList = Play.classloader.getAssignableClasses(BusinessExtension.class);

        for (Class<?> clazz : clazzList) {
            try {
                if (clazz.isAnnotationPresent(IgnoreExtension.class)) {
                    continue; // 跳过
                }
                if (clazz.isAnnotationPresent(ExtensionPoint.class)) {
                    ExtensionPoint extensionPoint = clazz.getAnnotation(ExtensionPoint.class);
                    if (extensionPoint != null) {
                        String pointName = extensionPoint.value();
                        List<BusinessExtension> businessExtensionList = extensionMap.get(pointName);
                        if (businessExtensionList == null) {
                            businessExtensionList = new ArrayList<>();
                            extensionMap.put(pointName, businessExtensionList);
                        }
                        BusinessExtension extension = (BusinessExtension) clazz.newInstance();
                        businessExtensionList.add(extension);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // extensionMap.put(extensionClass, businessExtensionList);
    }

    // TODO: public static List<ExtensionResult> runMatchs(...)

    public static ExtensionResult run(String extensionPointName, BusinessContext context) {
        return run(extensionPointName, context, null);
    }

    /**
     *
     * @param extensionPointName
     * @param context
     * @param defaultExtension //TODO: 这个需要有一个专门接口，不要我不用BusinessExtension
     * @return
     */
    public static ExtensionResult run(String extensionPointName,
                                      BusinessContext context, BusinessExtension defaultExtension) {
        List<BusinessExtension> extensionList = extensionMap.get(extensionPointName);

        for (BusinessExtension extension : extensionList) {
            if (extension.canUse(context)) {
                return extension.execute(context);
            }
        }

        // TODO: 接下来考虑做一下包装模式
        if (defaultExtension != null) {
            return defaultExtension.execute(context);
        }
        return null;
    }

}
