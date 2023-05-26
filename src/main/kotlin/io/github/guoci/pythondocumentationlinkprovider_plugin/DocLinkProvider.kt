package io.github.guoci.pythondocumentationlinkprovider_plugin

import com.google.common.collect.ImmutableMap
import com.intellij.psi.PsiElement

import com.jetbrains.python.documentation.PythonDocumentationLinkProvider
import com.jetbrains.python.documentation.PythonDocumentationMap
import com.jetbrains.python.documentation.PythonDocumentationProvider
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class DocLinkProvider : PythonDocumentationLinkProvider {
    private val nameToWebpageName: Map<String, String> by lazy {
        val b = ImmutableMap.builder<String, String>()
        try {
            BufferedReader(
                InputStreamReader(
                    DocLinkProvider::class.java
                        .getResourceAsStream("pandasNameMapping.tsv"),
                    Charsets.UTF_8
                )
            ).use { inputStream ->
                inputStream.lines().forEach { line ->
                    val kv = line.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    b.put(kv[0], kv[1])
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        b.build()
    }
  private val docSite = mapOf("django" to "https://docs.djangoproject.com/en/stable/",
      "matplotlib" to "https://matplotlib.org/devdocs/",
      "numpy" to "https://numpy.org/doc/stable/",
      "pandas" to "https://pandas.pydata.org/docs/dev/",
      "_pytest" to "https://docs.pytest.org/en/latest/reference/reference.html#",
      "pytorch" to "https://pytorch.org/docs/main/",
      "scipy" to "https://docs.scipy.org/doc/scipy/",
      "sklearn" to "https://scikit-learn.org/dev/",
      "tensorflow" to "https://www.tensorflow.org", "keras" to "https://www.tensorflow.org")

    override fun getExternalDocumentationUrl(element: PsiElement?, originalElement: PsiElement?): String? {
        val qname = PythonDocumentationProvider.getFullQualifiedName(element)

        return if (qname != null && qname.firstComponent in listOf(
                "django",
                "matplotlib",
                "numpy",
                "pandas",
                "_pytest",
                "pytorch",
                "scipy",
                "sklearn",
                "tensorflow", "keras"
            )
        ) {
//        return if (qname != null && qname.firstComponent in listOf("numpy", "scipy") &&
//                !PythonDocumentationMap.getInstance().entries.containsKey(qname.firstComponent)) {
            println("element = [${element}], originalElement = [${originalElement}]")
            val webPage = nameToWebpageName.get(qname.toString())
            if (webPage != null) {
                "${docSite[qname.firstComponent]}$webPage.html"
            } else {
                "${docSite[qname.firstComponent]}"
            }
        } else null

    }
}