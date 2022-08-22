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

public class SourceFile
{
    private int id;
    private int loc;
    private List<String> knownNames;
    private List<Contribution> contributions;

    protected static int newId = 1;
    protected static List<SourceFile> pathToFiles = new ArrayList<SourceFile> ();

    protected SourceFile(final String name)
    {
        this.id = newId++;
        this.loc = 0;
        this.knownNames = new ArrayList<String> ();
        this.knownNames.add(name);
        this.contributions = new ArrayList<Contribution> ();
        pathToFiles.add(this);
    }

    public String getId()
    {
        return "F" + Integer.toString(this.id);
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
            this.loc += contribution.getLOC();
        } else {
            this.loc -= contribution.getLOC();
        }

        this.contributions.add(contribution);
    }

    public String getNames()
    {
        if (this.knownNames.size() > 1) {
            String ret = "";
            for (int ii = 0; ii < this.knownNames.size(); ++ii) {
                ret += this.knownNames.get(ii);
                if(ii < this.knownNames.size() - 1) {
                    ret += ", ";
                }
            }
            return ret;
        } else {
            return this.knownNames.get(0);
        }
    }

    static public void setEverythingOld()
    {
        for (SourceFile sf : pathToFiles) {
            for (Contribution cont : sf.getContributions()) {
                cont.setNew(false);
            }
        }
    }

    static public List<Contribution> getAllContributionsFromBranch(final int branchId)
    {
        List<Contribution> ret = new ArrayList<Contribution> ();
        for (SourceFile sf : pathToFiles) {
            for (Contribution cont : sf.getContributions()) {
                if (cont.getCommit().getBranchId() == branchId) {
                    ret.add(cont);
                }
            }
        }
        return ret;
    }

    static public SourceFile getSourceFile(final String filename)
    {
        SourceFile ret = null;
        for (SourceFile sf : pathToFiles) {
            if (sf.goesByThisName(filename)) {
                ret = sf;
                break;
            }
        }

        if (ret == null) {
            ret = new SourceFile(filename);
        }

        return ret;
    }

    static public List<SourceFile> getAllFiles()
    {
        return pathToFiles;
    }

    @Override
    public String toString()
    {
        String ret = "[";
        for (String kname : this.knownNames) {
            ret += kname + ", ";
        }
        ret += "] -> ";
        ret += Integer.toString(this.loc);

        return ret;
    }
}
