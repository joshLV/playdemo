package unit;

import models.journal.MQJournal;
import org.junit.Test;
import play.test.UnitTest;

/**
 * @author likang
 *         Date: 12-4-10
 */
public class MQJournalTest extends UnitTest{
    @Test
    public void testMQJournal(){
        //为了测试而测试
        MQJournal journal = new MQJournal("test_queue", "test journal");
        assertNotNull(journal);
    }
}
