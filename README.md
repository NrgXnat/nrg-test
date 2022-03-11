# Test framework for XNAT-based systems #

## Usage ##
Most of the configuration properties can be included either on the command-line (using the syntax -Dproperty.name=property.value) when running the tests, or in the main properties file. By default, the tests will attempt to find the properties file in src/test/resources/config/local.properties. However, a different properties file (which is still required to be in src/test/resources/config) may be specified with the xnat.config property. If a property is found in both the properties file, and on the command-line, the value on the command line will take precedence. Provided here is an example command to launch the tests:
```
#!bash

$ mvn clean test -Dxnat.config=myxsync.properties -Dxnat.main.password=passw0rd -Dxnat.mainAdmin.password=passw0rd -Dxnat.admin.password=passw0rd
```

## Configuration ##
Each specific configuration setting may be done as a command line argument, or from the standard .properties file. Instead of defaulting to using local.properties, another file may be specified on the command line with -Dxnat.config=your_file (the analogous setting for jira.properties (discussed later) is jira.config). For security reasons, it's a better idea to use the command line for secure values such as passwords, although you may still use the .properties file (with warnings). Any arguments marked "Required" must be specified somewhere for every project, or the tests will not run correctly, while "Optional" parameters can sometimes be left out depending on the property and project. Parameters marked with an asterisk are only used by the tests using the [NRG_Selenium](https://bitbucket.org/xnatdev/nrg_selenium) framework (which extends this one).

