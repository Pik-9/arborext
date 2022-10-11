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

import java.lang.Throwable;
import java.util.List;

/**
 * This is thrown when all commits of some branches need to be
 * turned into another branch.
 */
public class NeedToSetBranch extends Throwable
{
    private int branchToSetTo;
    private List<Integer> branchesToSet;

    public NeedToSetBranch (final int branchToSetTo, final List<Integer> branchesToSet)
    {
        super();
        this.branchToSetTo = branchToSetTo;
        this.branchesToSet = branchesToSet;
    }

    /**
     * Change the branch id of given commit if necessary.
     */
    public void editCommitIfNecessary (Commit commit)
    {
        if (branchesToSet.contains(commit.getBranchId())) {
            commit.setBranchId(branchToSetTo);
        }
    }
}
