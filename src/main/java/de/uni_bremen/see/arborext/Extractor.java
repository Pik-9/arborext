package de.uni_bremen.see.arborext;

import java.util.List;

public abstract class Extractor
{
    protected String repoUrl;

    public Extractor(final String repository)
    {
        this.repoUrl = repository;
        this.cloneRepository();
    }

    public List<Commit> extractCommits()
    {
        // TODO
        return null;
    }

    abstract protected void cloneRepository();
    abstract protected void tidyUp();
    abstract protected List<Commit> getRawCommits();
    abstract protected void enrichWithContributions(Commit commit);
}
