package de.uni_bremen.see.arborext;

public class Contribution
{
    private int firstLine;
    private int lastLine;
    private boolean addition;
    private Commit commit;
    private SourceFile sfile;

    public Contribution(
        final int firstLine,
        final int lastLine,
        final boolean isAddition,
        final Commit commit,
        final String sourceFilePath
    ) {
        this.firstLine = firstLine;
        this.lastLine = lastLine;
        this.addition = isAddition;
        this.commit = commit;

        // TODO: Source file identification.
    }

    public int getFirstLine()
    {
        return this.firstLine;
    }

    public int getLastLine()
    {
        return this.lastLine;
    }

    public boolean isAddition()
    {
        return this.addition;
    }

    public Commit getCommit()
    {
        return this.commit;
    }
}
