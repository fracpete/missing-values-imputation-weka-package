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
 * Regex.java
 * Copyright (C) 2021 University of Waikato, Hamilton, NZ
 */

package weka.filters.unsupervised.attribute.missingvaluesinjection;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 <!-- globalinfo-start -->
 * Replaces the values of nominal/string attributes to missing that match the regular expression.<br>
 * For more information on regular expressions, see:<br>
 * https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p>
 *
 * <pre> -expression &lt;regexp&gt;
 *  The regular expression to use; matches get replaced with missing values.
 *  (default: '\?')</pre>
 *
 * <pre> -update-header
 *  If enabled, the specified values will get removed as values from the attribute(s).
 *  (default: disable)</pre>
 *
 * <pre> -R &lt;col1,col2,...&gt;
 *  The selection of columns to use in the injection process, 'first' and 'last' are valid indices..
 *  (default: first-last)</pre>
 *
 * <pre> -V
 *  Inverts the matching sense.</pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Regex
  extends AbstractInjectionWithRange {

  /** the flag for the expression. */
  public final static String EXPRESSION = "expression";

  /** the flag for updating the heade. */
  public final static String UPDATE_HEADER = "update-header";

  /** the comma-separated list of values. */
  protected String m_Expression = getDefaultExpression();

  /** whether to update the header. */
  protected boolean m_UpdateHeader = false;

  /** the pattern to use. */
  protected transient Pattern m_Pattern;

  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  @Override
  public String globalInfo() {
    return "Replaces the values of nominal/string attributes to missing that match the regular expression.\n"
      + "For more information on regular expressions, see:\n"
      + "https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html";
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
      "\t" + expressionTipText() + "\n"
	+ "\t(default: '" + getDefaultExpression() + "')",
      EXPRESSION, 1, "-" + EXPRESSION + " <regexp>"));

    result.addElement(new Option(
      "\t" + updateHeaderTipText() + "\n"
	+ "\t(default: disable)",
      UPDATE_HEADER, 0, "-" + UPDATE_HEADER));

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

    if (!getExpression().isEmpty()) {
      result.add("-" + EXPRESSION);
      result.add("" + getExpression());
    }

    if (getUpdateHeader())
      result.add("-" + UPDATE_HEADER);

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

    tmpStr = Utils.getOption(EXPRESSION, options);
    if (!tmpStr.isEmpty())
      setExpression(tmpStr);
    else
      setExpression(getDefaultExpression());

    setUpdateHeader(Utils.getFlag(UPDATE_HEADER, options));

    super.setOptions(options);
  }

  /**
   * Returns the default expression.
   *
   * @return the default
   */
  protected String getDefaultExpression() {
    return "\\?";
  }

  /**
   * Sets the expression to use.
   *
   * @param value the regexp
   */
  public void setExpression(String value) {
    try {
      Pattern.compile(value);
      m_Expression = value;
    }
    catch (Exception e) {
      System.err.println("Invalid regexp: " + value + "\n" + e);
      e.printStackTrace();
    }
  }

  /**
   * Returns the expression in use.
   *
   * @return the regexp
   */
  public String getExpression() {
    return m_Expression;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String expressionTipText() {
    return "The regular expression to use; matches get replaced with missing values.";
  }

  /**
   * Sets whether to update the header, ie removing the specified values
   * from the attributes.
   *
   * @param value true if to update the header
   */
  public void setUpdateHeader(boolean value) {
    m_UpdateHeader = value;
  }

  /**
   * Returns whether to update the header, ie removing the specified values
   * from the attributes.
   *
   * @return true if to update the header
   */
  public boolean getUpdateHeader() {
    return m_UpdateHeader;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String updateHeaderTipText() {
    return "If enabled, the specified values will get removed as values from the attribute(s).";
  }

  /**
   * Hook method to perform some initializations before
   * {@link #doBuildInjection(Instances)} is called.
   *
   * @param data	the training data
   */
  @Override
  protected void initInjection(Instances data) {
    Integer[] 	indices;
    int		i;

    super.initInjection(data);

    // remove invalid attribute types
    for (int index: m_Indices) {
      if (data.attribute(index).isNominal() || data.attribute(index).isString())
	continue;
      m_IndicesSet.remove(index);
    }
    indices = m_IndicesSet.toArray(new Integer[0]);
    m_Indices = new int[indices.length];
    for (i = 0; i < indices.length; i++)
      m_Indices[i] = indices[i];
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
    Instances			result;
    ArrayList<Attribute>	atts;
    int				i;
    List<String>		labels;
    int				n;
    boolean			any;

    m_Pattern = Pattern.compile(m_Expression);

    if (m_UpdateHeader) {
      any = false;
      atts = new ArrayList<Attribute>();
      for (i = 0; i < data.numAttributes(); i++) {
	if (m_IndicesSet.contains(i) && data.attribute(i).isNominal()) {
	  labels = new ArrayList<String>();
	  for (n = 0; n < data.attribute(i).numValues(); n++) {
	    if (m_Pattern.matcher(data.attribute(i).value(n)).matches()) {
	      any = true;
	      continue;
	    }
	    labels.add(data.attribute(i).value(n));
	  }
	  atts.add(new Attribute(data.attribute(i).name(), labels));
	}
	else {
	  atts.add((Attribute) data.attribute(i).copy());
	}
      }
      if (!any)
	System.err.println(getClass().getName() + ": No attributes were updated!");
      result = new Instances(data.relationName(), atts, 0);
      result.setClassIndex(data.classIndex());
      return result;
    }
    else {
      return new Instances(data, 0);
    }
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
    Instance 		result;
    double[]		values;
    int			i;

    if (m_Pattern == null)
      m_Pattern = Pattern.compile(m_Expression);

    // adjust indices
    if (m_UpdateHeader) {
      values = inst.toDoubleArray().clone();
      for (i = 0; i < inst.numAttributes(); i++) {
	if (inst.attribute(i).isString())
	  values[i] = m_OutputFormat.attribute(i).addStringValue(inst.stringValue(i));
	else if (inst.attribute(i).isRelationValued())
	  values[i] = m_OutputFormat.attribute(i).addRelation(inst.relationalValue(i));
	if (m_IndicesSet.contains(i)) {
	  if (m_Pattern.matcher(inst.stringValue(i)).matches())
	    values[i] = Utils.missingValue();
	  else
	    values[i] = m_OutputFormat.attribute(i).indexOfValue(inst.stringValue(i));
	}
      }
      result = new DenseInstance(inst.weight(), values);
      result.setDataset(m_OutputFormat);
    }
    else {
      result = (Instance) inst.copy();
      for (int index : m_Indices) {
	if (m_Pattern.matcher(inst.stringValue(index)).matches())
	  result.setMissing(index);
      }
    }

    return result;
  }
}
