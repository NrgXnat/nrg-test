package org.nrg.testing.xnat.processing.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.nrg.jira.testing_components.TestStatus;
import org.nrg.testing.util.IgnoreNullList;
import org.nrg.testing.xnat.processing.ProcessingCheckable;
import org.nrg.testing.xnat.processing.SessionRenewer;
import org.nrg.testing.xnat.processing.files.jackson.module.ProcessingFileJacksonModule;
import org.nrg.testing.xnat.rest.XnatRestDriver;
import org.nrg.xnat.jackson.mappers.YamlObjectMapper;
import org.nrg.xnat.pogo.experiments.ImagingSession;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ProcessingFileSetRequest implements ProcessingCheckable {

    private static final ObjectMapper yamlMapper = new YamlObjectMapper().registerModule(ProcessingFileJacksonModule.build());
    private String fileName;

    public ProcessingFileSetRequest(String name) {
        fileName = name;
    }

    public List<String> checkFilesMatch(ImagingSession session, XnatRestDriver xnatRestDriver, SessionRenewer sessionRenewer) {
        final IgnoreNullList<String> verificationErrors = new IgnoreNullList<>();
        try {
            final ProcessingFileSets processingFileSets = yamlMapper.readValue(xnatRestDriver.mainCredentials().get(xnatRestDriver.formatRestUrl("experiments", session.getAccessionNumber(), "/resources/validation/files", fileName)).then().assertThat().statusCode(200).and().extract().body().asInputStream(), ProcessingFileSets.class);
            xnatRestDriver.passStep();

            verificationErrors.addAll(processingFileSets.validate(xnatRestDriver, session, sessionRenewer));
            if (verificationErrors.isEmpty()) {
                Logger.getLogger(ProcessingFileSets.class).info("All files present and valid for " + fileName);
                xnatRestDriver.passStep();
            } else {
                xnatRestDriver.captureStep(TestStatus.FAIL, verificationErrors.join());
            }
            return verificationErrors;
        } catch (IOException e) {
            return Collections.singletonList("Could not read session resource file specifying how to verify processing output");
        }
    }

}
