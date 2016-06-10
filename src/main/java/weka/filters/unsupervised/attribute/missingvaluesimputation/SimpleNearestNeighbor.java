/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * SimpleNearestNeighbor.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package weka.filters.unsupervised.attribute.missingvaluesimputation;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Uses the specified nearest neighbor search to determine the neighborhood from which it uses:<br>
 * - the most common label (nominal attributes)<br>
 * - the average in the neighborhood (numeric/date attributes)<br>
 * to replace missing values for the Instance currently being processed.<br>
 * In case of ties for nominal attributes, the 'smaller' label (alphabetically speaking) wins.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p>
 * 
 * <pre> -search &lt;classname + options&gt;
 *  The nearest neighbor search algorithm.
 *  (default: weka.core.neighboursearch.LinearNNSearch)</pre>
 * 
 * <pre> -num-neighbors &lt;int&gt;
 *  The size of the neighborhood to use.
 *  (default: 100)</pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SimpleNearestNeighbor
  extends AbstractImputation {

  /** the flag for the search algorithm. */
  public final static String SEARCH = "search";

  /** the flag for the number of neighbors. */
  public final static String NUM_NEIGHBORS = "num-neighbors";

  /** the nearest neighbor search to use. */
  protected NearestNeighbourSearch m_Search = getDefaultSearch();

  /** the size of the neighborhood. */
  protected int m_NumNeighbors = getDefaultNumNeighbors();

  /** the training data. */
  protected Instances m_TrainingData;

  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  @Override
  public String globalInfo() {
    return
      "Uses the specified nearest neighbor search to determine the neighborhood "
      + "from which it uses:\n"
      + "- the most common label (nominal attributes)\n"
      + "- the average in the neighborhood (numeric/date attributes)\n"
      + "to replace missing values for the Instance currently being processed.\n"
      + "In case of ties for nominal attributes, the 'smaller' label (alphabetically speaking) wins.";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  @Override
  public Enumeration<Option> listOptions() {
    Vector<Option> result = new Vector<Option>();

    result.addElement(new Option(
      "\tThe nearest neighbor search algorithm.\n"
      + "\t(default: " + getDefaultSearch().getClass().getName() + ")",
      SEARCH, 1, "-" + SEARCH + " <classname + options>"));

    result.addElement(new Option(
      "\tThe size of the neighborhood to use.\n"
      + "\t(default: " + getDefaultNumNeighbors() + ")",
      NUM_NEIGHBORS, 1, "-" + NUM_NEIGHBORS + " <int>"));

    result.addAll(Collections.list(super.listOptions()));

    return result.elements();
  }

  /**
   * Gets the current settings of the filter.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  @Override
  public String[] getOptions() {
    List<String> result = new ArrayList<String>();

    result.add("-" + SEARCH);
    result.add("" + Utils.toCommandLine(m_Search));

    result.add("-" + NUM_NEIGHBORS);
    result.add("" + m_NumNeighbors);

    Collections.addAll(result, super.getOptions());

    return result.toArray(new String[result.size()]);
  }

  /**
   * Parses a given list of options.
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  @Override
  public void setOptions(String[] options) throws Exception {
    String 			tmpStr;
    String[] 			tmpOptions;
    NearestNeighbourSearch 	search;

    tmpStr = Utils.getOption(SEARCH, options);
    if (!tmpStr.isEmpty()) {
      tmpOptions    = Utils.splitOptions(tmpStr);
      tmpStr        = tmpOptions[0];
      tmpOptions[0] = "";
      search        = (NearestNeighbourSearch) Utils.forName(NearestNeighbourSearch.class, tmpStr, tmpOptions);
      setSearch(search);
    }
    else {
      setSearch(getDefaultSearch());
    }

    tmpStr = Utils.getOption(NUM_NEIGHBORS, options);
    if (!tmpStr.isEmpty())
      setNumNeighbors(Integer.parseInt(tmpStr));
    else
      setNumNeighbors(getDefaultNumNeighbors());

    super.setOptions(options);

    Utils.checkForRemainingOptions(options);
  }

  /**
   * Returns the default nearest neighbor search to use.
   *
   * @return		the default
   */
  protected NearestNeighbourSearch getDefaultSearch() {
    return new LinearNNSearch();
  }

  /**
   * Sets the nearest neighbor search to use.
   *
   * @param value 	the search
   */
  public void setSearch(NearestNeighbourSearch value) {
    m_Search = value;
  }

  /**
   * Returns the current nearest neighbor search in use.
   *
   * @return		the search
   */
  public NearestNeighbourSearch getSearch() {
    return m_Search;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String searchTipText() {
    return "The nearest neighbor search algorithm to use.";
  }

  /**
   * Returns the default nearest neighbor search to use.
   *
   * @return		the default
   */
  protected int getDefaultNumNeighbors() {
    return 100;
  }

  /**
   * Sets the size of the neighborhood to use.
   *
   * @param value 	the size
   */
  public void setNumNeighbors(int value) {
    if (value > 0)
      m_NumNeighbors = value;
    else
      System.err.println("Size of neighborhood must be > 0, provided: " + value);
  }

  /**
   * Returns the size of the neighborhood to use.
   *
   * @return		the size
   */
  public int getNumNeighbors() {
    return m_NumNeighbors;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String numNeighborsTipText() {
    return "The size of the neighborhood to use.";
  }

  /**
   * Returns the Capabilities of this filter.
   *
   * @return            the capabilities of this object
   * @see               Capabilities
   */
  @Override
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();
    result.disableAll();

    // attributes
    result.enable(Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capability.DATE_ATTRIBUTES);
    result.enable(Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capability.MISSING_VALUES);

    // class
    result.enableAllClasses();
    result.enable(Capability.MISSING_CLASS_VALUES);
    result.enable(Capability.NO_CLASS);

    return result;
  }

  /**
   * Performs the actual initialization of the imputation algorithm with the
   * training data.
   *
   * @param data the training data
   * @return the potentially modified header
   * @throws Exception if the training fails
   */
  @Override
  protected Instances doBuildImputation(Instances data) throws Exception {
    m_TrainingData = new Instances(data);
    m_Search.setInstances(m_TrainingData);
    return new Instances(data, 0);
  }

  /**
   * Computes the mean for a list of doubles.
   *
   * @param vector the list
   * @return the mean
   */
  public static double mean(List<Double> vector) {
    double sum = 0;

    if (vector.size() == 0)
      return 0;

    for (double element : vector)
      sum += element;

    return sum / vector.size();
  }

  /**
   * Performs the actual imputation.
   *
   * @param inst the instance to perform imputation on
   * @return the updated instance
   * @throws Exception if the imputation fails, eg if not initialized.
   */
  @Override
  protected Instance doImpute(Instance inst) throws Exception {
    Instance			result;
    Instances			closest;
    List<Integer>		missing;
    int				i;
    int				n;
    Attribute			att;
    List<Double>		values;
    String			label;
    HashMap<String,Integer>	counts;
    int				max;
    List<String>		labels;

    result = inst;

    // missing values?
    missing = new ArrayList<Integer>();
    for (i = 0; i < inst.numValues(); i++) {
      if (inst.isMissingSparse(i))
	missing.add(i);
    }

    // replace missing values
    if (missing.size() > 0) {
      result = (Instance) inst.copy();
      closest = m_Search.kNearestNeighbours(inst, m_NumNeighbors);
      for (i = 0; i < missing.size(); i++) {
	att = inst.attributeSparse(missing.get(i));
	switch (att.type()) {
	  case Attribute.NUMERIC:
	  case Attribute.DATE:
	    values = new ArrayList<Double>();
	    for (n = 0; n < closest.numInstances(); n++) {
	      if (!closest.instance(n).isMissingSparse(missing.get(i)))
		values.add(closest.instance(n).valueSparse(missing.get(i)));
	    }
	    result.setValueSparse(missing.get(i), mean(values));
	    break;

	  case Attribute.NOMINAL:
	    counts = new HashMap<String, Integer>();
	    for (n = 0; n < att.numValues(); n++)
	      counts.put(att.value(n), 0);
	    for (n = 0; n < closest.numInstances(); n++) {
	      if (!closest.instance(n).isMissingSparse(missing.get(i))) {
		label = att.value((int) closest.instance(n).valueSparse(missing.get(i)));
		counts.put(label, counts.get(label) + 1);
	      }
	    }
	    max    = 0;
	    labels = new ArrayList<String>(counts.keySet());
	    label  = labels.get(0);
	    Collections.sort(labels);
	    for (String l : labels) {
	      if (counts.get(l) > max) {
		max = counts.get(l);
		label = l;
	      }
	      max = Math.max(max, counts.get(l));
	    }
	    result.setValueSparse(missing.get(i), att.indexOfValue(label));
	    break;
	}
      }
    }

    return result;
  }
}
