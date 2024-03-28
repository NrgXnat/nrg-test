package org.nrg.testing.xnat.performance.charting

import org.nrg.testing.latex.Bar
import org.nrg.testing.latex.BarGraph
import org.nrg.testing.latex.BarWithColorDimension
import org.nrg.testing.xnat.performance.actions.ErrorProneAggregatableAction
import org.nrg.testing.xnat.performance.persistence.ErrorProneAggregatableRecord

class ErrorProneActionTimeCharter extends GenericBarGraphCharter<ErrorProneAggregatableAction, ErrorProneAggregatableRecord> {

    @Override
    BarGraph getBaseBarGraph() {
        new BarGraph().additionalPlotOptions([
                'colorbar',
                'colorbar style = {xshift = 1cm, title = Success rate}',
                'colormap={successcolormap}{rgb255(0)=(226, 220, 245) rgb255(1000)=(76, 62, 118)}',
                'point meta = explicit'
        ])
    }

    @Override
    Bar generateBarFrom(ErrorProneAggregatableRecord record, List<ErrorProneAggregatableRecord> allRecords) {
        // remap [minimum success count, maximum success count] to [0, 1000]
        final int min = allRecords*.successCount.min()
        final int max = allRecords*.successCount.max()
        final BigDecimal colorMapping = 1000 * (record.successCount - min)/(max - min)

        new BarWithColorDimension()
                .explicitPointMeta(record.successRateAsPercent())
                .colorMapValue(colorMapping.round(1).toPlainString())
                .label(record.xnatVersion)
                .value(convertAndDisplay(record.overallTimeInMillis))
    }

}
