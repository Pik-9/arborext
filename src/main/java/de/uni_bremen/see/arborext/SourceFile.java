package de.uni_bremen.see.arborext;

import java.util.List;
import java.util.ArrayList;

public class SourceFile
{
    private int loc;
    private List<String> knownNames;
    private List<Contribution> contributions;

    protected static List<SourceFile> pathToFiles = new ArrayList<SourceFile> ();

    protected SourceFile(final String name)
    {
        this.loc = 0;
        this.knownNames = new ArrayList<String> ();
        this.knownNames.add(name);
        this.contributions = new ArrayList<Contribution> ();
        pathToFiles.add(this);
    }

    public int getLOC()
    {
        return this.loc;
    }

    public List<Contribution> getContributions()
    {
        return this.contributions;
    }

    public boolean goesByThisName(final String filename)
    {
        return this.knownNames.contains(filename);
    }

    public void rename(final String newName)
    {
        this.knownNames.add(newName);
    }

    public void addContribution(Contribution contribution)
    {
        if (contribution.isAddition()) {
            this.loc += contribution.getLastLine() - contribution.getFirstLine() + 1;
        } else {
            this.loc -= contribution.getLastLine() - contribution.getFirstLine() + 1;
        }

        this.contributions.add(contribution);
    }

    static SourceFile getSourceFile(final String filename)
    {
        int index = pathToFiles.indexOf(filename);
        SourceFile ret = null;
        if (index == -1) {
            ret = new SourceFile(filename);
        } else {
            ret = pathToFiles.get(index);
        }

        return ret;
    }
}
