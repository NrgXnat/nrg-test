package org.nrg.testing.latex

abstract class StandaloneTikzpicture<T extends StandaloneTikzpicture> extends LatexDocument {

    String title
    String xlabel
    String ylabel
    public static final String BASE_TIKZPICTURE = LatexUtils.loadTemplate('tikzpicture.tex')

    @Override
    DocumentClass specifyDocumentClass() {
        new DocumentClass().options(['margin=5mm', 'varwidth']).className('standalone')
    }

    @Override
    PreambleOptions specifyPreambleOptions() {
        new PreambleOptions().additionalLines(['\\pgfplotsset{compat=1.18}'])
    }

    @Override
    PackageList specifyPackages() {
        new PackageList().packages([
                new LatexPackage().packageName('xcolor').options(['dvipsnames']),
                new LatexPackage().packageName('pgfplots'),
                new LatexPackage().packageName('tikz')
        ])
    }

    abstract String generatePlot()

    String generateExplanation() {
        ''
    }

    T title(String title) {
        setTitle(title)
        this as T
    }

    T xlabel(String xlabel) {
        setXlabel(xlabel)
        this as T
    }

    T ylabel(String ylabel) {
        setYlabel(ylabel)
        this as T
    }

    @Override
    String generateMainContent() {
        LatexUtils.replaceAndMaintainIndent(BASE_TIKZPICTURE, '%TIKZPICTURE%', generatePlot())
                .replace('%CHART_TITLE%', title)
                .replace('%XLABEL%', xlabel)
                .replace('%YLABEL%', ylabel)
                .replace('%CHART_EXPLANATION%', generateExplanation())
    }

}
