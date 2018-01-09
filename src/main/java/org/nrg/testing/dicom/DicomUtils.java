package org.nrg.testing.dicom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DicomUtils {

    public static final String HEX_DIGITS = "0123456789abcdef";

    public static List<String> resolveAllDicomEditTags(String wildcardedTag) {
        final List<String> tagList = new ArrayList<>();
        final int wildcardIndex = wildCardIndex(wildcardedTag);
        if (wildcardIndex > -1) {
            final char wildcard = wildcardedTag.charAt(wildcardIndex);
            switch (wildcard) {
                case 'X':
                case 'x':
                    for (int i = 0; i < HEX_DIGITS.length(); i++) {
                        tagList.addAll(resolveAllDicomEditTags(wildcardedTag.replaceFirst("X|x", Character.toString(HEX_DIGITS.charAt(i)))));
                    }
                    return tagList;
                case '@':
                    for (int i = 0; i < HEX_DIGITS.length(); i += 2) {
                        tagList.addAll(resolveAllDicomEditTags(wildcardedTag.replaceFirst("@", Character.toString(HEX_DIGITS.charAt(i)))));
                    }
                    return tagList;
                case '#':
                    for (int i = 1; i < HEX_DIGITS.length(); i += 2) {
                        tagList.addAll(resolveAllDicomEditTags(wildcardedTag.replaceFirst("#", Character.toString(HEX_DIGITS.charAt(i)))));
                    }
                    return tagList;
                default:
                    throw new RuntimeException("This shouldn't be possible");
            }
        } else {
            return Collections.singletonList(wildcardedTag);
        }
    }

    private static int wildCardIndex(String input) {
        return Collections.max(Arrays.asList(input.indexOf('X'), input.indexOf('x'), input.indexOf('@'), input.indexOf('#')));
    }

    @Deprecated // moved to grxnat DicomUtils
    public static int dicomTagTransform(String friendlyDicomHeader) {
        return Integer.parseInt(friendlyDicomHeader.replace("(", "").replace(")", "").replace(",", "").replace(" ", ""), 16);
    }

}
