package app.controllers.support;

import play.mvc.Controller;
import util.extension.ExtensionInvocation;
import util.extension.ExtensionInvoker;
import util.extension.annotation.ExtensionPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 列出ExtensionInvocation配置信息，供开发调试.
 */
public class ExtensionInvocations extends Controller {

    public static void index() {
        Map<Class<?>,List<ExtensionInvocation>> extensionMap = ExtensionInvoker.extensionMap;
        Map<String, List<ExtensionInvocation>> extensions = new HashMap<>();
        for (Class<?> extensionCategoryClazz : extensionMap.keySet()) {
            String key = extensionCategoryClazz.getName();
            if (extensionCategoryClazz.isAnnotationPresent(ExtensionPoint.class)) {
                ExtensionPoint extensionPoint = extensionCategoryClazz.getAnnotation(ExtensionPoint.class);
                key = extensionPoint.value() + ":" + extensionCategoryClazz.getName();
            }
            extensions.put(key, extensionMap.get(extensionCategoryClazz));
        }
        render(extensions);
    }

}
