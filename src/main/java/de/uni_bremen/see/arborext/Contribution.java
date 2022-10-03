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

/**
 * A contribution is a batch of lines in a source file by a certain Commit.
 *
 * @see SourceFile
 * @see Commit
 */
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

    /**
     * @param firstLine the start of the contribution in the source file.
     * @param lastLine the last line of the contribution in the source file.
     * @param isAddition is this commit adding lines or taking them away.
     * @param commit the commit tied to this contribution.
     * @param sourceFilePath the path to the source file within the repository.
     */
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

    /**
     * Get the number of lines this contribution is adding or taking away.
     */
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

    public int getBranchId()
    {
        return this.commit.getBranchId();
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
