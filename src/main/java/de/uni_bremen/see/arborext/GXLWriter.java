package de.uni_bremen.see.arborext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;

public class GXLWriter
{
    static private Document docFromCommit(Commit commit, DocumentBuilder builder)
    {
        Document doc = builder.newDocument();
        Element gxlNode = doc.createElement("gxl");
        doc.appendChild(gxlNode);

        gxlNode.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");

        Element graphNode = doc.createElement("graph");
        gxlNode.appendChild(graphNode);
        graphNode.setAttribute("edgeids", "true");
        graphNode.setAttribute("id", "CodeFacts");

        // Developer nodes
        Developer.probeDeveloper(commit.getAuthor());
        for (Developer dev : Developer.getDevelopers()) {
            Element devNode = doc.createElement("node");
            devNode.setAttribute("id", dev.getId());
            Element nodeType = doc.createElement("type");
            nodeType.setAttribute("xlink:href", "Developer");
            Element devName = doc.createElement("attr");
            devName.setAttribute("name", "Developer.Name");
            Element devNameString = doc.createElement("string");
            devNameString.setTextContent(dev.getName());

            devName.appendChild(devNameString);
            devNode.appendChild(nodeType);
            devNode.appendChild(devName);
            graphNode.appendChild(devNode);
        }

        // Files
        for (SourceFile sf : SourceFile.getAllFiles()) {
            Element fileNode = doc.createElement("node");
            fileNode.setAttribute("id", sf.getId());
            Element fileNodeType = doc.createElement("type");
            fileNodeType.setAttribute("xlink:href", "File");

            Element snameAttr = doc.createElement("attr");
            snameAttr.setAttribute("name", "Source.Name");
            Element valAttr = doc.createElement("string");
            valAttr.setTextContent(sf.getNames());
            snameAttr.appendChild(valAttr);

            Element linkageAttr = doc.createElement("attr");
            linkageAttr.setAttribute("name", "Linkage.Name");
            valAttr = doc.createElement("string");
            valAttr.setTextContent(sf.getNames());
            linkageAttr.appendChild(valAttr);

            Element mCoR = doc.createElement("attr");
            mCoR.setAttribute("name", "Metric.Number_Of_Calling_Routines");
            valAttr = doc.createElement("int");
            valAttr.setTextContent("0");
            mCoR.appendChild(valAttr);

            Element mCR = doc.createElement("attr");
            mCR.setAttribute("name", "Metric.Number_Of_Called_Routines");
            valAttr = doc.createElement("int");
            valAttr.setTextContent("0");
            mCR.appendChild(valAttr);

            Element mccabe = doc.createElement("attr");
            mccabe.setAttribute("name", "Metric.McCabe_Complexity");
            valAttr = doc.createElement("int");
            valAttr.setTextContent("1");
            mccabe.appendChild(valAttr);

            Element loc = doc.createElement("attr");
            loc.setAttribute("name", "Metric.Lines.LOC");
            valAttr = doc.createElement("int");
            valAttr.setTextContent(Integer.toString(sf.getLOC()));
            loc.appendChild(valAttr);

            fileNode.appendChild(fileNodeType);
            fileNode.appendChild(snameAttr);
            fileNode.appendChild(linkageAttr);
            fileNode.appendChild(mCoR);
            fileNode.appendChild(mCR);
            fileNode.appendChild(mccabe);
            fileNode.appendChild(loc);
            graphNode.appendChild(fileNode);
        }

        return doc;
    }

    static public void writeCommitsInGXL(List<Commit> commits, Extractor extractor)
        throws ParserConfigurationException, TransformerException, IOException, ExtractionError
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = transFactory.newTransformer();

        // Set transformer options
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.gupro.de/GXL/gxl-1.0.dtd");

        int commitNr = 0;

        for (Commit commit : commits) {
            extractor.enrichWithContributions(commit);

            Document doc = docFromCommit(commit, builder);
            DOMSource source = new DOMSource(doc);

            String filename = String.format("out_%06d.gxl", commitNr++);
            FileOutputStream out = new FileOutputStream(filename);
            StreamResult result = new StreamResult(out);

            transformer.transform(source, result);
        }
    }
}
