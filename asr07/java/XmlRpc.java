import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Conversão XML-RPC usada pelos programas Java, sem dependências externas. */
final class XmlRpc {
    static final class Call {
        final String method;
        final List<Object> arguments;

        Call(String method, List<Object> arguments) {
            this.method = method;
            this.arguments = arguments;
        }
    }

    static final class Fault extends Exception {
        Fault(String message) {
            super(message);
        }
    }

    private XmlRpc() {}

    static String request(String method, Object... arguments) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append("<methodCall><methodName>").append(escape(method)).append("</methodName><params>");
        for (Object argument : arguments) {
            xml.append("<param>").append(value(argument)).append("</param>");
        }
        return xml.append("</params></methodCall>").toString();
    }

    static String response(Object result) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?><methodResponse><params><param>"
                + value(result) + "</param></params></methodResponse>";
    }

    static String fault(int code, String message) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?><methodResponse><fault><value><struct>"
                + "<member><name>faultCode</name>" + value(code) + "</member>"
                + "<member><name>faultString</name>" + value(message) + "</member>"
                + "</struct></value></fault></methodResponse>";
    }

    static Call readRequest(byte[] xml) throws Exception {
        Element root = parse(xml).getDocumentElement();
        if (!"methodCall".equals(root.getTagName())) {
            throw new IllegalArgumentException("Raiz XML-RPC inválida");
        }
        String method = child(root, "methodName").getTextContent();
        List<Object> arguments = new ArrayList<>();
        Element params = optionalChild(root, "params");
        if (params != null) {
            for (Element param : children(params, "param")) {
                arguments.add(readValue(child(param, "value")));
            }
        }
        return new Call(method, arguments);
    }

    static Object readResponse(byte[] xml) throws Exception {
        Element root = parse(xml).getDocumentElement();
        if (!"methodResponse".equals(root.getTagName())) {
            throw new IllegalArgumentException("Resposta XML-RPC inválida");
        }
        Element fault = optionalChild(root, "fault");
        if (fault != null) {
            Element struct = child(child(fault, "value"), "struct");
            String code = "";
            String message = "";
            for (Element member : children(struct, "member")) {
                String name = child(member, "name").getTextContent();
                Object item = readValue(child(member, "value"));
                if ("faultCode".equals(name)) code = item.toString();
                if ("faultString".equals(name)) message = item.toString();
            }
            throw new Fault("XML-RPC " + code + ": " + message);
        }
        return readValue(child(child(child(root, "params"), "param"), "value"));
    }

    private static Document parse(byte[] xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    }

    private static Object readValue(Element value) {
        Element type = firstElement(value);
        if (type == null) return value.getTextContent(); // <value>texto</value> é XML-RPC válido
        String text = type.getTextContent();
        switch (type.getTagName()) {
            case "string": return text;
            case "double": return Double.parseDouble(text);
            case "int":
            case "i4": return Integer.parseInt(text);
            default: throw new IllegalArgumentException("Tipo XML-RPC não suportado: " + type.getTagName());
        }
    }

    private static String value(Object item) {
        if (item instanceof String) return "<value><string>" + escape((String) item) + "</string></value>";
        if (item instanceof Integer) return "<value><int>" + item + "</int></value>";
        if (item instanceof Double) {
            double number = (Double) item;
            if (!Double.isFinite(number)) throw new IllegalArgumentException("Número não finito");
            return "<value><double>" + number + "</double></value>";
        }
        throw new IllegalArgumentException("Tipo não suportado: " + item.getClass().getName());
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static Element child(Element parent, String name) {
        Element found = optionalChild(parent, name);
        if (found == null) throw new IllegalArgumentException("Elemento XML ausente: " + name);
        return found;
    }

    private static Element optionalChild(Element parent, String name) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element && name.equals(((Element) node).getTagName())) return (Element) node;
        }
        return null;
    }

    private static Element firstElement(Element parent) {
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element) return (Element) node;
        }
        return null;
    }

    private static List<Element> children(Element parent, String name) {
        List<Element> result = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element && name.equals(((Element) node).getTagName())) result.add((Element) node);
        }
        return result;
    }
}
