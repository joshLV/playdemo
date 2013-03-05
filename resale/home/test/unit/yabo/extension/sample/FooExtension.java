package unit.yabo.extension.sample;

import play.Logger;
import yabo.extension.annotation.ExtensionPoint;
import yabo.extension.base.BusinessExtension;
import yabo.extension.base.ExtensionResult;

/**
 * User: tanglq
 * Date: 13-3-4
 * Time: 下午10:28
 */
@ExtensionPoint("Sample")
public class FooExtension implements BusinessExtension<SampleContext> {

    @Override
    public ExtensionResult execute(SampleContext context) {
        context.result += 100;
        Logger.info("foo execute");
        return ExtensionResult.build().code(0);
    }

    @Override
    public boolean canUse(SampleContext context) {
        Logger.info("check foo");
        return "foo".equals(context.type);
    }
}
