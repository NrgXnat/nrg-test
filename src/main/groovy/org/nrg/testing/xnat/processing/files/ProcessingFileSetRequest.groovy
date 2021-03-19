package org.nrg.testing.xnat.processing.files

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.restassured.response.Response
import org.apache.log4j.Logger
import org.nrg.jira.components.zephyr.TestStatus
import org.nrg.testing.util.IgnoreNullList
import org.nrg.testing.xnat.processing.ProcessingCheckable
import org.nrg.testing.xnat.processing.SessionRenewer
import org.nrg.testing.xnat.processing.files.jackson.module.ProcessingFileJacksonModule
import org.nrg.testing.xnat.rest.XnatRestDriver
import org.nrg.xnat.jackson.mappers.YamlObjectMapper
import org.nrg.xnat.pogo.experiments.ImagingSession

class ProcessingFileSetRequest implements ProcessingCheckable {

    private static final Logger LOGGER = Logger.getLogger(ProcessingFileSets)
    private static final ObjectMapper yamlMapper = new YamlObjectMapper().registerModule(new ProcessingFileJacksonModule())
    private final String fileName

    ProcessingFileSetRequest(String name) {
        fileName = name
    }

    List<String> checkFilesMatch(ImagingSession session, XnatRestDriver xnatRestDriver, SessionRenewer sessionRenewer) {
        final IgnoreNullList<String> verificationErrors = new IgnoreNullList<>()
        final Response validationFileResponse = xnatRestDriver.mainCredentials.get(xnatRestDriver.mainInterface().formatRestUrl("/experiments/${session.accessionNumber}/resources/validation/files/${fileName}"))
        if (validationFileResponse.statusCode != 200) {
            return ['REST call to retrieve specification for file verification failed']
        }
        final ProcessingFileSets processingFileSets
        try {
            processingFileSets = yamlMapper.readValue(validationFileResponse.asInputStream(), ProcessingFileSets)
        } catch (Exception ignored) {
            return ['Could not read session resource file specifying how to verify processing output']
        }
        xnatRestDriver.passStep()

        verificationErrors.addAll(processingFileSets.validate(xnatRestDriver, session, sessionRenewer))
        if (verificationErrors.isEmpty()) {
            LOGGER.info("All files present and valid for ${fileName}")
            xnatRestDriver.passStep()
        } else {
            xnatRestDriver.captureStep(TestStatus.FAIL, verificationErrors.join())
        }
        verificationErrors
    }

}
