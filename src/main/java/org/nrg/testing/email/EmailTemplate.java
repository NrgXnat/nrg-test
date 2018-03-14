package org.nrg.testing.email;

import org.nrg.testing.util.ResourceLoader;
import org.nrg.testing.xnat.conf.Settings;
import org.nrg.xnat.util.FileIOUtils;
import org.nrg.xnat.util.TimeUtils;

import java.time.LocalDateTime;

public class EmailTemplate {

    private String currentTime = TimeUtils.UNAMBIGUOUS_DATETIME.format(LocalDateTime.now());
    private String template;

    public EmailTemplate(String template) {
        this.template = template;
    }

    public String read() {
        return FileIOUtils.readFile(ResourceLoader.copyAndGetResource(template)).replace("%site%", Settings.BASEURL).replace("%time%", currentTime);
    }

    public String usedTime() {
        return currentTime;
    }

}
