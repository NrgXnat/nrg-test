package org.nrg.testing.dicom

class DicomEditUtils {

    public static final List<String> HEX_DIGITS = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f']
    public static final List<String> EVEN_DIGITS = HEX_DIGITS.findAll { HEX_DIGITS.indexOf(it) % 2 == 0 }
    public static final List<String> ODD_DIGITS = HEX_DIGITS - EVEN_DIGITS
    public static final List<String> WILDCARDS = ['X', 'x', '@', '#']
    public static final Map<String, List<String>> REPLACEMENTS = [
            'X' : HEX_DIGITS,
            'x' : HEX_DIGITS,
            '@' : EVEN_DIGITS,
            '#' : ODD_DIGITS
    ]

    static List<String> resolveAllDicomEditTags(String wildcardedTag) {
        final List<String> tagList = []
        final int wildcardIndex = wildCardIndex(wildcardedTag)
        if (wildcardIndex > -1) {
            final String wildcard = wildcardedTag.charAt(wildcardIndex) as String
            REPLACEMENTS[wildcard].each { digit ->
                tagList.addAll(resolveAllDicomEditTags(wildcardedTag.replaceFirst(wildcard, digit)))
            }
            tagList
        } else {
            [wildcardedTag]
        }
    }

    private static int wildCardIndex(String input) {
        WILDCARDS.collect { wildcard ->
            input.indexOf(wildcard)
        }.max()
    }

}
