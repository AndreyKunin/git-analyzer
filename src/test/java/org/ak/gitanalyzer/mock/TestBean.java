package org.ak.gitanalyzer.mock;

/**
 * Created by Andrew on 02.12.2016.
 */
public class TestBean {
    private String s1;
    private String s2;
    private Double d1;

    public TestBean(String s1, String s2, Double d1) {
        this.s1 = s1;
        this.s2 = s2;
        this.d1 = d1;
    }

    public String getS1() {
        return s1;
    }

    public String getS2() {
        return s2;
    }

    public Double getD1() {
        return d1;
    }
}
