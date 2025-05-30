# PyCharm External Documentation

![Downloads](https://img.shields.io/jetbrains/plugin/d/io.github.guoci.PythonDocumentationLinkProvider)
![Rating](https://img.shields.io/jetbrains/plugin/r/rating/io.github.guoci.PythonDocumentationLinkProvider)
![Version](https://img.shields.io/jetbrains/plugin/v/io.github.guoci.PythonDocumentationLinkProvider)

This plugin provides the URLs for html documentation for several popular Python packages.
Press `Shift+F1` to get external documentation of the symbol under the cursor.

Currently supported modules:

- Django
- Matplotlib
- NumPy
- pandas
- pytest
- SciPy
- scikit-learn(sklearn)
- TensorFlow
- PyTorch

**⚠️Important notice⚠️**

In `Settings` :arrow_right: `Tools` :arrow_right: `(Python) External Documentation`, remove URL templates for modules
that you do not use.

Those URL templates will block this plugin from working.
