package jobs.dadong;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于解析大东票务提供的字符串型XML。
 * User: tanglq
 * Date: 13-1-28
 * Time: 下午2:50
 */
public class DadongXmlNodePath {

    /**
     * 返回<tagName></tagName>所包含的字符串内容数组。
     *
     * @param tagName
     * @param document
     * @return
     */
    public static List<String> selectNodes(final String tagName, final String document) {
        String beginTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";

        List<String> nodes = new ArrayList<>();
        int beginAt = document.indexOf(beginTag, 0) + beginTag.length();
        int endAt = document.indexOf(endTag, 0);

        while (beginAt > 0 && endAt > 0) {
            String value = document.substring(beginAt, endAt);
            nodes.add(value);
            beginAt = document.indexOf(beginTag, beginAt) + beginTag.length();
            endAt = document.indexOf(endTag, endAt+endTag.length());
        }

        return nodes;
    }

    public static String selectText(String tagName, String document) {
        List<String> nodes = selectNodes(tagName, document);
        if (nodes.size() > 0) {
            return nodes.get(0);
        }
        return null;
    }
}
