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
 * SupervisedPrediction.java
 * Copyright (C) 2022 University of Waikato, Hamilton, NZ
 */

package weka.filters.unsupervised.attribute.missingvaluesimputation;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Range;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.filters.unsupervised.attribute.Remove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * For each of the columns within the attribute range that contains missing values, it builds either a classification or regression model using the remaining attributes from the attribute range. With the predictions of these models, the missing values (ie class attribute for this model) get filled in.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p>
 *
 * <pre> -debug-info
 *  If enabled, outputs debugging information in the console.
 *  (default: off)</pre>
 *
 * <pre> -att-range &lt;range&gt;
 *  The attribute range to use for building models and predicting missing values.
 *  (default: first-last)</pre>
 *
 * <pre> -regression &lt;classname + options&gt;
 *  The regression algorithm to use for numeric attributes.
 *  (default: weka.classifiers.functions.LinearRegression)</pre>
 *
 * <pre> -classification &lt;classname + options&gt;
 *  The classification algorithm to use for nominal attributes.
 *  (default: RandomForest: No model built yet.)</pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SupervisedPrediction
    extends AbstractImputation {

  /** the debug flag. */
  public final static String DEBUG_INFO = "debug-info";

  /** the flag for the attribute range. */
  public final static String ATTRIBUTE_RANGE = "att-range";

  /** the flag for the regression algorithm. */
  public final static String REGRESSION = "regression";

  /** the flag for the classification algorithm. */
  public final static String CLASSIFICATION = "classification";

  /** whether to output debugging information. */
  protected boolean m_DebugInfo = false;

  /** the attribute range to use. */
  protected Range m_AttributeRange = getDefaultAttributeRange();

  /** the regression algorithm to use. */
  protected Classifier m_Regression = getDefaultRegression();

  /** the classification algorithm to use. */
  protected Classifier m_Classification = getDefaultClassification();

  /** the training data. */
  protected Instances m_TrainingData;

  /** the models used for filling in the missing values. */
  protected Map<Integer,Classifier> m_Models;

  /** the training headers for the models. */
  protected Map<Integer,Instances> m_Headers;

  /** the attribute indices. */
  protected List<Integer> m_AttributeIndices;

  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  @Override
  public String globalInfo() {
    return
        "For each of the columns within the attribute range that contains missing values, it builds "
            + "either a classification or regression model using the remaining attributes from the "
            + "attribute range. With the predictions of these models, the missing values (ie class "
            + "attribute for this model) get filled in.";
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
        "\t" + debugInfoTipText() + "\n"
            + "\t(default: off)",
        DEBUG_INFO, 0, "-" + DEBUG_INFO));

    result.addElement(new Option(
        "\t" + attributeRangeTipText() + "\n"
            + "\t(default: " + getDefaultAttributeRange().getRanges() + ")",
        ATTRIBUTE_RANGE, 1, "-" + ATTRIBUTE_RANGE + " <range>"));

    result.addElement(new Option(
        "\t" + regressionTipText() + "\n"
            + "\t(default: " + getDefaultRegression().getClass().getName() + ")",
        REGRESSION, 1, "-" + REGRESSION + " <classname + options>"));

    result.addElement(new Option(
        "\t" + classificationTipText() + "\n"
            + "\t(default: " + getDefaultClassification() + ")",
        CLASSIFICATION, 1, "-" + CLASSIFICATION + " <classname + options>"));

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

    if (getDebugInfo())
      result.add("-" + DEBUG_INFO);

    result.add("-" + ATTRIBUTE_RANGE);
    result.add("" + m_AttributeRange.getRanges());

    result.add("-" + REGRESSION);
    result.add("" + Utils.toCommandLine(m_Regression));

    result.add("-" + CLASSIFICATION);
    result.add("" + Utils.toCommandLine(m_Classification));

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
    Classifier 			algorithm;

    setDebugInfo(Utils.getFlag(DEBUG_INFO, options));

    tmpStr = Utils.getOption(ATTRIBUTE_RANGE, options);
    if (!tmpStr.isEmpty())
      setAttributeRange(new Range(tmpStr));
    else
      setAttributeRange(getDefaultAttributeRange());

    tmpStr = Utils.getOption(REGRESSION, options);
    if (!tmpStr.isEmpty()) {
      tmpOptions    = Utils.splitOptions(tmpStr);
      tmpStr        = tmpOptions[0];
      tmpOptions[0] = "";
      algorithm     = (Classifier) Utils.forName(Classifier.class, tmpStr, tmpOptions);
      setRegression(algorithm);
    }
    else {
      setRegression(getDefaultRegression());
    }

    tmpStr = Utils.getOption(CLASSIFICATION, options);
    if (!tmpStr.isEmpty()) {
      tmpOptions    = Utils.splitOptions(tmpStr);
      tmpStr        = tmpOptions[0];
      tmpOptions[0] = "";
      algorithm     = (Classifier) Utils.forName(Classifier.class, tmpStr, tmpOptions);
      setClassification(algorithm);
    }
    else {
      setClassification(getDefaultClassification());
    }

    super.setOptions(options);
  }

  /**
   * Sets whether to output debugging info.
   *
   * @param value 	true if to output
   */
  public void setDebugInfo(boolean value) {
    m_DebugInfo = value;
  }

  /**
   * Returns whether to output debugging info.
   *
   * @return		true if to output
   */
  public boolean getDebugInfo() {
    return m_DebugInfo;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String debugInfoTipText() {
    return "If enabled, outputs debugging information in the console.";
  }

  /**
   * Returns the default attribute range to use.
   *
   * @return		the default
   */
  protected Range getDefaultAttributeRange() {
    return new Range("first-last");
  }

  /**
   * Sets the attribute range to use.
   *
   * @param value 	the range
   */
  public void setAttributeRange(Range value) {
    m_AttributeRange = value;
  }

  /**
   * Returns the attribute range in use.
   *
   * @return		the range
   */
  public Range getAttributeRange() {
    return m_AttributeRange;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String attributeRangeTipText() {
    return "The attribute range to use for building models and predicting missing values.";
  }

  /**
   * Returns the default regression algorithm to use.
   *
   * @return		the default
   */
  protected Classifier getDefaultRegression() {
    LinearRegression	result;

    result = new LinearRegression();
    result.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));
    result.setEliminateColinearAttributes(false);

    return result;
  }

  /**
   * Sets the regression algorithm to use.
   *
   * @param value 	the algorithm
   */
  public void setRegression(Classifier value) {
    m_Regression = value;
  }

  /**
   * Returns the regression algorithm in use.
   *
   * @return		the algorithm
   */
  public Classifier getRegression() {
    return m_Regression;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String regressionTipText() {
    return "The regression algorithm to use for numeric attributes.";
  }

  /**
   * Returns the default classification algorithm to use.
   *
   * @return		the default
   */
  protected Classifier getDefaultClassification() {
    return new RandomForest();
  }

  /**
   * Sets the classification algorithm to use.
   *
   * @param value 	the algorithm
   */
  public void setClassification(Classifier value) {
    m_Classification = value;
  }

  /**
   * Returns the classification algorithm to use.
   *
   * @return		the algorithm
   */
  public Classifier getClassification() {
    return m_Classification;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String classificationTipText() {
    return "The classification algorithm to use for nominal attributes.";
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
   * Outputs the debugging message if debugging output is enabled.
   *
   * @param msg		the message to output
   */
  protected void debug(String msg) {
    if (m_DebugInfo)
      System.out.println("[DEBUG] " + getClass().getName() + " - " + msg);
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
    int[]		indices;
    String		actualRange;
    Remove		remove;
    FilteredClassifier	filteredClassifier;
    Instances		trainData;
    Capabilities	capsRegression;
    Capabilities	capsClassification;

    m_TrainingData = new Instances(data);
    m_Models       = new HashMap<Integer, Classifier>();
    m_Headers      = new HashMap<Integer, Instances>();
    m_AttributeRange.setUpper(m_TrainingData.numAttributes() - 1);
    indices = m_AttributeRange.getSelection();
    capsRegression     = m_Regression.getCapabilities();
    capsClassification = m_Classification.getCapabilities();

    // determine attributes to predict
    m_AttributeIndices = new ArrayList<Integer>();
    debug("Checking attribute range: " + m_AttributeRange.getRanges());
    for (int index: indices) {
      if (index == data.classIndex()) {
        debug("Skipping class attribute at #" + (index + 1));
        continue;
      }
      if (m_TrainingData.attribute(index).isNominal() || m_TrainingData.attribute(index).isNumeric()) {
        if (m_TrainingData.attribute(index).isNominal() && !capsClassification.test(m_TrainingData.attribute(index), true)) {
          debug("Nominal attribute #" + (index + 1) + ": not handled by classification algorithm if class (" + capsClassification.getFailReason().getMessage() + ")");
          continue;
        }
        if (m_TrainingData.attribute(index).isNumeric() && !capsRegression.test(m_TrainingData.attribute(index), true)) {
          debug("Numeric attribute #" + (index + 1) + ": not handled by regression algorithm if class (" + capsRegression.getFailReason().getMessage() + ")");
          continue;
        }
        if (m_TrainingData.attributeStats(index).missingCount == 0) {
          debug("Attribute #" + (index + 1) + ": no missing values");
          continue;
        }
        m_AttributeIndices.add(index);
      }
    }
    Collections.sort(m_AttributeIndices);
    indices = new int[m_AttributeIndices.size()];
    for (int i = 0; i < m_AttributeIndices.size(); i++)
      indices[i] = m_AttributeIndices.get(i);
    actualRange = Range.indicesToRangeList(indices);
    debug("Actual range: " + actualRange);

    // build datasets and models
    for (int index: indices) {
      trainData = new Instances(data);
      trainData.setClassIndex(index);
      remove = new Remove();
      remove.setAttributeIndices(actualRange);
      remove.setInvertSelection(true);
      filteredClassifier = new FilteredClassifier();
      filteredClassifier.setFilter(remove);
      if (m_TrainingData.attribute(index).isNominal())
        filteredClassifier.setClassifier(AbstractClassifier.makeCopy(m_Classification));
      else
        filteredClassifier.setClassifier(AbstractClassifier.makeCopy(m_Regression));
      debug("Building model for attribute #" + (index + 1) + ": " + Utils.toCommandLine(filteredClassifier));
      filteredClassifier.buildClassifier(trainData);
      m_Models.put(index, filteredClassifier);
      m_Headers.put(index, new Instances(trainData, 0));
    }

    return new Instances(data, 0);
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
    Instance	result;
    Instance	instCopy;
    double	pred;

    result = (Instance) inst.copy();
    for (Integer index: m_AttributeIndices) {
      instCopy = (Instance) inst.copy();
      instCopy.setDataset(m_Headers.get(index));
      pred = m_Models.get(index).classifyInstance(instCopy);
      if (m_TrainingData.attribute(index).isNominal())
        result.setValue(index, m_TrainingData.attribute(index).value((int) pred));
      else
        result.setValue(index, pred);
    }

    return result;
  }
}
