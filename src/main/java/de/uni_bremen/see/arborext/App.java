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

import java.util.List;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.cli.*;

/**
 * The main application.
 */
public class App 
{
    public static void main( String[] args )
    {
        Extractor ext = null;
        List<Commit> commits = null;
        String path = "";

        Options opt = new Options ();
        opt.addOption ("r", "repository", true, "The repository URL to inspect.");
        opt.addOption ("p", "proto", true, "The VCS to use. Possible values are git, hg, svn, dummy");
        opt.addOption ("h", "help", false, "Show this help dialog.");

        HelpFormatter help = new HelpFormatter ();
        CommandLineParser parser = new DefaultParser ();
        CommandLine cmd = null;
        try {
            cmd = parser.parse (opt, args);
        } catch (ParseException exc) {
            System.out.println ("ERROR: " + exc);
            System.exit (1);
        }

        if (cmd.hasOption ("h")) {
            help.printHelp ("arborext", opt);
            System.exit (0);
        }

        if (cmd.hasOption ("r")) {
            path = cmd.getOptionValue ("r");
        } else {
            System.out.println ("You need to specify a repository with -r.");
            help.printHelp ("arborext", opt);
            System.exit (1);
        }

        String vcsType = cmd.hasOption ("p") ? cmd.getOptionValue ("p") : "git";
        try {
            if (vcsType.equals("git")) {
                ext = new GitExtractor(path);
            } else if (vcsType.equals("svn")) {
                System.out.println("SVN is currentyly not supported.");
                System.exit(0);
            } else if (vcsType.equals("hg")) {
                System.out.println("Mercurial is currentyly not supported.");
                System.exit(0);
            } else if (vcsType.equals("dummy")) {
                ext = new DummyExtractor();
            } else {
                System.err.println("ERROR: Unknwon protocol: " + vcsType);
                System.exit(1);
            }
        } catch (ExtractionError exc) {
            System.err.println("ERROR: " + exc.getMessage());
            System.exit(1);
        }

        try {
            commits = ext.extractCommits();
        } catch (ExtractionError exc) {
            System.err.println("ERROR: " + exc.getMessage());
            System.exit(1);
        }

        try {
            System.out.println("Writing to GXL files...");
            GXLWriter.writeCommitsInGXL(commits, ext);
            System.out.println("Done.");
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

        try {
            System.out.println("All done. Tidying up...");
            ext.tidyUp();
        } catch (IOException exc) {
            System.err.println("ERROR: Could not tidy up:" + exc.getMessage());
            System.exit(1);
        }
    }
}
