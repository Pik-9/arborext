package de.uni_bremen.see.arborext;

import java.lang.Process;
import java.lang.ProcessBuilder;
import java.lang.InterruptedException;
import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;

public class GitExtractor extends Extractor
{
    public GitExtractor(final String repository) throws ExtractionError
    {
        super(repository);

        ProcessBuilder version = new ProcessBuilder();
        version.command("git", "--version");

        try {
            Process proc = version.start();
            int exitVal = proc.waitFor();
            if (exitVal != 0) {
                throw new ExtractionError("Git is not installed.");
            }
        } catch(InterruptedException exc) {
            throw new ExtractionError("Git is not installed.");
        } catch(IOException exc) {
            throw new ExtractionError("Git is not installed.");
        }
    }

    @Override
    protected void cloneRepository() throws ExtractionError
    {
        ProcessBuilder cloning = new ProcessBuilder();
        cloning.command("git", "clone", this.repoUrl, "tmprepo");

        try {
            Process proc = cloning.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = proc.waitFor();
            if (exitVal != 0) {
                throw new ExtractionError("Could not clone:\n" + output);
            }
        } catch (IOException exc) {
            throw new ExtractionError("ERROR: " + exc.getMessage());
        } catch(InterruptedException exc) {
            throw new ExtractionError("ERROR: Cloning got interrupted: " + exc.getMessage());
        }
    }

    @Override
    protected List<Commit> getRawCommits() throws ExtractionError
    {
        ProcessBuilder log = new ProcessBuilder();
        log.command("git", "log", "--all", "--pretty=%H;%an;%ct;%P;%s");
        List<String> logLines = new ArrayList<String> ();
        List<Commit> ret = new ArrayList<Commit> ();

        try {
            log.directory(new File("tmprepo"));

            Process proc = log.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                logLines.add(line);
            }

            String errorString = "";
            while ((line = errorReader.readLine()) != null) {
                errorString += line + "\n";
            }

            int exitVal = proc.waitFor();
            if (exitVal != 0) {
                throw new ExtractionError("ERROR while doing `git log`: " + errorString);
            }
        } catch (IOException exc) {
            throw new ExtractionError("ERROR: " + exc.getMessage());
        } catch(InterruptedException exc) {
            throw new ExtractionError("ERROR: Log got interrupted: " + exc.getMessage());
        }

        for (String entry : logLines) {
            String[] parts = entry.split(";", 5);
            String[] parents = parts[3].split(" ");

            Commit cmmt = new Commit(
                parts[0],
                parts[1],
                new Date(Long.parseLong(parts[2]) * 1000),
                parts[4]
            );

            for (String phash : parents) {
                cmmt.addParentCommit(phash);
            }

            ret.add(cmmt);
        }

        return ret;
    }

    @Override
    public void enrichWithContributions(Commit commit) throws ExtractionError
    {
    }
}
