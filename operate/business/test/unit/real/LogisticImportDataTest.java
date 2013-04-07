package unit.real;

import models.order.LogisticImportData;
import org.junit.Ignore;
import org.junit.Test;
import play.test.UnitTest;

import java.math.BigDecimal;
import java.util.List;

/**
 * User: tanglq
 * Date: 13-4-7
 * Time: 上午11:55
 */
public class LogisticImportDataTest extends UnitTest {

    @Ignore
    @Test
    public void testWubaLogisticImport() throws Exception {
        LogisticImportData wubaData = new LogisticImportData();
        wubaData.outerGoodsNo = "TestGoods1x1\nTestGoods2x2";
        wubaData.salePrice = BigDecimal.TEN;

        List<LogisticImportData> logisticImportDataList = LogisticImportData.processWubaLogistic(wubaData);
        assertEquals(2, logisticImportDataList.size());
        assertEquals("TestGoods1", logisticImportDataList.get(0).outerGoodsNo);
        assertEquals(new Long(1), logisticImportDataList.get(0).buyNumber);
        assertEquals("TestGoods2", logisticImportDataList.get(1).outerGoodsNo);
        assertEquals(new Long(2), logisticImportDataList.get(1).buyNumber);
    }
}
