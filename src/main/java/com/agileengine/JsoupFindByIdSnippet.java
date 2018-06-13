package com.agileengine;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsoupFindByIdSnippet {

    private static Logger LOGGER = LoggerFactory.getLogger(JsoupFindByIdSnippet.class);

    private static String CHARSET_NAME = "utf8";

    public static void main(String[] args) throws IOException {

        String originalDoc = args[0];
        String diffDoc = args[1];
        String targetElementId = "make-everything-ok-button";

        Optional<Element> buttonOpt = findElementById(new File(originalDoc), targetElementId);

        Optional<String> stringifiedAttributesOpt = buttonOpt.map(button ->
                button.attributes().asList().stream()
                        .map(attr -> attr.getKey() + " = " + attr.getValue())
                        .collect(Collectors.joining(", "))
        );


        stringifiedAttributesOpt.ifPresent(attrs -> LOGGER.info("Target element attrs: [{}]", attrs));


        Document doc1 = Jsoup.parse(
                new File(diffDoc),
                CHARSET_NAME,
                new File(diffDoc).getAbsolutePath()
        );
        findDiffElementById(doc1,buttonOpt.get());
        findDiffElementByTitle(doc1,buttonOpt.get());
        findDiffElementByClass(doc1,buttonOpt.get());



    }

    private static Optional<Element> findElementById(File htmlFile, String targetElementId) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            return Optional.of(doc.getElementById(targetElementId));

        } catch (IOException e) {
            LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

    private static void findDiffElementById(Document diffDocument, Element originalElement) {
        Element elementById = diffDocument.getElementById(originalElement.id());
        if(null != elementById) {
            LOGGER.info("Path to the matched element: {}",getXpath(elementById));
        }
    }

    private static void findDiffElementByClass(Document diffDocuement, Element originalElement) {
        originalElement.classNames().forEach(className -> {
            Elements elementsByClass = diffDocuement.getElementsByClass(className);
            if(null != elementsByClass) {
                elementsByClass.forEach(element-> {
                    if(element.attributes().get("title").equalsIgnoreCase(originalElement.attr("title"))) {
                        LOGGER.info("Path to the matched by class element: {}, attrs {}", getXpath(element), getElementAttrs(element));
                    }
                });
            }
        });
    }

    private static void findDiffElementByTitle(Document diffDocument, Element originalElement) {
        String title = originalElement.attributes().get("title");
        if(null != title) {
            Elements elementsByAttribute = diffDocument.getElementsByAttribute(title);
            if(null != elementsByAttribute) {
                elementsByAttribute.forEach(element -> {
                    LOGGER.info("Path to the matched by title element: {}", getXpath(element));
                });
            }
        }
    }

    private static String getXpath(Element htmlElement) {
        StringBuilder absPath=new StringBuilder();
        Elements parents = htmlElement.parents();

        for (int j = parents.size()-1; j >= 0; j--) {
            Element element = parents.get(j);
            absPath.append("/");
            absPath.append(element.tagName());
            absPath.append("[");
            absPath.append(element.siblingIndex());
            absPath.append("]");
        }
        return absPath.toString();
    }

    private static String getElementAttrs(Element element) {
        return element.attributes().asList().stream()
                .map(attr -> attr.getKey() + " = " + attr.getValue())
                .collect(Collectors.joining(", "));
    }

}