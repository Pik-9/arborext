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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * An extractor that is not really extracting anything, but providing a static dummy history.
 */
public class DummyExtractor extends Extractor
{
    DummyExtractor() throws ExtractionError
    {
        super("");
    }

    @Override
    protected void cloneRepository() throws ExtractionError
    {
        // Nothing to do
    }

    @Override
    public void tidyUp()
    {
        // Nothing to do
    }

    @Override
    protected List<Commit> getRawCommits() throws ExtractionError
    {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        List<Commit> ret = new ArrayList<Commit> ();

        try {
            Commit ca = new Commit("A", "Alice", df.parse("01.01.2022 12:00"), "Initial commit.");
            Commit cb = new Commit("B", "Alice", df.parse("02.01.2022 12:00"), "Add to Mercury.");
            Commit cc = new Commit("C", "Bob", df.parse("03.01.2022 12:00"), "Add Venus.");
            Commit cd = new Commit("D", "Alice", df.parse("04.01.2022 12:00"), "Add Earth.");
            Commit ce = new Commit("E", "Charles", df.parse("05.01.2022 12:00"), "Add Mars.");
            Commit cf = new Commit("F", "Alice", df.parse("06.01.2022 12:00"), "Merge Venus into Mercury and Mars.");

            //ca.addChildCommit("B");

            cb.addParentCommit("A");
            //cb.addChildCommit("C");
            //cb.addChildCommit("D");

            cc.addParentCommit("B");
            //cc.addChildCommit("F");

            cd.addParentCommit("B");
            //cd.addChildCommit("E");
            //cd.addChildCommit("F");

            ce.addParentCommit("D");

            cf.addParentCommit("D");
            cf.addParentCommit("C");

            //ca.setBranchId(1);
            //cb.setBranchId(1);
            //cc.setBranchId(2);
            //cd.setBranchId(1);
            //ce.setBranchId(3);
            //cf.setBranchId(1);

            ret.add(ca);
            ret.add(cb);
            ret.add(cc);
            ret.add(cd);
            ret.add(ce);
            ret.add(cf);
        } catch (ParseException exc) {
            System.err.println("Parse Error: " + exc.getMessage());
            return null;
        }

        return ret;
    }

    @Override
    public void enrichWithContributions(Commit commit) throws ExtractionError
    {
        SourceFile.setEverythingOld();

        if (commit.getHash() == "A") {
            new Contribution(0, 0, true, commit, "Mercury.txt");
        } else if (commit.getHash() == "B") {
            new Contribution(1, 2, true, commit, "Mercury.txt");
        } else if (commit.getHash() == "C") {
            new Contribution(0, 1, true, commit, "Venus.txt");
        } else if (commit.getHash() == "D") {
            new Contribution(0, 2, true, commit, "Earth.txt");
        } else if (commit.getHash() == "E") {
            new Contribution(0, 0, true, commit, "Mars.txt");
        } else if (commit.getHash() == "F") {
            // No contributions, just a merge
            for (Contribution cont : SourceFile.getAllContributionsFromBranch(2)) {
                cont.getCommit().setBranchId(1);
            }
        }
    }
}
