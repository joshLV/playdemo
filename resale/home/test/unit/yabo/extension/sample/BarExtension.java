package unit.yabo.extension.sample;

import play.Logger;
import yabo.extension.annotation.ExtensionPoint;
import yabo.extension.base.BusinessExtension;
import yabo.extension.base.ExtensionResult;

/**
 * User: tanglq
 * Date: 13-3-4
 * Time: 下午10:46
 */
@ExtensionPoint("Sample")
public class BarExtension implements BusinessExtension<SampleContext> {
    @Override
    public ExtensionResult execute(SampleContext context) {
        context.result -= 100;
        Logger.info("bar execute");
        return ExtensionResult.build().code(1).message("Bar Error!");
    }

    @Override
    public boolean canUse(SampleContext context) {
        Logger.info("check bar");
        return "bar".equals(context.type);
    }
}
