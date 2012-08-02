package unit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import play.test.UnitTest;
import unit.data.ScoreData;
import utils.CrossTableConverter;
import utils.CrossTableUtil;

public class CrossTableUtilTest extends UnitTest {

    CrossTableConverter<ScoreData, BigDecimal> converter = new CrossTableConverter<ScoreData, BigDecimal>() {
        @Override
        public String getRowKey(ScoreData target) {
            return target.studentName;
        }

        @Override
        public String getColumnKey(ScoreData target) {
            return target.courseName;
        }

        @Override
        public BigDecimal addValue(ScoreData target, BigDecimal oldValue) {
            if (target.score == null) {
                return oldValue;
            }
            if (oldValue != null) {
                return oldValue.add(target.score);
            }
            return target.score;
        }
    };
    
    @Test
    public void 测试生成交叉表() {
        List<ScoreData> testList = generateScoreList();
        List<Map<String, Object>> resultMapedList = CrossTableUtil.generateCrossTable(testList, converter);
        
        assertNotNull(resultMapedList);
        assertEquals(3, resultMapedList.size());
        assertEquals("张三", resultMapedList.get(0).get(CrossTableUtil.KEY_COLUMN));
        // 张三有2个英语成绩，总成绩会成为150
        assertEquals(new BigDecimal("150"), resultMapedList.get(0).get("英语"));
        
        assertEquals("李四", resultMapedList.get(1).get(CrossTableUtil.KEY_COLUMN));
        assertEquals("赵五", resultMapedList.get(2).get(CrossTableUtil.KEY_COLUMN));
        // 赵五没有英语成绩
        assertNull(resultMapedList.get(2).get("英语"));
    }
    
    private List<ScoreData> generateScoreList() {
        ArrayList<ScoreData> list = new ArrayList<>();
        
        list.add(new ScoreData("张三", "语文", new BigDecimal("89")));
        list.add(new ScoreData("张三", "数学", new BigDecimal("92")));
        list.add(new ScoreData("张三", "英语", new BigDecimal("100")));
        list.add(new ScoreData("张三", "物理", new BigDecimal("79")));
        
        list.add(new ScoreData("李四", "语文", new BigDecimal("85")));
        list.add(new ScoreData("李四", "数学", new BigDecimal("91")));
        list.add(new ScoreData("李四", "英语", new BigDecimal("90")));
        list.add(new ScoreData("李四", "物理", new BigDecimal("69")));

        // 张三有2个英语成绩，总成绩会成为150
        list.add(new ScoreData("张三", "英语", new BigDecimal("50")));
        
        // 无英语成绩
        list.add(new ScoreData("赵五", "语文", new BigDecimal("89")));
        list.add(new ScoreData("赵五", "数学", new BigDecimal("92")));
        list.add(new ScoreData("赵五", "物理", new BigDecimal("79")));
        
        return list;
    }
}
