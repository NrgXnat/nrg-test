package org.nrg.testing.xnat.processing.files.resources;

import org.nrg.xnat.pogo.resources.ResourceFile;

public class ProcessingResourceFile extends ResourceFile {

    private boolean regex = false;
    private String mutator;
    private String comparator;
    private String compareTo;
    private String expectedText;
    private long expectedSize;

    public boolean isRegex() {
        return regex;
    }

    public void setRegex(boolean regex) {
        this.regex = regex;
    }

    public String getMutator() {
        return mutator;
    }

    public void setMutator(String mutator) {
        this.mutator = mutator;
    }

    public String getComparator() {
        return comparator;
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

    public String getCompareTo() {
        return compareTo;
    }

    public void setCompareTo(String compareTo) {
        this.compareTo = compareTo;
    }

    public String getExpectedText() {
        return expectedText;
    }

    public void setExpectedText(String expectedText) {
        this.expectedText = expectedText;
    }

    public long getExpectedSize() {
        return expectedSize;
    }

    public void setExpectedSize(long expectedSize) {
        this.expectedSize = expectedSize;
    }

}
