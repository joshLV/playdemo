package com.uhuila.common.util;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * TODO.
 * <p/>
 * User: sujie
 * Date: 10/19/12
 * Time: 1:04 PM
 */
public class HtmlUtilTest extends TestCase {

    @Test
    public void testToText() {
        String html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" +
                "<HTML><HEAD>" +
                "<META http-equiv=Content-Type content=\"text/html; charset=gb2312\">" +
                "<META content=\"MSHTML 6.00.6000.17095\" name=GENERATOR><LINK " +
                "href=\"BLOCKQUOTE{margin-Top: 0px; margin-Bottom: 0px; margin-Left: 2em}\"" +
                "rel=stylesheet></HEAD>" +
                "<BODY style=\"FONT-SIZE: 10pt; MARGIN: 10px; FONT-FAMILY: verdana\">" +
                "<DIV><FONT face=Verdana size=2>hello，测试邮件</FONT></DIV>" +
                "<DIV><FONT face=Verdana size=2></FONT>&nbsp;</DIV>" +
                "<DIV align=left><FONT face=Verdana color=#c0c0c0 size=2>2011-03-03 " +
                "</FONT></DIV><FONT face=Verdana size=2>"+
                "<HR style=\"WIDTH: 122px; HEIGHT: 2px\" align=left SIZE=2>"+

                "<DIV><FONT face=Verdana color=#c0c0c0 size=2><SPAN>shopeye7</SPAN> " +
                "</FONT></DIV></FONT></BODY></HTML>" ;

        assertEquals("hello，测试邮件 2011-03-03 \nshopeye7 ", HtmlUtil.html2text(html));
    }

}
