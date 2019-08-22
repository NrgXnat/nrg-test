package org.nrg.testing.email

import org.nrg.testing.CommonStringUtils
import org.nrg.testing.FileIOUtils
import org.nrg.testing.TimeUtils
import org.nrg.testing.xnat.conf.Settings

class MessageTemplate {

    private final String currentTime = TimeUtils.getTimestamp(TimeUtils.UNAMBIGUOUS_DATETIME)
    private final String template
    private final Map<String, String> replacements = [:]

    MessageTemplate(String template) {
        this.template = template
        replacements.put('%site%', Settings.BASEURL)
        replacements.put('%time%', currentTime)
    }

    MessageTemplate replace(String key, Object value) {
        replacements.put(key, String.valueOf(value))
        this
    }

    String read() {
        CommonStringUtils.replaceEach(FileIOUtils.loadResource(template).text, replacements)
    }

    String usedTime() {
        currentTime
    }

}
