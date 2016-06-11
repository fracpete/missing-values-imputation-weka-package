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
 * MultiInjection.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package weka.filters.unsupervised.attribute.missingvaluesinjection;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Applies the specified injection algorithms one after the other.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p>
 *
 * <pre> -algorithm &lt;classname + options&gt;
 *  The injection algorithms to use (can be supplied multiple times).</pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MultiInjection
  extends AbstractInjection {

  /** the flag for the algorithm. */
  public final static String ALGORITHM = "algorithm";

  /** the injection algorithms to apply. */
  protected Injection[] m_Algorithms = getDefaultAlgorithms();

  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  @Override
  public String globalInfo() {
    return "Applies the specified injection algorithms one after the other.";
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
      "\tThe injection algorithms to use (can be supplied multiple times).",
        ALGORITHM, 1, "-" + ALGORITHM + " <classname + options>"));

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

    for (Injection imp: m_Algorithms) {
      result.add("-" + ALGORITHM);
      result.add("" + Utils.toCommandLine(imp));
    }

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
    String 		tmpStr;
    String[] 		tmpOptions;
    List<Injection>	imps;
    Injection		imp;

    imps   = new ArrayList<Injection>();
    do {
      tmpStr = Utils.getOption(ALGORITHM, options);
      if (!tmpStr.isEmpty()) {
	tmpOptions = Utils.splitOptions(tmpStr);
	tmpStr = tmpOptions[0];
	tmpOptions[0] = "";
	imp = (Injection) Utils.forName(Injection.class, tmpStr, tmpOptions);
	imps.add(imp);
      }
    }
    while (!tmpStr.isEmpty());
    if (imps.size() == 0)
      imps.addAll(Arrays.asList(getDefaultAlgorithms()));
    setAlgorithms(imps.toArray(new Injection[imps.size()]));

    super.setOptions(options);

    Utils.checkForRemainingOptions(options);
  }

  /**
   * Returns the default injections to use.
   *
   * @return		the default
   */
  protected Injection[] getDefaultAlgorithms() {
    return new Injection[]{
      new NullInjection()
    };
  }

  /**
   * Sets the injections to use.
   *
   * @param value 	the injections
   */
  public void setAlgorithms(Injection[] value) {
    m_Algorithms = value;
  }

  /**
   * Returns the current injections in use.
   *
   * @return		the injections
   */
  public Injection[] getAlgorithms() {
    return m_Algorithms;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String algorithmsTipText() {
    return "The injection algorithms to apply sequentially to the data.";
  }

  /**
   * Performs the actual initialization of the injection algorithm with the
   * training data.
   *
   * @param data the training data
   * @return the potentially modified header
   * @throws Exception if the training fails
   */
  @Override
  protected Instances doBuildInjection(Instances data) throws Exception {
    Instances	result;
    int		i;

    result = data;

    for (i = 0; i < m_Algorithms.length; i++) {
      try {
	result = m_Algorithms[i].buildInjection(result);
      }
      catch (Exception e) {
	throw new Exception("Algorithm #" + (i+1) + " failed!", e);
      }
    }

    return result;
  }

  /**
   * Performs the actual injection.
   *
   * @param inst the instance to perform injection on
   * @return the updated instance
   * @throws Exception if the injection fails, eg if not initialized.
   * @see #buildInjection(Instances)
   */
  @Override
  protected Instance doInject(Instance inst) throws Exception {
    Instance	result;
    int		i;

    result = inst;

    for (i = 0; i < m_Algorithms.length; i++) {
      try {
	result = m_Algorithms[i].inject(result);
      }
      catch (Exception e) {
	throw new Exception("Algorithm #" + (i+1) + " failed!", e);
      }
    }

    return result;
  }
}
