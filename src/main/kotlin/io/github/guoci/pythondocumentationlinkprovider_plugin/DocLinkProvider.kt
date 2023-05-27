package io.github.guoci.pythondocumentationlinkprovider_plugin

import com.google.common.collect.ImmutableMap
import com.intellij.psi.PsiElement
import com.jetbrains.python.documentation.PythonDocumentationLinkProvider
import com.jetbrains.python.documentation.PythonDocumentationProvider
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.resolve.QualifiedNameFinder
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class DocLinkProvider : PythonDocumentationLinkProvider {
    private val docsMapping: Map<String, String> by lazy {
        val b = ImmutableMap.builder<String, String>()
        try {
            BufferedReader(
                InputStreamReader(
                    DocLinkProvider::class.java
                        .getResourceAsStream("/allNameMapping.tsv")!!,
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

        b.buildKeepingLast() // TODO change to b.buildOrThrow()
    }

    //  private val docSite = mapOf("django" to "https://docs.djangoproject.com/en/stable/",
//      "matplotlib" to "https://matplotlib.org/devdocs/",
//      "numpy" to "https://numpy.org/doc/stable/",
//      "pandas" to "https://pandas.pydata.org/docs/dev/",
//      "_pytest" to "https://docs.pytest.org/en/latest/reference/reference.html#",
//      "pytorch" to "https://pytorch.org/docs/main/",
//      "scipy" to "https://docs.scipy.org/doc/scipy/",
//      "sklearn" to "https://scikit-learn.org/dev/",
//      "tensorflow" to "https://www.tensorflow.org", "keras" to "https://www.tensorflow.org"
//  )
    private val numpy_doc_site = "https://numpy.org/doc/stable"
    private val scipy_doc_site = "https://docs.scipy.org/doc/scipy"
    private val pandas_doc_site = "https://pandas.pydata.org/docs/dev"
    private val django_doc_site = "https://docs.djangoproject.com/en/stable"
    private val matplotlib_doc_site = "https://matplotlib.org/devdocs"
    private val tensorflow_doc_site = "https://www.tensorflow.org"

    private fun getQname(element: PsiElement?, originalElement: PsiElement?): String {
        val moduleQName = QualifiedNameFinder.findCanonicalImportPath(element!!, originalElement)
        val namedElement = PythonDocumentationProvider.getNamedElement(element)
        val qName = StringBuilder(moduleQName.toString()).append(".")
        if (namedElement is PyFunction && namedElement.containingClass != null) {
            qName.append(namedElement.containingClass!!.name).append(".")
        }
        qName.append(namedElement?.name)
        return qName.toString()
    }

    override fun getExternalDocumentationUrl(element: PsiElement?, originalElement: PsiElement?): String? {
//        val qname = PythonDocumentationProvider.getFullQualifiedName(element)?.toString()
        val qname = getQname(element, originalElement)
        println("element = [${element}], originalElement = [${originalElement}]")
        println("qname = $qname")
        return if (qname == null)
            null
        else {
            val path = docsMapping[qname]
            when {
                qname.startsWith("numpy.") -> "$numpy_doc_site/$path"
                qname.startsWith("scipy.") -> "$scipy_doc_site/$path"
                qname.startsWith("pandas.") -> {
                    if (path == null) {
                        val name = qname.split(".").last()
                        if (name == "iloc" || name == "loc" || name == "iat")
                            "$pandas_doc_site/reference/api/pandas.DataFrame.$name.html"
                        else
                            "https://pandas.pydata.org/docs/search.html?q=$name"
                    } else
                        "$pandas_doc_site/$path"
                }

                qname.startsWith("django.") -> "$django_doc_site/$path"
                qname.startsWith("matplotlib.") -> "$matplotlib_doc_site/$path"

                qname.startsWith("tensorflow.") -> {
                    val qname2 = if (qname.startsWith("tensorflow.python."))
                        "tensorflow." + qname.substring("tensorflow.python.".length) else qname
                    val path2 = docsMapping[qname2]
                    val pathFinal = path ?: path2
                    if (pathFinal == null)
                        "$tensorflow_doc_site/s/results?q=${java.net.URLEncoder.encode(qname2, "UTF8")}"
                    else
                        "$tensorflow_doc_site/api_docs/python/$pathFinal"
                }

                qname.startsWith("keras.") -> {
                    val path2 = docsMapping["tensorflow.$qname"]
                    "$tensorflow_doc_site/api_docs/python/${path2 ?: path}"
                }

                qname.startsWith("_pytest.") -> "https://docs.pytest.org/en/latest/reference/reference.html#$path"

                qname.startsWith("torch.") -> {
                    val qname2 = qname.replace("_TensorBase._TensorBase", "_TensorBase")
                    val qname3 =
                        if (qname2.startsWith("torch._C._TensorBase.")) "torch.Tensor." + qname2.substring("torch._C._TensorBase.".length)
                        else qname2
                    println("qname3 = ${qname3}")
                    "https://pytorch.org/docs/main/${docsMapping[qname3]}"
                }

                qname.startsWith("sklearn.") -> "https://scikit-learn.org/dev/$path"

                else -> null
            }

        }
    }

}
