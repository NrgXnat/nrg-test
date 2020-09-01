package org.nrg.testing.enums

enum TestBehavior {

    RUN ('run'),
    IGNORE ('ignore'),
    SKIP ('skip')

    final String identifier

    TestBehavior(String identifier) {
        this.identifier = identifier
    }

    static TestBehavior get(String identifier) {
        values().find { it.identifier == identifier }
    }

}