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
 * UserSuppliedValues.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package weka.filters.unsupervised.attribute.missingvaluesimputation;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Uses the IRMI algorithm as published by Templ et al in 'Iterative stepwise regression imputation using standard and robust methods'.<br>
 * <br>
 * Matthias Templ, Alexander Kowarik, Peter Filzmoser (2011). Iterative stepwise regression imputation using standard and robust methods. Computational Statistics &amp; Data Analysis. 55(10):2793-2806.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;article{Templ2011,
 *    author = {Matthias Templ and Alexander Kowarik and Peter Filzmoser},
 *    journal = {Computational Statistics &amp; Data Analysis},
 *    number = {10},
 *    pages = {2793-2806},
 *    title = {Iterative stepwise regression imputation using standard and robust methods},
 *    volume = {55},
 *    year = {2011},
 *    ISSN = {0167-9473},
 *    HTTP = {http://www.statistik.tuwien.ac.at/public/filz/papers/CSDA11TKF.pdf}
 * }
 * </pre>
 * <br><br>
 <!-- technical-bibtex-end -->
 *
 <!-- options-start -->
 * Valid options are: <p>
 * 
 * <pre> -nominal-classifier &lt;classname + options&gt;
 *  Nominal classifier to use
 *  (default: : No model built yet.)</pre>
 * 
 * <pre> -numeric-classifier &lt;classname + options&gt;
 *  Nominal classifier to use
 *  (default: Linear Regression: No model built yet.)</pre>
 * 
 * <pre> -num-epochs &lt;int&gt;
 *  Max number of epochs
 *  (default: 100)</pre>
 * 
 * <pre> -epsilon &lt;double&gt;
 *  Epsilon for early termination
 *  (default: 5.0)</pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class IRMI
  extends AbstractImputation
  implements TechnicalInformationHandler {

  /** the flag for the nominal classifier. */
  public final static String NOMINAL_CLASSIFIER = "nominal-classifier";

  /** the flag for the numeric classifier. */
  public final static String NUMERIC_CLASSIFIER = "numeric-classifier";

  /** the flag for the number of epochs. */
  public final static String NUM_EPOCHS = "num-epochs";

  /** the flag for the early termination value. */
  public final static String EPSILON = "epsilon";

  /** the classifier for nominal attributes. */
  protected Classifier m_nominalClassifier = getDefaultNominalClassifier();

  /** the classifier for numeric attributes. */
  protected Classifier m_numericClassifier = getDefaultNumericClassifier();

  /** the number of epochs. */
  protected int m_numEpochs = getDefaultNumEpochs();

  /** the early termination value. */
  protected double m_epsilon = getDefaultEpsilon();

  /** the classifiers used internally. */
  protected Classifier[] m_classifiers;

  /** the temporary header. */
  protected Instances m_Header;

  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  @Override
  public String globalInfo() {
    return
      "Uses the IRMI algorithm as published by Templ et al in 'Iterative "
	+ "stepwise regression imputation using standard and robust methods'.\n"
	+ "\n"
	+ getTechnicalInformation();
  }

  /**
   * Returns an instance of a TechnicalInformation object, containing
   * detailed information about the technical background of this class,
   * e.g., paper reference or book this class is based on.
   *
   * @return 		the technical information about this class
   */
  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation 	result;

    result = new TechnicalInformation(Type.ARTICLE);
    result.setValue(Field.AUTHOR, "Matthias Templ and Alexander Kowarik and Peter Filzmoser");
    result.setValue(Field.TITLE, "Iterative stepwise regression imputation using standard and robust methods");
    result.setValue(Field.JOURNAL, "Computational Statistics & Data Analysis");
    result.setValue(Field.YEAR, "2011");
    result.setValue(Field.VOLUME, "55");
    result.setValue(Field.NUMBER, "10");
    result.setValue(Field.PAGES, "2793-2806");
    result.setValue(Field.ISSN, "0167-9473");
    result.setValue(Field.HTTP, "http://www.statistik.tuwien.ac.at/public/filz/papers/CSDA11TKF.pdf");

    return result;
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
      "\t" + nominalClassifierTipText() + "\n"
	+ "\t(default: " + getDefaultNominalClassifier() + ")",
      NOMINAL_CLASSIFIER, 1, "-" + NOMINAL_CLASSIFIER + " <classname + options>"));

    result.addElement(new Option(
      "\t" + nominalClassifierTipText() + "\n"
	+ "\t(default: " + getDefaultNumericClassifier() + ")",
      NUMERIC_CLASSIFIER, 1, "-" + NUMERIC_CLASSIFIER + " <classname + options>"));

    result.addElement(new Option(
      "\t" + numEpochsTipText() + "\n"
	+ "\t(default: " + getDefaultNumEpochs() + ")",
      NUM_EPOCHS, 1, "-" + NUM_EPOCHS + " <int>"));

    result.addElement(new Option(
      "\t" + epsilonTipText() + "\n"
	+ "\t(default: " + getDefaultEpsilon() + ")",
      EPSILON, 1, "-" + EPSILON + " <double>"));

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

    result.add("-" + NOMINAL_CLASSIFIER);
    result.add(Utils.toCommandLine(m_nominalClassifier));

    result.add("-" + NUMERIC_CLASSIFIER);
    result.add(Utils.toCommandLine(m_numericClassifier));

    result.add("-" + NUM_EPOCHS);
    result.add("" + m_numEpochs);

    result.add("-" + EPSILON);
    result.add("" + m_epsilon);

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
    String[]	tmpOptions;

    tmpStr = Utils.getOption(NOMINAL_CLASSIFIER, options);
    if (!tmpStr.isEmpty()) {
      tmpOptions    = Utils.splitOptions(tmpStr);
      tmpStr        = tmpOptions[0];
      tmpOptions[0] = "";
      setNominalClassifier((Classifier) Utils.forName(Classifier.class, tmpStr, tmpOptions));
    }
    else {
      setNominalClassifier(getDefaultNominalClassifier());
    }

    tmpStr = Utils.getOption(NUMERIC_CLASSIFIER, options);
    if (!tmpStr.isEmpty()) {
      tmpOptions    = Utils.splitOptions(tmpStr);
      tmpStr        = tmpOptions[0];
      tmpOptions[0] = "";
      setNumericClassifier((Classifier) Utils.forName(Classifier.class, tmpStr, tmpOptions));
    }
    else {
      setNumericClassifier(getDefaultNumericClassifier());
    }

    tmpStr = Utils.getOption(NUM_EPOCHS, options);
    if (!tmpStr.isEmpty())
      setNumEpochs(Integer.parseInt(tmpStr));
    else
      setNumEpochs(getDefaultNumEpochs());

    tmpStr = Utils.getOption(EPSILON, options);
    if (!tmpStr.isEmpty())
      setEpsilon(Double.parseDouble(tmpStr));
    else
      setEpsilon(getDefaultEpsilon());

    super.setOptions(options);

    Utils.checkForRemainingOptions(options);
  }

  /**
   * Returns the default nominal classifier.
   *
   * @return the default
   */
  protected Classifier getDefaultNominalClassifier() {
    return new Logistic();
  }

  /**
   * Sets the classifier to use for nominal attributes.
   *
   * @param value the classifier
   */
  public void setNominalClassifier(Classifier value) {
    m_nominalClassifier = value;
  }

  /**
   * Returns the classifier in use for nominal attributes.
   *
   * @return the classifier
   */
  public Classifier getNominalClassifier() {
    return m_nominalClassifier;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String nominalClassifierTipText() {
    return "Nominal classifier to use";
  }

  protected Classifier getDefaultNumericClassifier() {
    return new LinearRegression();
  }

  /**
   * Sets the classifier to use for numeric attributes.
   *
   * @param value the classifier
   */
  public void setNumericClassifier(Classifier value) {
    m_numericClassifier = value;
  }

  /**
   * Returns the classifier in use for numeric attributes.
   *
   * @return the classifier
   */
  public Classifier getNumericClassifier() {
    return m_numericClassifier;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String numericClassifierTipText() {
    return "Numeric classifier to use";
  }

  /**
   * Returns the default number of epochs.
   *
   * @return the default
   */
  protected int getDefaultNumEpochs() {
    return 100;
  }

  /**
   * Sets the number of epochs.
   *
   * @param value the epochs
   */
  public void setNumEpochs(int value) {
    m_numEpochs = value;
  }

  /**
   * Returns the number of epochs.
   *
   * @return the epochs
   */
  public int getNumEpochs() {
    return m_numEpochs;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String numEpochsTipText() {
    return "Max number of epochs";
  }

  /**
   * Returns the default value for the early termination value.
   *
   * @return the default
   */
  protected double getDefaultEpsilon() {
    return 5;
  }

  /**
   * Sets the early termination value.
   *
   * @param value the termination value
   */
  public void setEpsilon(double value) {
    m_epsilon = value;
  }

  /**
   * Returns the early termination value.
   *
   * @return the termination value
   */
  public double getEpsilon() {
    return m_epsilon;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String epsilonTipText() {
    return "Epsilon for early termination";
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
   * Computes the median.
   *
   * @param vals	the values to compute the median for
   * @return		the median
   */
  protected double median(double[] vals) {
    if (vals.length == 0)
      return 0.0;

    ArrayList<Double> newVals = new ArrayList<Double>(vals.length);
    for (double val : vals) {
      if (!Utils.isMissingValue(val))
	newVals.add(val);
    }

    if (newVals.size() == 0)
      return 0.0;

    Collections.sort(newVals);

    // if the array is even, get the avg of the middle two
    int midPoint = newVals.size() / 2;
    if (newVals.size() % 2 == 0)
      return (newVals.get(midPoint) + newVals.get(midPoint-1)) / 2.0;
    else
      return newVals.get(midPoint);
  }

  /**
   * Computes the mode.
   *
   * @param vals	the label indices to compute the mode for
   * @return		the mode
   */
  protected double mode(double[] vals) {
    if (vals.length == 0)
      return 0.0;

    Map<Double, Integer> counts = new HashMap<Double, Integer>();
    for (double num : vals) {
      if (Utils.isMissingValue(num))
	continue;
      if (counts.get(num) == null)
	counts.put(num, 1);
      else
	counts.put(num, counts.get(num) + 1);
    }

    double bestKey = 0;
    double bestVal = Double.NEGATIVE_INFINITY;
    Set<Double> keys = counts.keySet();
    for (double key : keys) {
      if (counts.get(key) > bestVal) {
	bestKey = key;
	bestVal = counts.get(key);
      }
    }

    return bestKey;
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
    Instances df = new Instances(data);
    int originalClassIndex = df.classIndex();

    for (int i = 0; i < df.numInstances(); i++)
      df.get(i).setClassValue(Double.NaN);

    /*
     * Step 2: Sort the variables according to the original amount of
     * missing values. We now assume that the variables are already sorted,
     * i.e. M(x1) >= ... >= M(x2), where M(xj) denotes the number of missing
     * cells in variable xj. Set I = {1..p}
     */

    /*
     * Step 4 prelim: Create a list of missing/observed indices
     * with respect to each potential attribute.
     */
    Pair[] numMissing = new Pair[df.numAttributes()];
    ArrayList<ArrayList<Integer>> missingIndices = new ArrayList<ArrayList<Integer>>();
    ArrayList<ArrayList<Integer>> observedIndices = new ArrayList<ArrayList<Integer>>();
    for (int l = 0; l < df.numAttributes(); l++) {
      int missingCount = 0;
      ArrayList<Integer> missing = new ArrayList<Integer>();
      ArrayList<Integer> observed = new ArrayList<Integer>();
      for (int i = 0; i < df.numInstances(); i++) {
	if (df.get(i).isMissing(l)) {
	  missing.add(i);
	  missingCount += 1;
	}
	else {
	  observed.add(i);
	}
      }
      missingIndices.add(missing);
      observedIndices.add(observed);
      numMissing[l] = new Pair(missingCount, l);
    }
    Arrays.sort(numMissing);

    /*
     * Step 1: Initialise the missing values using a simple imputation
     * technique (e.g. k-nearest neighbour or mean imputation).
     */
    for (int i = 0; i < df.numAttributes(); i++) {
      if (i == df.classIndex())
	continue;

      double[] vals = df.attributeToDoubleArray(i);
      double colMean;
      if (df.attribute(i).isNumeric())
	colMean = median(vals);
      else // it is nominal
	colMean = mode(vals);

      for (int y = 0; y < vals.length; y++) {
	if (Double.isNaN(df.get(y).value(i)) || Double.isInfinite(df.get(y).value(i)))
	  df.get(y).setValue(i, colMean);
      }
    }
    boolean[] isStable = new boolean[df.numAttributes()];
    for (int i = 0; i < df.numAttributes(); i++)
      isStable[i] = false;

    /*
     * Step 4: Create a matrix with the variables corresponding to
     * the observed and missing cells of x_l.
     */
    m_classifiers = new Classifier[df.numAttributes()];

    /*
     * Step 3: Set l = 0 (i.e. l = 1)
     */
    for (int epochs = 0; epochs < getNumEpochs(); epochs++) {
      for (Pair p : numMissing) {
	int l = p.index;
	if (p.value == 0) // none missing
	  continue;
	if (l == originalClassIndex)
	  continue;
	if (isStable[l])
	  continue;
	if (observedIndices.get(l).size() == 0)
	  continue;

	Instances observed = new Instances(data, 0);
	for (int i : observedIndices.get(l))
	  observed.add(df.get(i));
	observed.setClassIndex(l);

	Classifier cls;
	if (df.attribute(l).isNominal())
	  cls = AbstractClassifier.makeCopy(m_nominalClassifier);
	else
	  cls = AbstractClassifier.makeCopy(m_numericClassifier);

        /*
         * Step 5: Build the model. Then predict the missing class.
         */
	cls.buildClassifier(observed);
	m_classifiers[l] = cls;

	double sumOfSquares = 0;
	df.setClassIndex(l);
	for (int idx : missingIndices.get(l)) {
	  double currentClassValue = df.get(idx).value(l);
	  double newClassValue = m_classifiers[l].classifyInstance(df.get(idx));
	  df.get(idx).setValue(l, newClassValue);
	  sumOfSquares += Math.pow(currentClassValue - newClassValue, 2);
	}

	if (sumOfSquares < m_epsilon)
	  isStable[l] = true;

        /*
         * Step 6: Carry out steps 4-5 in turn for each l = 2..p.
         */
      }

      boolean allStable = true;
      for (int j = 0; j < isStable.length; j++) {
	if (j != originalClassIndex) {
	  if (!isStable[j]) {
	    allStable = false;
	    break;
	  }
	}
      }
      if (allStable)
	break;
    }

    // temp header for imputation time
    m_Header = new Instances(data, 0);

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
    Instance		result;
    int			i;

    result = (Instance) inst.copy();
    result.setDataset(m_Header);

    for (i = 0; i < result.numAttributes(); i++) {
      if (i == inst.classIndex())
	continue;
      if (result.isMissing(i)) {
	if (m_classifiers[i] != null) {
	  m_Header.setClassIndex(i);
	  result.setValue(i, m_classifiers[i].classifyInstance(result));
	}
      }
    }

    result.setDataset(m_OutputFormat);

    return result;
  }
}
