package de.uni_bremen.see.arborext;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class Commit
{
    private String hash;
    private String authorName;
    private Date date;
    private String commitMessage;
    private int branchId;
    private List<Commit> parentCommits;
    private List<Commit> childCommits;

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
        this.parentCommits = new ArrayList<Commit> ();
        this.childCommits = new ArrayList<Commit> ();
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

    public boolean isMerge()
    {
        return this.parentCommits.size() > 1;
    }
}
