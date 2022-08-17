package de.uni_bremen.see.arborext;

import java.util.List;
import java.util.ArrayList;

public class DummyExtractor extends Extractor
{
    DummyExtractor(String repository) throws ExtractionError
    {
        super(repository);
    }

    @Override
    protected void cloneRepository() throws ExtractionError
    {
        // Nothing to do
    }

    @Override
    protected void tidyUp()
    {
        // Nothing to do
    }

    @Override
    protected List<Commit> getRawCommits() throws ExtractionError
    {
        return null;
    }

    @Override
    protected void enrichWithContributions(Commit commit) throws ExtractionError
    {
        // TODO
    }
}
