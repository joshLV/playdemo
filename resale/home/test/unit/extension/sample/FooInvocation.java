package unit.extension.sample;

import play.Logger;
import util.extension.ExtensionResult;
import util.extension.annotation.ExtensionPoint;

/**
 * User: tanglq
 * Date: 13-3-4
 * Time: 下午10:28
 */
@ExtensionPoint("Sample")
public class FooInvocation extends SampleInvocation {

    @Override
    public ExtensionResult execute(SampleContext context) {
        context.result += 100;
        Logger.info("foo execute");
//        return ExtensionResult.build().code(0);
        return ExtensionResult.code(0);
    }

    @Override
    public boolean match(SampleContext context) {
        Logger.info("check foo");
        return "foo".equals(context.type);
    }
}
