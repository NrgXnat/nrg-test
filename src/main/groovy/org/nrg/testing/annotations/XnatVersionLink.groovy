package org.nrg.testing.annotations

import org.nrg.xnat.versions.XnatVersion

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface XnatVersionLink {
    Class<? extends XnatVersion>[] xnatVersions()
    String mappedValue()
}
