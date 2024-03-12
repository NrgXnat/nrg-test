package org.nrg.testing.latex

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import org.apache.commons.math3.util.Pair
import org.nrg.testing.xnat.performance.regression.PolynomialRegression
import org.nrg.testing.xnat.performance.regression.ReportableRegression

@Builder(builderStrategy = SimpleStrategy, prefix = '')
class ScatterPlotDataset implements LatexComponent {

    String label
    String color = 'black'
    String marker = '*'
    String markerSize = '1.5 pt'
    Long timestamp
    List<Pair<String, String>> coordinates
    List<RegressionCurve> regressions = []
    private static final String BASE_REGRESSION_CHART = LatexUtils.loadTemplate('regression_curve.tex')

    @Override
    String inject(String baseContent) {
        LatexUtils.replaceAndMaintainIndentFromList(
                baseContent,
                '%REGRESSIONS%',
                regressions.collect { regression ->
                    regression.inject(BASE_REGRESSION_CHART)
                }
        )
                .replace('%PLOT_COLOR%', color)
                .replace('%PLOT_MARKER%', marker)
                .replace('%PLOT_SIZE%', markerSize)
                .replace('%PLOT_LABEL%', label)
                .replace('%PLOT_COORDINATES%', coordinates.collect { coordinate ->
                    "(${coordinate.key},${coordinate.value})"
                }.join(''))
    }

}
