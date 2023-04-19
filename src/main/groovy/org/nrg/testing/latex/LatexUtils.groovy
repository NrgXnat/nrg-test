package org.nrg.testing.latex

import org.nrg.testing.FileIOUtils

import java.nio.file.Paths
import java.util.regex.Pattern

class LatexUtils {

    static String loadTemplate(String fileName) {
        FileIOUtils.loadResource(Paths.get('latex', fileName).toString()).text
    }

    static Pattern regexSpacesAndKey(String replacementKey) {
        Pattern.compile("^ *${replacementKey}\$")
    }

    static String offset(int numSpaces, List<String> inputLines) {
        final String spaces = ' ' * numSpaces
        inputLines.collect { line ->
            spaces + line
        }.join('\n')
    }

    static String replaceAndMaintainIndent(String baseContent, String replacementKey, String replacementValue) {
        replaceAndMaintainIndentFromList(baseContent, replacementKey, replacementValue.split('\n') as List<String>)
    }

    static String replaceAndMaintainIndentFromList(String baseContent, String replacementKey, List<String> replacementValue) {
        final Pattern regex = regexSpacesAndKey(replacementKey)
        baseContent.split('\n').collect { line ->
            if (line.matches(regex)) {
                offset(line.count(' '), replacementValue)
            } else {
                line
            }
        }.join('\n')
    }

    static String joinOptionalList(List<String> inputList) {
        inputList ? "[${inputList.join(',')}]" : ''
    }

}
