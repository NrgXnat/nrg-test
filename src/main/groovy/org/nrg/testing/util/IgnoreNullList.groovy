package org.nrg.testing.util

class IgnoreNullList<T> extends ArrayList<T> {

    IgnoreNullList() {
        super()
    }

    @Override
    boolean add(T t) {
        t != null && super.add(t)
    }

    @Override
    boolean addAll(Collection<? extends T> collection) {
        if (collection == null) {
            false
        } else {
            boolean changed = false
            collection.each { object ->
                if (add(object)) changed = true
            }
            changed
        }
    }

    String join() {
        this.join(', ')
    }

}
