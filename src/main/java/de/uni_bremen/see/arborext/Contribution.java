package de.uni_bremen.see.arborext;

public class Contribution
{
    private int id;
    private int firstLine;
    private int lastLine;
    private boolean addition;
    private boolean newlyCreated;
    private Commit commit;
    private SourceFile sfile;

    static protected int newId = 1;

    public Contribution(
        final int firstLine,
        final int lastLine,
        final boolean isAddition,
        final Commit commit,
        final String sourceFilePath
    ) {
        this.id = newId++;
        this.firstLine = firstLine;
        this.lastLine = lastLine;
        this.addition = isAddition;
        this.newlyCreated = true;
        this.commit = commit;

        this.sfile = SourceFile.getSourceFile(sourceFilePath);
        sfile.addContribution(this);

        this.commit.addContribution(this);
    }

    public String getId()
    {
        return "C" + Integer.toString(this.id);
    }

    public int getFirstLine()
    {
        return this.firstLine;
    }

    public int getLastLine()
    {
        return this.lastLine;
    }

    public int getLOC()
    {
        return this.lastLine - this.firstLine + 1;
    }

    public boolean isAddition()
    {
        return this.addition;
    }

    public Commit getCommit()
    {
        return this.commit;
    }

    public boolean isNew()
    {
        return this.newlyCreated;
    }

    public void setNew(final boolean isNew)
    {
        this.newlyCreated = false;
    }

    @Override
    public String toString()
    {
        String ret = this.commit.getHash();
        if (this.newlyCreated) {
            ret += "*";
        }

        ret += " {" + this.sfile.toString() + "} ";

        if (this.addition) {
            ret += "+";
        } else {
            ret += "-";
        }

        ret += "[" + Integer.toString(this.firstLine) + ".." + Integer.toString(this.lastLine) + "]";

        return ret;
    }
}
