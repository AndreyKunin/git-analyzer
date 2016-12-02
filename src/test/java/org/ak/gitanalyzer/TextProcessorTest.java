package org.ak.gitanalyzer;

import org.ak.gitanalyzer.http.processor.ProcessorMock;
import org.ak.gitanalyzer.mock.TestBean;
import org.ak.gitanalyzer.util.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Created by Andrew on 02.12.2016.
 */
public class TextProcessorTest {

    @Before
    public void init() throws Exception {
        Configuration.INSTANCE.clean();
        Configuration.INSTANCE.initConfiguration();
    }

    @Test
    public void getCSVForReportTest() throws Exception {
        ProcessorMock processor = new ProcessorMock(Configuration.nf.get());
        Collection<TestBean> objects = Arrays.asList(new TestBean("s10", "s20", 0.1), new TestBean("s11", "s21", 0.1), new TestBean("s12", "s22", 0.2));
        String result = processor.getCSVForReport(new String[] {"h1", "h2,1", "h3\\1", "h4\"1"}, objects, (tb, sb) -> {
            processor.getHtmlWriter().appendDouble(sb, "name1", tb.getD1());
            processor.getHtmlWriter().appendString(sb, "name2", tb.getS1());
            processor.getHtmlWriter().appendInteger(sb, "name3", (int) (tb.getD1() * 10));
            processor.getHtmlWriter().appendString(sb, "name4", tb.getS2() + ",\\\"", true);
        });
        assertEquals("\"h1\",\"h2\\,1\",\"h3\\\\1\",\"h4\\\"1\"\n" +
                "    \"name1\": 0.1,\n" +
                "    \"name2\": \"s10\",\n" +
                "    \"name3\": 1,\n" +
                "    \"name4\": \"s20,\\\"\"\n" +
                "\n" +
                "    \"name1\": 0.1,\n" +
                "    \"name2\": \"s11\",\n" +
                "    \"name3\": 1,\n" +
                "    \"name4\": \"s21,\\\"\"\n" +
                "\n" +
                "    \"name1\": 0.2,\n" +
                "    \"name2\": \"s12\",\n" +
                "    \"name3\": 2,\n" +
                "    \"name4\": \"s22,\\\"\"\n" +
                "\n", result);
    }

    @Test
    public void getJSONForTableTest() throws Exception {
        ProcessorMock processor = new ProcessorMock(Configuration.nf.get());
        Collection<TestBean> objects = Arrays.asList(new TestBean("s10", "s20", 0.1), new TestBean("s11", "s21", 0.1), new TestBean("s12", "s22", 0.2));
        String result = processor.getJSONForTable(objects, (tb, sb) -> {
            processor.getHtmlWriter().appendDouble(sb, "name1", tb.getD1());
            processor.getHtmlWriter().appendString(sb, "name2", tb.getS1());
            processor.getHtmlWriter().appendInteger(sb, "name3", (int) (tb.getD1() * 10));
            processor.getHtmlWriter().appendLink(sb, "name4", "id1", "function1", tb.getS1(), tb.getS2());
            processor.getHtmlWriter().appendLink(sb, "name5", "id1", "function1", true, tb.getS1());
        });
        assertEquals("[\n" +
                "  {\n" +
                "    \"name1\": 0.1,\n" +
                "    \"name2\": \"s10\",\n" +
                "    \"name3\": 1,\n" +
                "    \"name4\": \"<a id=\\\"id10\\\" href=\\\"javascript:function1('##', 's10', 's20')\\\">Open</a>\",\n" +
                "    \"name5\": \"<a id=\\\"id11\\\" href=\\\"javascript:function1('##', 's10')\\\">Open</a>\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name1\": 0.1,\n" +
                "    \"name2\": \"s11\",\n" +
                "    \"name3\": 1,\n" +
                "    \"name4\": \"<a id=\\\"id12\\\" href=\\\"javascript:function1('##', 's11', 's21')\\\">Open</a>\",\n" +
                "    \"name5\": \"<a id=\\\"id13\\\" href=\\\"javascript:function1('##', 's11')\\\">Open</a>\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"name1\": 0.2,\n" +
                "    \"name2\": \"s12\",\n" +
                "    \"name3\": 2,\n" +
                "    \"name4\": \"<a id=\\\"id14\\\" href=\\\"javascript:function1('##', 's12', 's22')\\\">Open</a>\",\n" +
                "    \"name5\": \"<a id=\\\"id15\\\" href=\\\"javascript:function1('##', 's12')\\\">Open</a>\"\n" +
                "  }\n" +
                "]", result.replaceAll("'id\\d+'", "'##'"));
    }

    @Test
    public void getJSONFor2DTest() throws Exception {
        ProcessorMock processor = new ProcessorMock(Configuration.nf.get());
        Collection<TestBean> objects = Arrays.asList(new TestBean("s10", "s20", 0.1), new TestBean("s11", "s21", 0.1), new TestBean("s12", "s22", 0.2));
        String result = processor.getJSONFor2D(objects, (tb, sb) -> processor.getHtmlWriter().appendChartEntry(sb, tb.getD1()));
        assertEquals("0.1,0.1,0.2", result);
    }

    @Test
    public void getJSONForGraphTest() throws Exception {
        ProcessorMock processor = new ProcessorMock(Configuration.nf.get());
        Collection<TestBean> objects = Arrays.asList(new TestBean("s10", "s20", 0.1), new TestBean("s11", "s21", 0.1), new TestBean("s12", "s22", 0.2));
        String result = processor.getJSONForGraph(objects, tb -> (int) (tb.getD1() * 10), (tb, sb, recordId) -> {
            processor.getHtmlWriter().appendDouble(sb, "name1", tb.getD1());
            processor.getHtmlWriter().appendString(sb, "name2", tb.getS1());
            processor.getHtmlWriter().appendInteger(sb, "name1", recordId, true);
        });
        assertEquals("{\n" +
                "    \"name1\": 0.1,\n" +
                "    \"name2\": \"s10\",\n" +
                "    \"name1\": 1\n" +
                "},\n" +
                "{\n" +
                "    \"name1\": 0.1,\n" +
                "    \"name2\": \"s11\",\n" +
                "    \"name1\": 1\n" +
                "},\n" +
                "{\n" +
                "    \"name1\": 0.2,\n" +
                "    \"name2\": \"s12\",\n" +
                "    \"name1\": 2\n" +
                "}\n", result);
    }

    @Test
    public void getTypeStringTest() throws Exception {
        ProcessorMock processor = new ProcessorMock(Configuration.nf.get());
        assertEquals("thick", processor.getTypeString(1.05, 1.0, 0.9));
        assertEquals("normal", processor.getTypeString(0.95, 1.0, 0.9));
        assertEquals("thin", processor.getTypeString(0.85, 1.0, 0.9));
    }
}
