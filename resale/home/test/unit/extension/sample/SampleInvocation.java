package unit.extension.sample;

import util.extension.ExtensionInvocation;
import util.extension.annotation.ExtensionPoint;

/**
 * User: tanglq
 * Date: 13-3-5
 * Time: 下午4:40
 */
@ExtensionPoint("sample_invocation")
public abstract class SampleInvocation implements ExtensionInvocation<SampleContext> {

}
