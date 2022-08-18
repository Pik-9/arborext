package de.uni_bremen.see.arborext;

import java.util.List;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Extractor ext = null;
        List<Commit> commits = null;

        try {
            ext = new GitExtractor("https://codeberg.org/Pik-9/scan2rpdf.git");
            commits = ext.extractCommits();
        } catch (ExtractionError exc) {
            System.err.println("ERROR: " + exc.getMessage());
            System.exit(1);
        }

        for (Commit cmmt : commits) {
            System.out.println("+------------------------+");
            System.out.println(cmmt.toString());
            System.out.println("+------------------------+");
        }

        try {
            ext.tidyUp();
        } catch (IOException exc) {
            System.err.println("ERROR: Could not tidy up:" + exc.getMessage());
            System.exit(1);
        }

        /*
        try {
            ext = new DummyExtractor("blank");
            commits = ext.extractCommits();

            for (Commit cmmt : commits) {
                //ext.enrichWithContributions(cmmt);
            }

            for (Commit cmmt : commits) {
                System.out.println("+------------------------+");
                System.out.println(cmmt.toString());
                System.out.println("+------------------------+");
            }

            ext.tidyUp();
        } catch (Exception exc) {
            System.err.println("ERROR: " + exc.getMessage());
            System.exit(1);
        }

        try {
            GXLWriter.writeCommitsInGXL(commits, ext);
        } catch(ParserConfigurationException exc) {
            System.err.println("Parser error: " + exc.getMessage());
            System.exit(1);
        } catch(TransformerException exc) {
            System.err.println("Transformer error: " + exc.getMessage());
            System.exit(1);
        } catch(IOException exc) {
            System.err.println("IO error: " + exc.getMessage());
            System.exit(1);
        } catch (Exception exc) {
            System.err.println("ERROR: " + exc.getMessage());
            System.exit(1);
        }
        */
    }
}
