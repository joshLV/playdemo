package unit.extension.sample;

import util.extension.ExtensionInvocation;
import util.extension.annotation.ExtensionPoint;

/**
 * User: tanglq
 * Date: 13-3-5
 * Time: 下午5:03
 */
@ExtensionPoint("NotsupportedTest")
public abstract class NotsupportedInvocation implements ExtensionInvocation<SampleContext> {
}
