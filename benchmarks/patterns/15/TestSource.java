public static boolean test() throws Throwable {
    java.lang.String name = "Language";
    org.w3c.dom.Document doc = createDom(name);

    return (doc.getDocumentElement().getTagName().equals(name));
}
