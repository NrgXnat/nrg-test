package org.nrg.testing.email;

import org.nrg.testing.util.ResourceLoader;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.xnat.util.FileIOUtils;
import org.nrg.xnat.util.TimeUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class EmailTemplate {

    private String currentTime = TimeUtils.UNAMBIGUOUS_DATETIME.format(LocalDateTime.now());
    private String template;
    private Map<String, String> replacements = new HashMap<>();

    public EmailTemplate(String template) {
        this.template = template;
        replacements.put("%site%", Settings.BASEURL);
        replacements.put("%time%", currentTime);
    }

    public EmailTemplate replace(String key, Object value) {
        replacements.put(key, String.valueOf(value));
        return this;
    }

    public String read() {
        String baseString = FileIOUtils.readFile(ResourceLoader.copyAndGetResource(template));
        for (Map.Entry<String, String> replacementEntry : replacements.entrySet()) {
            baseString = baseString.replace(replacementEntry.getKey(), replacementEntry.getValue());
        }
        return baseString;
    }

    public String usedTime() {
        return currentTime;
    }

}
