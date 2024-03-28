package org.nrg.testing.util

import org.nrg.xnat.pogo.XnatPlugin

import javax.annotation.Nullable

/**
 * Util class for comparing version numbers
 * https://stackoverflow.com/a/11024200
 */
class Version implements Comparable<Version> {

    final private String version

    final String get() {
        return this.version
    }

    Version(final String version) {
        Objects.requireNonNull(version)
        if (!version.matches("[0-9]+(\\.[0-9]+)*")) {
            throw new IllegalArgumentException("Invalid version format")
        }

        this.version = version
    }

    Version(final XnatPlugin plugin) {
        this(plugin.version.split("-")[0]) // convert "1.0.2-SNAPSHOT-xpl" to "1.0.2"
    }

    boolean lessThan(@Nullable final Version that) {
        return this.compareTo(that) < 0
    }

    boolean lessThanOrEqualTo(@Nullable final Version that) {
        return this.compareTo(that) <= 0
    }

    boolean greaterThan(@Nullable final Version that) {
        return this.compareTo(that) > 0
    }

    boolean greaterThanOrEqualTo(@Nullable final Version that) {
        return this.compareTo(that) >= 0
    }

    boolean equals(@Nullable final Version that) {
        return this.compareTo(that) == 0
    }

    @Override
    int compareTo(@Nullable final Version that) {
        if (that == null) {
            return 1
        }

        final String[] thisParts = this.get().split("\\.")
        final String[] thatParts = that.get().split("\\.")
        final int length = Math.max(thisParts.length, thatParts.length)

        for (int i = 0; i < length; i++) {
            final int thisPart
                    = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0

            final int thatPart
                    = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0

            if (thisPart < thatPart) {
                return -1
            }
            if (thisPart > thatPart) {
                return 1
            }
        }
        return 0
    }
}
