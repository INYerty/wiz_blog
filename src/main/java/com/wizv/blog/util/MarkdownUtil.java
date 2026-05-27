package com.wizv.blog.util;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.Collections;
import java.util.List;

public class MarkdownUtil {

    private static final List<Extension> extensions = Collections.singletonList(TablesExtension.create());
    private static final Parser parser = Parser.builder().extensions(extensions).build();
    private static final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();

    public static String toHtml(String markdown) {
        if (markdown == null) {
            return "";
        }
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}

