package org.nrg.testing.file

class FileLocation {
    
    public static final String CURRENT_DIRECTORY = new File('.').getCanonicalPath()
    public static final String RESOURCE_DIRECTORY = makePath(CURRENT_DIRECTORY, 'src', 'test', 'resources')

    static String getDataLocation() {
        makePath(RESOURCE_DIRECTORY, 'data')
    }

    static String getConfigLocation() {
        makePath(RESOURCE_DIRECTORY, 'config')
    }

    static String getResultLocation() {
        makePath(CURRENT_DIRECTORY, 'target')
    }

    static String getTimeLogsLocation() {
        makePath(CURRENT_DIRECTORY, 'timeLogs')
    }

    private static String makePath(String... parts) {
        parts.join(File.separator)
    }
    
}
