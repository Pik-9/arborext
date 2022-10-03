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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;

/**
 * A single commit in the version history.
 */
public class Commit
{
    private String hash;
    private String authorName;
    private Date date;
    private String commitMessage;
    private int branchId;
    private List<String> parentCommits;
    private List<String> childCommits;
    private List<Contribution> contributions;

    static protected HashMap<String, Commit> commitHashes = new HashMap<String, Commit> ();

    /**
     * @param hash the identifier of the commit (a hash in git's case).
     * @param author the author name of the commit.
     * @param date the commit date.
     * @param commitMessage the commit message.
     */
    public Commit(
        final String hash,
        final String author,
        final Date date,
        final String commitMessage
    ) {
        this.hash = hash;
        this.authorName = author;
        this.date = date;
        this.commitMessage = commitMessage;
        this.branchId = 0;
        this.parentCommits = new ArrayList<String> ();
        this.childCommits = new ArrayList<String> ();
        this.contributions = new ArrayList<Contribution> ();

        commitHashes.put(this.hash, this);
    }

    public String getHash()
    {
        return this.hash;
    }

    public String getAuthor()
    {
        return this.authorName;
    }

    public Date getDate()
    {
        return this.date;
    }

    public String getCommitMessage()
    {
        return this.commitMessage;
    }

    public int getBranchId()
    {
        return this.branchId;
    }

    public void setBranchId(final int branchId)
    {
        this.branchId = branchId;
    }

    public boolean isMerge()
    {
        return this.parentCommits.size() > 1;
    }

    public void addParentCommit(final String parentHash)
    {
        if (!parentHash.isEmpty()) {
            this.parentCommits.add(parentHash);
        }
    }

    public void addChildCommit(final String childHash)
    {
        if (!childHash.isEmpty()) {
            this.childCommits.add(childHash);
        }
    }

    public List<Commit> getParents()
    {
        List<Commit> ret = new ArrayList<Commit> ();
        for (String phash : this.parentCommits) {
            ret.add(commitHashes.get(phash));
        }

        return ret;
    }

    public List<Commit> getChildren()
    {
        List<Commit> ret = new ArrayList<Commit> ();
        for (String chash : this.childCommits) {
            ret.add(commitHashes.get(chash));
        }

        return ret;
    }

    public List<Contribution> getContributions()
    {
        return this.contributions;
    }

    public void addContribution(Contribution contribution)
    {
        this.contributions.add(contribution);
    }

    /**
     * Add this commit as a child commit for all its parent commits.
     */
    public void fillChildren()
    {
        for (Commit com : this.getParents()) {
            com.addChildCommit(this.hash);
        }
    }

    public int countParents()
    {
        return this.parentCommits.size();
    }

    public int countChildren()
    {
        return this.childCommits.size();
    }

    @Override
    public String toString()
    {
        String ret = "Commit:  " + this.hash + "\n";
        ret += "Author:  " + this.authorName + "\n";
        ret += "Date:    " + this.date + "\n";
        ret += "Msg:     " + this.commitMessage + "\n";
        ret += "Branch:  " + Integer.toString(this.branchId) + "\n";
        ret += "Parents:";

        for (String phash : this.parentCommits) {
            ret += " " + phash;
        }

        ret += "\nChildren:";

        for (String chash : this.childCommits) {
            ret += " " + chash;
        }

        ret += "\nContributions:\n";

        for (Contribution cont : this.contributions) {
            ret += cont.toString() + "\n";
        }

        return ret;
    }
}
