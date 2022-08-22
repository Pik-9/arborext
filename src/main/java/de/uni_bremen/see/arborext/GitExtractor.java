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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
        // Merge commit don't have any contributions, but change the branch Id of all
        // previous contributions.

        SourceFile.setEverythingOld();

        if (commit.isMerge()) {
            for (Commit parent : commit.getParents()) {
                parent.setBranchId(commit.getBranchId());
            }

            return;
        }

        ProcessBuilder showProc = new ProcessBuilder();
        showProc.command("git", "show", commit.getHash());
        List<String> diffLines = new ArrayList<String> ();

        try {
            showProc.directory(new File("tmprepo"));

            Process proc = showProc.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                diffLines.add(line);
            }

            String errorString = "";
            while ((line = errorReader.readLine()) != null) {
                errorString += line + "\n";
            }

            int exitVal = proc.waitFor();
            if (exitVal != 0) {
                throw new ExtractionError("ERROR while doing `git show`: " + errorString);
            }
        } catch (IOException exc) {
            throw new ExtractionError("ERROR: " + exc.getMessage());
        } catch(InterruptedException exc) {
            throw new ExtractionError("ERROR: `git show` got interrupted: " + exc.getMessage());
        }

        Pattern renameFrom = Pattern.compile("^rename from (?<filegroup>.+)$");
        Pattern renameTo = Pattern.compile("^rename to (?<filegroup>.+)$");
        Pattern origFile = Pattern.compile("^--- (a/)?(?<filegroup>.+)$");
        Pattern newFile = Pattern.compile("^\\+\\+\\+ (b/)?(?<filegroup>.+)$");
        Pattern linesPattern = Pattern.compile("^@@ -(\\d+),(\\d+) \\+(\\d+),(\\d+) @@.*");

        String oldFileName = "";
        String newFileName = "";
        String aFile = "";
        String bFile = "";
        int line_a, nr_a, line_b, nr_b;

        for (String line : diffLines) {
            Matcher rnFromMatcher = renameFrom.matcher(line);
            Matcher rnToMatcher = renameTo.matcher(line);
            Matcher orgFileMatcher = origFile.matcher(line);
            Matcher newFileMatcher = newFile.matcher(line);
            Matcher linesMatcher = linesPattern.matcher(line);

            boolean isFileDeletion = false;


            if (rnFromMatcher.matches()) {
                oldFileName = rnFromMatcher.group("filegroup");
            }

            if (rnToMatcher.matches()) {
                newFileName = rnToMatcher.group("filegroup");
                if (oldFileName.isEmpty()) {
                    throw new ExtractionError("The diff is malformed.");
                }

                SourceFile.getSourceFile(oldFileName).rename(newFileName);
            }

            if (orgFileMatcher.matches()) {
                aFile = orgFileMatcher.group("filegroup");
            }

            if (newFileMatcher.matches()) {
                bFile = newFileMatcher.group("filegroup");

                if (bFile.equals("/dev/null")) {
                    isFileDeletion = true;
                    new Contribution(
                        0,
                        SourceFile.getSourceFile(aFile).getLOC(),
                        false,
                        commit,
                        aFile
                    );
                }
            }

            if (linesMatcher.matches()) {
                line_a = Integer.parseInt(linesMatcher.group(1));
                nr_a = Integer.parseInt(linesMatcher.group(2));
                line_b = Integer.parseInt(linesMatcher.group(3));
                nr_b = Integer.parseInt(linesMatcher.group(4));

                if (bFile.isEmpty()) {
                    throw new ExtractionError("The diff is malformed.");
                }

                if (!isFileDeletion) {
                    int delta = nr_b > nr_a ? nr_b - nr_a : nr_a - nr_b;
                    new Contribution(
                        line_b,
                        line_b + delta,
                        delta >= 0,
                        commit,
                        bFile.equals("/dev/null") ? aFile : bFile
                    );
                }
            }
        }
    }
}
