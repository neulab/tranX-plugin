package edu.cmu.tranx;

import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.python.util.PythonInterpreter;

import java.util.ArrayList;
import java.util.List;

public class CodeSyntaxHighlighter {
    PythonInterpreter interpreter = new PythonInterpreter();

    public List<String> highlightCodeList(List<String> codeList) {
        interpreter.set("codeList", codeList);
        interpreter.exec("from pygments import highlight\n"
                + "from pygments.lexers import PythonLexer\n"
                + "from pygments.formatters import HtmlFormatter\n"
                + "results = []\n"
                + "for code in codeList:\n"
                + "\tresults.append('<html>' + highlight(code, PythonLexer(), HtmlFormatter(style='bw', noclasses=True)) + '</html>')\n");
        List<String> htmlTextList = interpreter.get("results", List.class);
        for (int i = 0; i < htmlTextList.size(); i++) {
            String htmlText = htmlTextList.get(i);
            Document doc = Jsoup.parseBodyFragment(htmlText);
            for (Element element : doc.select("[style]")) {
                String style = element.attr("style");
                ArrayList<String> splits = new ArrayList<>();
                for (String s : style.split("; ")) {
                    if (s.contains("background:"))
                        continue;
                    splits.add(s);
                }
                element.attr("style", String.join("; ", splits));
            }
            htmlTextList.set(i, doc.toString());
        }
        return htmlTextList;
    }

    @Nullable
    public static CodeSyntaxHighlighter getInstance() {
        return ServiceManager.getService(CodeSyntaxHighlighter.class);
    }

}
