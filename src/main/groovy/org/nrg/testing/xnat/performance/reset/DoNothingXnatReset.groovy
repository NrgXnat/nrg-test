package org.nrg.testing.xnat.performance.reset

class DoNothingXnatReset extends FixedScriptXnatReset {

    DoNothingXnatReset() {
        super(false)
    }

    @Override
    List<String> commands() {
        []
    }

}
