package org.nrg.testing.xnat.processing.files.comparators.imaging

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class PixelValue {

    int red, green, blue, gray
    boolean isColor

    PixelValue(int red, int green, int blue) {
        this.red = red
        this.green = green
        this.blue = blue
        isColor = true
    }

    PixelValue(int gray) {
        this.gray = gray
        isColor = false
    }

    protected PixelValue() {}

}
