package org.nrg.testing.latex

abstract class LatexDocument {

    public static final String BASE_CONTENT = LatexUtils.loadTemplate('base.tex')
    String currentContent

    String produceSourceDocument() {
        currentContent = BASE_CONTENT
        currentContent = specifyDocumentClass().inject(currentContent)
        currentContent = specifyPackages().inject(currentContent)
        currentContent = specifyPreambleOptions().inject(currentContent)
        currentContent = currentContent.replace('%MAIN_CONTENT%', generateMainContent())
        currentContent
    }

    PreambleOptions specifyPreambleOptions() {
        new PreambleOptions()
    }

    abstract DocumentClass specifyDocumentClass()

    abstract PackageList specifyPackages()

    abstract String generateMainContent()

}
