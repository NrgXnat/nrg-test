package org.nrg.testing.email

import org.nrg.testing.xnat.versions.XnatTestingVersionManager
import org.nrg.xnat.pogo.Project
import org.nrg.xnat.pogo.users.User
import org.nrg.xnat.versions.Xnat_1_7_4

abstract class XnatEmailCatalog {

    abstract void define(EmailQuery existingQuery)

    static XnatEmailCatalog verificationEmail(User user) {
        new XnatEmailCatalog() {
            @Override
            void define(EmailQuery existingQuery) {
                existingQuery.containing([
                        "${user.firstName} ${user.lastName}",
                        (XnatTestingVersionManager.testedVersionFollows(Xnat_1_7_4)) ? 'If you would like to register, please confirm your email address' : 'Please click this link to verify your email address'
                ])
            }
        }
    }

    static XnatEmailCatalog projectAccessEmail(Project project, boolean approved) {
        new XnatEmailCatalog() {
            @Override
            void define(EmailQuery existingQuery) {
                if (XnatTestingVersionManager.testedVersionFollows(Xnat_1_7_4)) {
                    existingQuery.containing(
                            approved ? "You have been granted access to the ${project.title} project" : "request to access the ${project.title} project has been denied"
                    )
                } else {
                    existingQuery.containing(
                            "${project.title} access ${approved ? 'granted' : 'denied'}"
                    )
                }
            }
        }
    }

}
