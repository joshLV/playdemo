package util.extension;

import play.Logger;
import play.Play;
import util.extension.annotation.ExtensionPoint;
import util.extension.annotation.IgnoreExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Extension功能的召唤师。
 */
public class ExtensionInvoker {

    public static final Map<Class<?>, List<ExtensionInvocation>> extensionMap = new HashMap<>();

    /**
     * 从上下文初始化指定的BusinessExtension.
     */
    public static void initExtensions() {
        List<Class> categoryClazzList = Play.classloader.getAssignableClasses(ExtensionInvocation.class);

        for (Class<?> categoryClazz : categoryClazzList) {
            try {
                if (categoryClazz.isAnnotationPresent(ExtensionPoint.class)) {
                    ExtensionPoint extensionPoint = categoryClazz.getAnnotation(ExtensionPoint.class);
                    if (extensionPoint != null) {

                        String pointName = extensionPoint.value();
                        Logger.info("Found " + pointName + ":" + categoryClazz.getName());
                        // 得到此类别的所有子类，作为具体的执行子类
                        List<Class> invocationClazzList = Play.classloader.getAssignableClasses(categoryClazz);

                        for (Class<?> invocationClazz : invocationClazzList) {
                            List<ExtensionInvocation> extensionInvocationList = extensionMap.get(categoryClazz);
                            if (extensionInvocationList == null) {
                                extensionInvocationList = new ArrayList<>();
                                extensionMap.put(categoryClazz, extensionInvocationList);
                            }
                            if (!categoryClazz.isAnnotationPresent(IgnoreExtension.class)) {
                                // 只加入没有Ingore的类
                                ExtensionInvocation invocation = (ExtensionInvocation) invocationClazz.newInstance();
                                extensionInvocationList.add(invocation);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: public static List<ExtensionResult> runMatchs(...)

    public static ExtensionResult run(Class<?> extensionPointName, ExtensionContext context) {
        return run(extensionPointName, context, null);
    }

    /**
     * @param extensionPointName
     * @param context
     * @param defaultAction      //TODO: 这个需要有一个专门接口，不要用BusinessExtension
     * @return
     */
    public static ExtensionResult run(Class<?> extensionPointName,
                                      ExtensionContext context, DefaultAction defaultAction) {
        List<ExtensionInvocation> extensionList = extensionMap.get(extensionPointName);

        if (extensionList != null) { //无配置时会出现NullPointException
            for (ExtensionInvocation extension : extensionList) {
                if (extension.match(context)) {
                    return extension.execute(context);
                }
            }
        }

        // TODO: 接下来考虑做一下包装模式
        if (defaultAction != null) {
            return defaultAction.execute(context);
        }
        Logger.info("Not Found Any " + extensionPointName.getName()
                + " or DefaultAction to run!");
        return ExtensionResult.build().code(0).message("Not Found Any Invocation");
    }

}
