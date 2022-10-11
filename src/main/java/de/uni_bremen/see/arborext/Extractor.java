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
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

/**
 * Base class for Version Control System extractors.
 */
public abstract class Extractor
{
    protected String repoUrl;

    private int newBranchNr;

    /**
     * @param repository the url to the online repository to extract from.
     */
    public Extractor(final String repository) throws ExtractionError
    {
        this.repoUrl = repository;
        System.out.println("Start cloning...");
        this.cloneRepository();
        System.out.println("Done cloning.");
    }

    /**
     * Recursively go down the commit history and assign a branch number for every commit.
     *
     * @param cmmt the commit to start with. Moving from children to parents.
     */
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

    /**
     * Extract all commits from the repository.
     *
     * @throws ExtractionError if anything went wrong.
     * @return a list of commits.
     */
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

    /**
     * Clone the repository to local hard drive.
     *
     * @throws ExtractionError if anything went wrong.
     */
    abstract protected void cloneRepository() throws ExtractionError;

    /**
     * Get raw commits that only contain metadata and need to be enriched with the important stuff later on.
     *
     * @throws ExtractionError if anything went wrong.
     * @return a list of raw commits.
     */
    abstract protected List<Commit> getRawCommits() throws ExtractionError;

    /**
     * Enrich a commit with contributions.
     *
     * @param commit the commit.
     * @throws ExtractionError if anything went wrong.
     */
    abstract public void enrichWithContributions(Commit commit) throws ExtractionError, NeedToSetBranch;

    /**
     * Usually deletes the temporary clone from the hard drive.
     *
     * @throws IOException if the temporary repository could not be deleted for whatever reason.
     */
    public void tidyUp() throws java.io.IOException
    {
        FileUtils.deleteDirectory(new java.io.File("tmprepo"));
    }
}
