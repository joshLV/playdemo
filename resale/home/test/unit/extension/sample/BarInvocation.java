package unit.extension.sample;

import play.Logger;
import util.extension.ExtensionResult;

/**
 * User: tanglq
 * Date: 13-3-4
 * Time: 下午10:46
 */
public class BarInvocation extends SampleInvocation {
    @Override
    public ExtensionResult execute(SampleContext context) {
        context.result -= 100;
        Logger.info("bar execute");
//        return ExtensionResult.build().code(1).message("Bar Error!");
        return ExtensionResult.code(1).message("Bar Error!");

    }

    @Override
    public boolean match(SampleContext context) {
        Logger.info("check bar");
        return "bar".equals(context.type);
    }
}
