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

/**
 * Interface for classes that that to provide command line options for
 * configurable parameters.
 * 
 * @author Christoph Hauert
 */
public interface CLOProvider {

	/**
	 * All providers of command line options must implement this method to collect
	 * their options.
	 * <p>
	 * Each command line option is (uniquely) identified by it's name (see
	 * {@link CLOption#getName()}), which corresponds to the long version of the
	 * option. If an attempt is made to add an option with a name that already
	 * exists, the <code>parser</code> issues a warning and ignores the option.
	 * Thus, in general, implementing subclasses should first register their options
	 * and call <code>super.collectCLO(CLOParser)</code> at the <em>end</em> such
	 * that subclasses are able to override command line options specified in a
	 * parental class.
	 * </p>
	 * 
	 * @param parser reference to parser that manages command line options.
	 * @see CLOParser#addCLO(CLOption)
	 */
	public void collectCLO(CLOParser parser);
}
