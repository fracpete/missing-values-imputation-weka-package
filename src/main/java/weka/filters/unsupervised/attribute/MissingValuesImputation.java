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

/*
 *    MissingValuesImputation.java
 *    Copyright (C) 2014-2016 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute;

import weka.core.Capabilities;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.SimpleBatchFilter;
import weka.filters.UnsupervisedFilter;
import weka.filters.unsupervised.attribute.missingvaluesimputation.Imputation;
import weka.filters.unsupervised.attribute.missingvaluesimputation.NullImputation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/** 
 <!-- globalinfo-start -->
 * Replaces missing values using the specified imputation algorithm.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 * 
 * <pre> -algorithm &lt;classname + options&gt;
 *  The imputation algorithm to use.
 *  (default: weka.filters.unsupervised.attribute.missingvaluesimputation.NullImputation)</pre>
 * 
 * <pre> -output-debug-info
 *  If set, filter is run in debug mode and
 *  may output additional info to the console</pre>
 * 
 * <pre> -do-not-check-capabilities
 *  If set, filter capabilities are not checked when input format is set
 *  (use with caution).</pre>
 * 
 <!-- options-end -->
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class MissingValuesImputation 
  extends SimpleBatchFilter
  implements UnsupervisedFilter {

  /** for serialization */
  private final static long serialVersionUID = 8349568310991609867L;

  /** the flag for the algorithm. */
  public final static String ALGORITHM = "algorithm";

  /** The imputation algorithm to use. */
  protected Imputation m_Algorithm = getDefaultAlgorithm();
  
  /**
   * Returns a string describing this filter
   *
   * @return a description of the filter suitable for
   * displaying in the explorer/experimenter gui
   */
  @Override
  public String globalInfo() {
    return "Replaces missing values using the specified imputation algorithm.";
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
      "\tThe imputation algorithm to use.\n"
        + "\t(default: " + getDefaultAlgorithm().getClass().getName() + ")",
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
    
    result.add("-" + ALGORITHM);
    result.add("" + Utils.toCommandLine(getAlgorithm()));

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
    String 	tmpStr;
    String[] 	tmpOptions;
    Imputation 	imp;

    tmpStr = Utils.getOption(ALGORITHM, options);
    if (!tmpStr.isEmpty()) {
      tmpOptions = Utils.splitOptions(tmpStr);
      tmpStr = tmpOptions[0];
      tmpOptions[0] = "";
      imp = (Imputation) Utils.forName(Imputation.class, tmpStr, tmpOptions);
      setAlgorithm(imp);
    }
    else {
      setAlgorithm(getDefaultAlgorithm());
    }

    super.setOptions(options);

    Utils.checkForRemainingOptions(options);
  }

  /**
   * Returns the default imputation algorithm.
   *
   * @return the default
   */
  protected Imputation getDefaultAlgorithm() {
    return new NullImputation();
  }

  /**
   * Sets the imputation algorithm to use.
   * 
   * @param value the algorithm
   */
  public void setAlgorithm(Imputation value) {
    m_Algorithm = value;
  }

  /**
   * Gets the imputation algorithm in use.
   * 
   * @return the algorithm
   */
  public Imputation getAlgorithm() {
    return m_Algorithm;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String algorithmTipText() {
    return "The imputation algorithm to apply to the data.";
  }

  /**
   * Returns the Capabilities of this filter.
   * 
   * @return the capabilities of this object
   * @see Capabilities
   */
  @Override
  public Capabilities getCapabilities() {
    return m_Algorithm.getCapabilities();
  }

  /**
   * Returns whether to allow the determineOutputFormat(Instances) method access
   * to the full dataset rather than just the header.
   * 
   * @return whether determineOutputFormat has access to the full input dataset
   */
  @Override
  public boolean allowAccessToFullInputFormat() {
    return true;
  }

  /**
   * Determines the output format based on the input format and returns this. In
   * case the output format cannot be returned immediately, i.e.,
   * hasImmediateOutputFormat() returns false, then this method will called from
   * batchFinished() after the call of preprocess(Instances), in which, e.g.,
   * statistics for the actual processing step can be gathered.
   * 
   * @param inputFormat the input format to base the output format on
   * @return the output format
   * @throws Exception in case the determination goes wrong
   * @see #hasImmediateOutputFormat()
   * @see #batchFinished()
   */
  @Override
  protected Instances determineOutputFormat(Instances inputFormat) throws Exception {
    return m_Algorithm.buildImputation(inputFormat);
  }

  /**
   * Processes the given data (may change the provided dataset) and returns the
   * modified version. This method is called in batchFinished(). This
   * implementation only calls process(Instance) for each instance in the given
   * dataset.
   * 
   * @param instances the data to process
   * @return the modified data
   * @throws Exception in case the processing goes wrong
   */
  @Override
  protected Instances process(Instances instances) throws Exception {
    return m_Algorithm.impute(instances);
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 1 $");
  }

  /**
   * Main method for executing this filter.
   *
   * @param args should contain arguments to the filter: 
   * use -h for help
   */
  public static void main(String[] args) {
    runFilter(new MissingValuesImputation(), args);
  }
}