* xnat.main.user: (Required) - The username of a non-admin account. This account need not exist unless xnat.init is false.
* xnat.main.password: (Required) - The password of a non-admin account. This account need not exist unless xnat.init is false.
* xnat.mainAdmin.user: (Optional) - The username of an admin account. This account need not exist.
* xnat.mainAdmin.password: (Optional) - The password of an admin account. This account need not exist.
* xnat.admin.user: (Optional) - The username of an admin account. This account must exist and is used to create the main accounts.
* xnat.admin.password: (Optional) - The password of an admin account. This account must exist and is used to create the main accounts.
* xnat.version: (Required) - The version of XNAT the server is running. Currently supported: 1.6dev, 1.7.2, 1.7.3, 1.7.4, 1.7.5, 1.7.5.2, 1.7.6, 1.7.7, 1.7dev, 1.8.0
* xnat.users.email: (Required) - The email address for the selenium user accounts. This must be a gmail account.
* xnat.users.email.password: (Optional) - The password for the email address for the main user accounts.
* xnat.baseurl: (Required) - The URL for the test environment.
* xnat.defaultTimeout*: (Optional) - Timeout in seconds for certain Selenium operations. Defaults to 30.
* xnat.browser*: (Optional) - Browser to run the tests in, which must be installed on your system. Currently supports Firefox and (Windows) Chrome. Defaults to Firefox.
* xnat.init*: (Optional) - Should main XNAT accounts be set up? Useful to turn off to speed up debugging, when the accounts already exist, or when admin credentials are not provided. Defaults to true.
* xnat.dicom.host: (Optional) - Host for XNAT DICOM Receiver. Defaults to the host specified in xnat.baseurl.
* xnat.dicom.port: (Optional) - Port number for XNAT DICOM Receiver. Defaults to 8104.
* xnat.dicom.aetitle: (Optional) - DICOM AE Title for XNAT DICOM Receiver. Defaults to "XNAT".
* * xnat.dicom.callingaetitle: (Optional) - The calling AE Title used to identify this application when sending a c-store request to the XNAT DICOM Receiver. Defaults to "GRXNAT".
* xnat.jira: (Optional) - Should a testing cycle be created in JIRA according to settings found in jira.properties? Defaults to false.
* xnat.requireAdmin: (Optional) - Are admin accounts required/allowed? Defaults to true.
* xnat.notifiedEmails: (Optional) - Comma-separated list of emails to which results will be sent on test completion. If true, requires xnat.users.email.password to be set.
* xnat.notifyOnSuccess: (Optional) - Should summary email be sent when all tests pass? Defaults to false (as long as prerequisite properties are set).
* xnat.notificationTitle: (Optional) - What name should be used for the test suite in the summary email? This is optional for running tests in general, but required for the summary email to be sent.
* xnat.dependencies: (Optional) - Can be set to false to disable dependency checks between certain tests. Defaults to true.
* xnat.pipeline.useDynamicOrdering*: (Optional) - For use in pipeline tests only: if set to true, uses a [Multiprocessor scheduling](https://bitbucket.org/xnatdev/multiprocessor-scheduling) algorithm to order tests. Otherwise uses the order in which methods are defined in class. Defaults to false.
* xnat.pipeline.slots*: (Optional) - Number of pipeline queue slots. Used only in pipeline tests. Required if xnat.pipeline.useDynamicOrdering is true.
* xnat.timelogs: (Optional) - Can be set to true to generate summary CSVs of tests with runtimes and simple statistics. Defaults to false.
* xnat.gitLogs: (Optional) - Can be set to true to turn XNAT and tomcat logs into git repos (requires test_logger XNAT plugin). Defaults to false.
* xnat.basic: (Optional) - Can be set to true to only run methods/classes annotated with `@Basic` (marking either the test or the class is sufficient). If false (or left out), all allowed tests will be run.
* xnat.db.url: (Optional) - URL for XNAT database if tests need DB access.
* xnat.db.user: (Optional) - User for XNAT database if tests need DB access.
* xnat.db.password: (Optional) - Password for XNAT database if tests need DB access.
* xnat.captureDom*: (Optional) - Specify whether or not the state of the DOM (contents of body in the DOM) should be recorded when a test fails. Defaults to false.
* xnat.ssh.user: (Optional) - Username for an account to use for SSH. Required by some tests.
* xnat.ssh.key: (Optional) - Name of SSH private key. Required by some tests.
* xnat.producePdf: (Optional) - If set to true, results from JIRA will be scraped to export the test cycle as a PDF. Requires pdflatex (pdflatex must be on $PATH). Defaults to false.
* xnat.setupMrscan: (Optional) - If set to true, the xnat:mrScanData data type will be set-up on the XNAT server in startup. Defaults to false.
* xnat.testBehavior.expectedFailures: (Optional) - For tests or classes annotated with `@ExpectedFailure`, how should the behavior be handled? Supported values: run, skip, ignore. Defaults to skip.
* xnat.testBehavior.missingPlugins: (Optional) - For tests or classes annotated with plugin dependencies, how should these tests be handled when one or more of the required plugins is not installed? Supported values: skip, ignore. Defaults to ignore.
* xnat.temp: (Optional) - Path to a directory to use as temp storage. Defaults to the system-provided default.
* mail.smtp.host: (Optional) - Host to use as the smtp relay if the summary email is being used. Defaults to localhost.
* mail.smtp.port: (Optional) - Port to use for smtp if the summary email is being used. Defaults to 25.
* cs.swarm.canEnable: (Optional) - Specifies whether or not the XNAT server being tested has backend support for Docker swarm. Defaults to false.
* cs.swarm.timeout: (Optional) - Timing parameter used in Docker Swarm tests. Defaults to 5.
* tomcat.version: (Optional) - Name for the service for tomcat. Used over SSH for tests requiring tomcat restart. Defaults to tomcat7.
* firefox.path*: (Optional) - Path to the firefox binary if pointing to a custom installation location is needed. Defaults to firefox application on the $PATH.


### Additional XNAT servers ###
Some test series (e.g. XSync) require more than one distinct XNAT server. To add a second XNAT server, you would add xnat2.required=true to the configuration properties file, and then specify relevant values for:

* xnat2.main.user
* xnat2.main.password
* xnat2.mainAdmin.user
* xnat2.mainAdmin.password
* xnat2.admin.user
* xnat2.admin.password
* xnat2.version
* xnat2.baseurl
* xnat2.init

As of right now, many of the more advanced features (SSH, DB, etc.) are only available on the primary XNAT.

## JIRA Configuration ##
All fields (except any marked Optional) are required if you wish to use JIRA integration. They may be set as command line arguments or in config/jira.properties.

* jira.user: The username of the JIRA account used to execute tests.
* jira.password: The password of the above account.
* jira.url: URL for JIRA.
* jira.project: Project key for project that contains the tests (e.g. XNAT).
* jira.project.version: Version under which to file the cycle.
* jira.cycleName: (Optional) - Name for cycle to be created. Always includes timestamp added on to the end. Defaults to "XNAT test suite cycle"
* jira.steps: Should screenshots be uploaded for relevant steps? Defaults to false. Relevant only for selenium tests.