package de.uni_bremen.see.arborext;

import java.util.List;

public abstract class Extractor
{
    protected String repoUrl;

    public Extractor(final String repository) throws ExtractionError
    {
        this.repoUrl = repository;
        this.cloneRepository();
    }

    public List<Commit> extractCommits() throws ExtractionError
    {
        // TODO
        return null;
    }

    abstract protected void cloneRepository() throws ExtractionError;
    abstract protected void tidyUp();
    abstract protected List<Commit> getRawCommits() throws ExtractionError;
    abstract protected void enrichWithContributions(Commit commit) throws ExtractionError;
}
