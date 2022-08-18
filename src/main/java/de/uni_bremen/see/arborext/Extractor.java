package de.uni_bremen.see.arborext;

import java.util.List;
import java.util.ArrayList;

public abstract class Extractor
{
    protected String repoUrl;

    private int newBranchNr;

    public Extractor(final String repository) throws ExtractionError
    {
        this.repoUrl = repository;
        this.cloneRepository();
    }

    private void assignBranchId(Commit cmmt)
    {
        List<Commit> parents = cmmt.getParents();
        if (parents.size() == 0) {
            // Initial commit.
            cmmt.setBranchId(this.newBranchNr++);
        } else {
            boolean givenNr = false;
            for (Commit pcom : parents) {
                if (pcom.getBranchId() != 0) {
                    // This is a fork from another branch.
                    // --> New branch id.
                    continue;
                }

                // Recurse down
                this.assignBranchId(pcom);

                if (!givenNr) {
                    // Inherit branch id of first parent.
                    cmmt.setBranchId(pcom.getBranchId());
                    givenNr = true;
                }
            }
            if (!givenNr) {
                // This is a merge commit of two existing branches creating a third one.
                // --> New branch id.
                cmmt.setBranchId(this.newBranchNr++);
            }
        }
    }

    public List<Commit> extractCommits() throws ExtractionError
    {
        List<Commit> coms = this.getRawCommits();

        // Set child commits
        coms.forEach((com) -> com.fillChildren());

        // Get a list of end commits (wihtout any children)
        List<Commit> finalComs = new ArrayList<Commit> ();
        for (Commit com : coms) {
            if (com.countChildren() == 0) {
                finalComs.add(com);
            }
        }

        this.newBranchNr = 1;

        // Sort by date
        coms.sort((ca, cb) -> (ca.getDate().compareTo(cb.getDate())));

        //finalComs.forEach(assignBranchId);
        for (Commit fc : finalComs) {
            this.assignBranchId(fc);
        }

        return coms;
    }

    abstract protected void cloneRepository() throws ExtractionError;
    abstract protected List<Commit> getRawCommits() throws ExtractionError;

    abstract public void tidyUp();
    abstract public void enrichWithContributions(Commit commit) throws ExtractionError;
}
