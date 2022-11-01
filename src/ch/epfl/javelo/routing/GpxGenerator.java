package ch.epfl.javelo.routing;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

public final class GpxGenerator {

    /**
     * prend en arguments un itinéraire et le profil de cet itinéraire et retourne le document GPX correspondant
     *
     * @param itin    un itinéraire
     * @param profile le profil de cet itinéraire
     */
    public static Document createGpx(Route itin, ElevationProfile profile) {
        Document doc = newDocument(); // voir plus bas

        Element root = doc
                .createElementNS("http://www.topografix.com/GPX/1/1",
                        "gpx");
        doc.appendChild(root);

        root.setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                "http://www.topografix.com/GPX/1/1 "
                        + "http://www.topografix.com/GPX/1/1/gpx.xsd");
        root.setAttribute("version", "1.1");
        root.setAttribute("creator", "JaVelo");

        Element metadata = doc.createElement("metadata");
        root.appendChild(metadata);

        Element name = doc.createElement("name");
        metadata.appendChild(name);
        name.setTextContent("Route JaVelo");

        Element rte = doc.createElement("rte");
        root.appendChild(rte);

        double pos = 0;
        for (Edge edge : itin.edges()) {
            if (edge.equals(itin.edges().get(0))) {
                Edge zero = itin.edges().get(0);
                gpx(doc, rte, profile, zero, pos, true);
            }
            pos += edge.length();
            gpx(doc, rte, profile, edge, pos, false);
        }
        return doc;
    }

    private static void gpx(Document doc, Element rte, ElevationProfile profile, Edge edge, double pos, boolean from) {
        Element rtept = doc.createElement("rtept");
        rte.appendChild(rtept);
        if (from) {
            rtept.setAttribute("lon", String.format(Locale.ROOT, "%.5f", Math.toDegrees(edge.fromPoint().lon())));
            rtept.setAttribute("lat", String.format(Locale.ROOT, "%.5f", Math.toDegrees(edge.fromPoint().lat())));
        } else {
            rtept.setAttribute("lon", String.format(Locale.ROOT, "%.5f", Math.toDegrees(edge.toPoint().lon())));
            rtept.setAttribute("lat", String.format(Locale.ROOT, "%.5f", Math.toDegrees(edge.toPoint().lat())));
        }
        Element ele = doc.createElement("ele");
        rtept.appendChild(ele);
        ele.setTextContent(String.format(Locale.ROOT, "%.2f", profile.elevationAt(pos)));
    }

    private static Document newDocument() {
        try {
            return DocumentBuilderFactory
                    .newDefaultInstance()
                    .newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException e) {
            throw new Error(e); // Should never happen
        }
    }

    /**
     * écrit le document GPX correspondant dans le fichier, ou lève IOException en cas d'erreur d'entrée/sortie
     *
     * @param name    un nom de fichier
     * @param itin    un itinéraire
     * @param profile le profil de cet itinéraire
     */
    public static void writeGpx(String name, Route itin, ElevationProfile profile) throws IOException {
        Document doc = createGpx(itin, profile);
        Writer w = new FileWriter(name);
        try {
            Transformer transformer = TransformerFactory
                    .newDefaultInstance()
                    .newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc),
                    new StreamResult(w));
        } catch (TransformerException e) {
            throw new Error(e); // Should never happen
        }
        w.close();
    }
}