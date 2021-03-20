//
// EvoLudo Project
//
// Copyright 2010 Christoph Hauert
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// For publications in any form, you are kindly requested to attribute the
// author and project as follows:
//
//	Hauert, Christoph (<year>) EvoLudo Project, http://www.evoludo.org
//			(doi: <doi>[, <version>])
//
//	<doi>:	digital object identifier of the downloaded release (or the
//			most recent release if downloaded from github.com),
//	<year>:	year of release (or download), and
//	[, <version>]: optional version number (as reported in output header
//			or GUI console) to simplify replication of reported results.
//
// The formatting may be adjusted to comply with publisher requirements.
//

package org.evoludo.util;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author Christoph Hauert
 */
public class Plist extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param plist the plist-dictionary to compare against <code>this</code>
	 * @return the number of differences found
	 */
	public int diff(Plist plist) {
		return diff(plist, null);
	}

	/**
	 * @param plist the plist-dictionary to compare against <code>this</code>
	 * @param skip the collection of keys to skip
	 * @return the number of differences found
	 */
	public int diff(Plist plist, Collection<String> skip) {
		int nDiffs = 0;
		// this is the reference list
		// step 1: check if plist contains all keys of this
		for (String key : keySet()) {
			if (plist.containsKey(key))
				continue;
			nDiffs += reportDiff("key '" + key + "' missing in plist.", key, skip);
		}
		// step 2: check if this contains all keys of plist
		for (String key : plist.keySet()) {
			if (containsKey(key))
				continue;
			nDiffs += reportDiff("key '" + key + "' missing in this.", key, skip);
		}
		// step 3: compare entries this
		return nDiffs + diffDict(this, plist, skip);
	}

	@SuppressWarnings("unchecked")
	private int diffDict(Plist reference, Plist plist, Collection<String> skip) {
		int nDiffs = 0;
		for (String key : reference.keySet()) {
			if (!plist.containsKey(key)) {
				if (reference == this)
					continue;
				nDiffs += reportDiff("key '" + key + "' missing in plist.", key, skip);
				continue;
			}
			Object val = reference.get(key);
			Object pval = plist.get(key);
			if (val.getClass() != pval.getClass()) {
				nDiffs += reportDiff("key '" + key + "' values class differs (this: " + val.getClass() + ", plist: "
						+ pval.getClass() + ").", key, skip);
				continue;
			}
			if (val instanceof Plist) {
				// entry is dict
				int diff = diffDict((Plist) val, (Plist) pval, skip);
				if (diff == 0)
					continue;
				nDiffs += reportDiff("key '" + key + "' dicts differ.", key, skip);
				continue;
			}
			if (val instanceof List) {
				// entry is array
				int diff = diffArray((List<Object>) val, (List<Object>) pval, skip);
				if (diff == 0)
					continue;
				nDiffs += diff;
				continue;
			}
			if (val instanceof String) {
				if (((String) val).equals(pval))
					continue;
				nDiffs += reportDiff("key '" + key + "' strings differ (this: " + val + ", plist: " + pval + ").", key,
						skip);
				continue;
			}
			if (val instanceof Integer) {
				if (((Integer) val).equals(pval))
					continue;
				nDiffs += reportDiff("key '" + key + "' integers differ (this: " + val + ", plist: " + pval + ").", key,
						skip);
				continue;
			}
			if (val instanceof Double) {
				if (((Double) val).equals(pval))
					continue;
				nDiffs += reportDiff("key '" + key + "' reals differ (this: " + val + ", plist: " + pval + ").", key,
						skip);
				continue;
			}
			if (val instanceof Boolean) {
				if (((Boolean) val).equals(pval))
					continue;
				nDiffs += reportDiff("key '" + key + "' boolean differ (this: " + val + ", plist: " + pval + ").", key,
						skip);
				continue;
			}
			nDiffs += reportDiff("key '" + key + "' unknown value type (class: " + val.getClass() + ").", key, skip);
		}
		return nDiffs;
	}

	@SuppressWarnings("unchecked")
	private int diffArray(List<Object> reference, List<Object> array, Collection<String> skip) {
		if (reference.size() != array.size()) {
			return reportDiff("arrays differ in size (this: " + reference.size() + ", plist: " + array.size() + ").");
		}
		int nDiffs = 0;
		int i = 0;
		for (Object ele : reference) {
			Object pele = array.get(i++);
			if (ele.getClass() != pele.getClass()) {
				nDiffs += reportDiff(
						"arrays class differs (this: " + ele.getClass() + ", plist: " + pele.getClass() + ").");
				continue;
			}
			if (ele instanceof Plist) {
				// entry is dict
				nDiffs += diffDict((Plist) ele, (Plist) pele, skip);
				continue;
			}
			if (ele instanceof List) {
				// entry is array
				nDiffs += diffArray((List<Object>) ele, (List<Object>) pele, skip);
				continue;
			}
			if (ele instanceof String) {
				if (((String) ele).equals(pele))
					continue;
				nDiffs += reportDiff(
						"arrays of strings differ at index " + i + " (this: " + ele + ", plist: " + pele + ").");
				continue;
			}
			if (ele instanceof Integer) {
				if (((Integer) ele).equals(pele))
					continue;
				nDiffs += reportDiff(
						"arrays of integers differ at index " + i + " (this: " + ele + ", plist: " + pele + ").");
				continue;
			}
			if (ele instanceof Double) {
				if (((Double) ele).equals(pele))
					continue;
				nDiffs += reportDiff(
						"arrays of reals differ at index " + i + " (this: " + ele + ", plist: " + pele + ").");
				continue;
			}
			if (ele instanceof Boolean) {
				if (((Boolean) ele).equals(pele))
					continue;
				nDiffs += reportDiff(
						"arrays of boolean differ at index " + i + " (this: " + ele + ", plist: " + pele + ").");
				continue;
			}
			nDiffs += reportDiff("arrays of unknown type (class: " + ele.getClass() + ").");
		}
		return nDiffs;
	}

	private int reportDiff(String msg) {
		return reportDiff(msg, null, null);
	}

	private int reportDiff(String msg, String key, Collection<String> skip) {
		if (skip != null && skip.contains(key))
			return 0;
		System.err.println((new Date().toString()) + " - Plist.diff: " + msg);
		return 1;
	}
}
