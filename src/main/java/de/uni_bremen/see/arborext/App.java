package de.uni_bremen.see.arborext;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try {
            Extractor ext = new DummyExtractor("blank");
            List<Commit> commits = ext.extractCommits();

            for (Commit cmmt : commits) {
                ext.enrichWithContributions(cmmt);
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
    }
}
