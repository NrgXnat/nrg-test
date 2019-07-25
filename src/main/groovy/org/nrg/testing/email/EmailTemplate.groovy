package org.nrg.testing.email

import org.nrg.testing.CommonStringUtils
import org.nrg.testing.FileIOUtils
import org.nrg.testing.xnat.conf.Settings
import org.nrg.xnat.util.TimeUtils

import java.time.LocalDateTime

class EmailTemplate {

    private final String currentTime = TimeUtils.UNAMBIGUOUS_DATETIME.format(LocalDateTime.now())
    private final String template
    private final Map<String, String> replacements = new HashMap<>()

    EmailTemplate(String template) {
        this.template = template
        replacements.put("%site%", Settings.BASEURL)
        replacements.put("%time%", currentTime)
    }

    EmailTemplate replace(String key, Object value) {
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
