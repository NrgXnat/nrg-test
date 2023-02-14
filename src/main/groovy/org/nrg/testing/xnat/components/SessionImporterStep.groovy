package org.nrg.testing.xnat.components

import org.nrg.testing.enums.TestData
import org.nrg.testing.xnat.BaseXnatRestTest
import org.nrg.xnat.enums.MergeBehavior
import org.nrg.xnat.importer.importers.SessionImporterRequest
import org.nrg.xnat.pogo.Project

class SessionImporterStep implements TestComponent {

    private final File data
    private final MergeBehavior overwrite

    SessionImporterStep(File data, MergeBehavior overwrite = null) {
        this.data = data
        this.overwrite = overwrite
    }

    SessionImporterStep(TestData data, MergeBehavior overwrite = null) {
        this(data.toFile(), overwrite)
    }

    @Override
    void perform(BaseXnatRestTest xnatRestTest, Project project) {
        final SessionImporterRequest request = new SessionImporterRequest()
                .destArchive(project)
                .file(data)
        if (overwrite != null) {
            request.overwrite(overwrite)
        }
        xnatRestTest.mainInterface().callImporter(request)
    }

}