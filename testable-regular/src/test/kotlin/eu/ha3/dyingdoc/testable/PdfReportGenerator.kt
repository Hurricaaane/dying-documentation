package eu.ha3.dyingdoc.testable

import gutenberg.itext.FontModifier
import gutenberg.itext.Styles
import gutenberg.itext.model.Markdown
import tzatziki.analysis.exec.gson.JsonIO
import tzatziki.analysis.exec.model.FeatureExec
import tzatziki.pdf.support.Configuration
import tzatziki.pdf.support.DefaultPdfReportBuilder
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZonedDateTime


/**
 * (Default template)
 * Created on 2018-03-17
 *
 * @author Ha3
 */
object PdfReportGenerator {
    private val BASE_PATH = Paths.get("testable-regular")

    @JvmStatic
    fun main(args: Array<String>) {
        val cucumberReportPath = BASE_PATH.resolve("target/tzk/exec.json")
        val outputPath = BASE_PATH.resolve("target/report.pdf")
        val imagesPath = BASE_PATH.resolve("src/main/kotlin/test/resources/tzk/images")
        val introResource = "/tzk/intro.md"


        val imagesLocation = imagesPath.toUri().toString()
        val executions = loadExec(cucumberReportPath.toFile())

        Files.deleteIfExists(outputPath)

        DefaultPdfReportBuilder()
            .using(Configuration()
                .displayFeatureTags(true)
                .displayScenarioTags(true)
                .declareProperty("imageDir", imagesLocation)
                .adjustFont(Styles.TABLE_HEADER_FONT, FontModifier().size(10.0f))
            )
            .title("Dying Documentation")
            .subTitle("Analysis report, generated on " + ZonedDateTime.now())
            .markup(Markdown.fromUTF8Resource(introResource))
            .features(executions)
            .sampleSteps()
            .generate(outputPath.toFile())
    }

    @Throws(IOException::class)
    private fun loadExec(file: File): List<FeatureExec> {
        var kin: InputStream? = null
        try {
            kin = FileInputStream(file)
            return JsonIO().load(kin)

        } finally {
            try {
                kin?.close()
            } finally {
            }
        }
    }
}