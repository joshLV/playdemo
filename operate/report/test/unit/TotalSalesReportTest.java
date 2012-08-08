package unit;

import java.text.SimpleDateFormat;
import java.util.List;
import models.totalsales.TotalSalesCondition;
import models.totalsales.TotalSalesReport;
import org.junit.Test;
import play.test.UnitTest;

public class TotalSalesReportTest extends UnitTest {

    @Test
    public void testGenerateDateList() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        TotalSalesCondition condition = new TotalSalesCondition();
        condition.beginAt = df.parse("2012-06-01");
        condition.endAt = df.parse("2012-06-04");
        
        List<String> dateList = TotalSalesReport.generateDateList(condition);
        assertEquals(4, dateList.size());
        assertEquals("2012-6-01", dateList.get(0));
        assertEquals("2012-6-02", dateList.get(1));
        assertEquals("2012-6-04", dateList.get(3));
    }

    @Test
    public void testGenerateDateListOnlyOneDay() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        TotalSalesCondition condition = new TotalSalesCondition();
        condition.beginAt = df.parse("2012-07-01");
        condition.endAt = df.parse("2012-07-01");
        
        List<String> dateList = TotalSalesReport.generateDateList(condition);
        assertEquals(1, dateList.size());
        assertEquals("2012-7-01", dateList.get(0));
    }    

    @Test
    public void testGenerateDateListDecOnlyOneDay() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        TotalSalesCondition condition = new TotalSalesCondition();
        condition.beginAt = df.parse("2012-12-01");
        condition.endAt = df.parse("2012-12-01");
        
        List<String> dateList = TotalSalesReport.generateDateList(condition);
        assertEquals(1, dateList.size());
        assertEquals("2012-12-01", dateList.get(0));
    }        
}
