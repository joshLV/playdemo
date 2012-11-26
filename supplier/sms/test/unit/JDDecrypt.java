package unit;

import models.jingdong.groupbuy.JDGroupBuyUtil;
import org.junit.Test;
import play.test.UnitTest;

/**
 * @author likang
 *         Date: 12-11-22
 */
public class JDDecrypt extends UnitTest{
    String str = "39Ty2m39d9UozkgLo1h19yu35kOIsRjIevS10iZXoKymaPv7QtRRAkK0qfRfphK4Pz6U034CRVPK\n" +
            "eGoPL14tOXexNib/ffxjgNDG2sIRKRsSge+NfMlAs+VwInNO8oZqrCNAJWSG+RKlkRbj2V3GOvUp\n" +
            "ZPfGCGjhvwqnMv3xccFKfY70msZyZ26ubky/SL932Wr8Z4UgPQnRdB0PRFlcMNraAtj7GdgnuKeU\n" +
            "zuhm/l3+o0qQMLUbu9AZTaMeN4bkij1//BBN6j/l833cV2AawmEF5INX07RkrFpf/8tAx2Z3b6hv\n" +
            "higbRQCXlJVaMU0xah2GD35ZPDdGIf5DiR6zFaiqL7kx72ahZjZQGV8HGyqFCgYVUBsMTOuroUBn\n" +
            "sw7WurJlOBtz7vZn3bjOekSK7lfSOzjpYcQJRmj4Ohm2ErNdYcQ7IL8P7sH4d0PSRN2lCqyMrLlb\n" +
            "msStcG7A8vfMVCF9nFdaj3YuS0TiFfXGlFTqo40ewxMqt0LoDgMGQx1zJw/kkbatmi/UxJfy6SyB\n" +
            "shg5c0Da74KAYJvyn4xTt7I=";
    @Test
    public void testDecrypt() {
        System.out.println(JDGroupBuyUtil.decryptMessage(str));
    }
}
