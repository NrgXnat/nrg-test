package org.nrg.testing.annotations;

import org.nrg.testing.xnat.versions.XnatVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AddedIn {
    Class<? extends XnatVersion> value();
}
