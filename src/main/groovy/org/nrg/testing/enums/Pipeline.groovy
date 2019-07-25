package org.nrg.testing.enums

enum Pipeline {
    FS_5_3 ('Freesurfer_5.3'),
    PUP ('PETUnifiedPipeline'),
    FBIRN ('fbirn_phantom_64'),
    BOLD ('4dfpBoldPreProcessing', 'GenericBoldPreprocessing'),
    MR_TO_ATLAS ('RegisterMRToAtlas'),
    WMH ('WhiteMatterHyperintensitiesSegmentation'),
    ILP ('Launch ILP', 'ILP'),
    CUSTOM (null)

    private final String launchName
    private final String historyName

    Pipeline(String launchName, String historyName) {
        this.launchName = launchName
        this.historyName = historyName
    }

    Pipeline(String name) {
        launchName = name
        historyName = name
    }

    String getLaunchName() {
        launchName
    }

    String getHistoryName() {
        historyName
    }

}
