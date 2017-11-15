package org.nrg.testing.dicom;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class DicomScopable {

    protected DicomScopable parent;

    public void setParent(DicomScopable parent) {
        this.parent = parent;
    }

    public DicomScopable getParent() {
        return parent;
    }

    public abstract void markChildren();

    public List<DicomScopable> getFullScope(List<DicomScopable> partialList) {
        if (partialList == null) {
            final List<DicomScopable> result = new ArrayList<>();
            result.add(this);
            return getParent().getFullScope(result);
        } else {
            partialList.add(this);
            return getParent().getFullScope(partialList);
        }
    }

    public List<DicomScopable> getFullScope() {
        return getFullScope(null);
    }

    public String getFullScopeStringRepresentation() {
        final List<DicomScopable> scope = getFullScope();
        String partial = ((DicomTag)scope.get(1)).fullHexString() + "\n";

        for (int i = 3; i < scope.size(); i += 3) {
            partial += StringUtils.repeat(" ", 8*(i/3) - 4) + StringUtils.repeat(">", i/3) + " Sequence item #" + ((SequenceItem)scope.get(i)).getSequenceIndex() + "\n";
            partial += StringUtils.repeat(" ", 8*(i/3)    ) + StringUtils.repeat(">", i/3) + " "                + ((DicomTag)scope.get(i + 1)).fullHexString() + "\n";
        }
        return partial;
    }

}
