/*
 * org.nrg.selenium.util.IgnoreNullList
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.testing.util;

import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collection;

public class IgnoreNullList<T> extends ArrayList<T> {

    public IgnoreNullList() {
        super();
    }

    @Override
    public boolean add(T t) {
        return t != null && super.add(t);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        boolean changed = false;
        if (collection == null) return false;
        for (T object : collection) {
            if (add(object)) changed = true;
        }
        return changed;
    }

    public String join() {
        return Joiner.on(", ").join(this);
    }

}
