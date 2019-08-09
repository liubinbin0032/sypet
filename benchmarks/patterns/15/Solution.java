public class Solution {

    public static Document createDom(String name) throws Throwable {
    	DocumentBuilderFactory v1 = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = v1.newDocumentBuilder();
		Document doc = db.newDocument();
		org.w3c.dom.Element root = doc.createElement(name);
		doc.appendChild(root);
		return doc;
	}
}