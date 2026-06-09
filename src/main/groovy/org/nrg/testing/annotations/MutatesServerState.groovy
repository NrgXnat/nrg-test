package org.nrg.testing.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Inherited
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Marks a test class that mutates server-global XNAT state: site config, the site-wide anon script,
 * DICOM routing rules, site-level custom forms, role grants, etc. When parallel class execution is
 * enabled (xnat.parallel.classes > 1), annotated classes are serialized against the entire suite
 * (they take the write side of a suite-wide read/write lock), while unannotated classes may run
 * concurrently with each other against the same XNAT.
 *
 * Classes whose {@link TestRequires} annotation declares openXnat or closedXnat (at class or method
 * level) are treated as if they carried this annotation, since the framework toggles site config on
 * their behalf.
 *
 * The annotation is inherited, so annotating a base test class covers all of its subclasses.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface MutatesServerState {
}
