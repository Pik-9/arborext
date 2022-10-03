/**
 * Copyright (C) 2022 Daniel Steinhauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.uni_bremen.see.arborext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Can write a commit history into GXL files.
 */
public class GXLWriter
{
    static private Element createAttrNode(Document doc, final String name, final String type, final String value)
    {
        Element node = doc.createElement("attr");
        node.setAttribute("name", name);
        Element valAttr = doc.createElement(type);
        valAttr.setTextContent(value);
        node.appendChild(valAttr);

        return node;
    }

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

            devNode.appendChild(nodeType);
            devNode.appendChild(createAttrNode(doc, "Developer.Name", "string", dev.getName()));
            graphNode.appendChild(devNode);
        }

        // The contribution nodes should be added after the file nodes.
        List<Element> contributionNodes = new ArrayList<Element> ();

        // The edges should be added after the nodes
        List<Element> edges = new ArrayList<Element> ();

        int newEdgeId = 1;

        // Files
        for (SourceFile sf : SourceFile.getAllFiles()) {
            Element fileNode = doc.createElement("node");
            fileNode.setAttribute("id", sf.getId());
            Element fileNodeType = doc.createElement("type");
            fileNodeType.setAttribute("xlink:href", "File");

            fileNode.appendChild(createAttrNode(doc, "Source.Name", "string", sf.getNames()));
            fileNode.appendChild(createAttrNode(doc, "Linkage.Name", "string", sf.getNames()));
            fileNode.appendChild(createAttrNode(doc, "Metric.Number_Of_Calling_Routines", "int", "0"));
            fileNode.appendChild(createAttrNode(doc, "Metric.Number_Of_Called_Routines", "int", "0"));
            fileNode.appendChild(createAttrNode(doc, "Metric.McCabe_Complexity", "int", "1"));
            fileNode.appendChild(createAttrNode(doc, "Metric.Lines.LOC", "int", Integer.toString(sf.getLOC())));
            graphNode.appendChild(fileNode);

            // Contributions
            for (Contribution cont : sf.getContributions()) {
                Element cNode = doc.createElement("node");
                cNode.setAttribute("id", cont.getId());
                Element contNodeType = doc.createElement("type");
                contNodeType.setAttribute("xlink:href", "Contribution");

                cNode.appendChild(createAttrNode(doc, "Metric.Lines.FirstLine", "int", Integer.toString(cont.getFirstLine())));
                cNode.appendChild(createAttrNode(doc, "Metric.Lines.LastLine", "int", Integer.toString(cont.getLastLine())));
                cNode.appendChild(createAttrNode(doc, "Metric.Lines.LOC", "int", Integer.toString(cont.getLOC())));
                cNode.appendChild(createAttrNode(doc, "Contribution.FileId", "string", sf.getId()));
                cNode.appendChild(createAttrNode(doc, "Info.CommitId", "string", cont.getCommit().getHash()));
                cNode.appendChild(createAttrNode(doc, "Info.CommitAuthor", "string", cont.getCommit().getAuthor()));
                cNode.appendChild(createAttrNode(doc, "Info.CommitMessage", "string", cont.getCommit().getCommitMessage()));
                cNode.appendChild(createAttrNode(doc, "Info.CommitTimestamp", "string", cont.getCommit().getDate().toString()));
                cNode.appendChild(createAttrNode(doc, "Info.Branch", "int", Integer.toString(cont.getBranchId())));

                contributionNodes.add(cNode);

                // Edge for contributions that were added with this commit.
                if (cont.isNew()) {
                    Element cEdge = doc.createElement("edge");
                    cEdge.setAttribute("id", "E" + Integer.toString(newEdgeId++));
                    cEdge.setAttribute("from", Developer.probeDeveloper(cont.getCommit().getAuthor()).getId());
                    cEdge.setAttribute("to", cont.getId());
                    Element cEdgeType = doc.createElement("type");
                    cEdgeType.setAttribute("xlink:href", "Call");
                    cEdge.appendChild(cEdgeType);
                    edges.add(cEdge);
                }
            }
        }

        for (Element ele : contributionNodes) {
            graphNode.appendChild(ele);
        }

        for (Element ele : edges) {
            graphNode.appendChild(ele);
        }

        return doc;
    }

    /**
     * Write all provided commits into separate GXL files for each commit.
     *
     * @param commits list of commits.
     * @param extractor the extractor to extract data from the VCS.
     *
     * @throws ParserConfigurationException if something went wrong with the parser.
     * @throws TransformerException if something went wrong with the transformer.
     * @throws IOException if the files could not be written to or the repository could not be deleted.
     * @throws ExtractionError if something went wrong with the extraction.
     */
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
