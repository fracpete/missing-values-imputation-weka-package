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
 * UserSuppliedValues.java
 * Copyright (C) 2016-2021 University of Waikato, Hamilton, NZ
 */

package weka.filters.unsupervised.attribute.missingvaluesimputation;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.SingleIndex;
import weka.core.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Replaces missing values with the specified values on the selected range of attributes.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p>
 * 
 * <pre> -numeric &lt;double&gt;
 *  The replacement value for missing values of numeric attributes.
 *  (default: 0.0)</pre>
 * 
 * <pre> -date &lt;string&gt;
 *  The replacement value for missing values of date attributes.
 *  (default: 2000-01-01)</pre>
 * 
 * <pre> -date-format &lt;java.text.SimpleDateFormat string&gt;
 *  The format string for parsing the date replacement value.
 *  (default: yyyy-MM-dd)</pre>
 * 
 * <pre> -nominal &lt;string&gt;
 *  The index of the label to use as replacement.
 *  (default: first)</pre>
 * 
 * <pre> -R &lt;col1,col2,...&gt;
 *  The selection of columns to use in the imputation process, 'first' and 'last' are valid indices..
 *  (default: first-last)</pre>
 * 
 * <pre> -V
 *  Inverts the matching sense.</pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class UserSuppliedValues
  extends AbstractImputationWithRange {

  /** the flag for the numeric value. */
  public final static String NUMERIC = "numeric";

  /** the flag for the date value. */
  public final static String DATE = "date";

  /** the flag for the date format value. */
  public final static String DATE_FORMAT = "date-format";

  /** the flag for the nominal label index. */
  public final static String NOMINAL = "nominal";

  /** the numeric replacement value. */
  protected double m_Numeric = getDefaultNumeric();

  /** the date replacement value. */
  protected String m_Date = getDefaultDate();

  /** the format of the date replacement value. */
  protected String m_DateFormat = getDefaultDateFormat();

  /** the parsed date value. */
  protected long m_DateValue;

  /** the nominal replacement value. */
  protected SingleIndex m_Nominal = new SingleIndex(getDefaultNominal());

  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  @Override
  public String globalInfo() {
    return
      "Replaces missing values with the specified values on the selected "
        + "range of attributes.";
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
      "\t" + numericTipText() + "\n"
      + "\t(default: " + getDefaultNumeric() + ")",
      NUMERIC, 1, "-" + NUMERIC + " <double>"));

    result.addElement(new Option(
      "\t" + dateTipText() + "\n"
      + "\t(default: " + getDefaultDate() + ")",
      DATE, 1, "-" + DATE + " <string>"));

    result.addElement(new Option(
      "\t" + dateFormatTipText() + "\n"
      + "\t(default: " + getDefaultDateFormat() + ")",
      DATE_FORMAT, 1, "-" + DATE_FORMAT + " <java.text.SimpleDateFormat string>"));

    result.addElement(new Option(
      "\t" + nominalTipText() + "\n"
      + "\t(default: " + getDefaultNominal() + ")",
      NOMINAL, 1, "-" + NOMINAL + " <string>"));

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

    result.add("-" + NUMERIC);
    result.add("" + m_Numeric);

    result.add("-" + DATE);
    result.add(m_Date);

    result.add("-" + DATE_FORMAT);
    result.add(m_DateFormat);

    result.add("-" + NOMINAL);
    result.add(m_Nominal.getSingleIndex());

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

    tmpStr = Utils.getOption(NUMERIC, options);
    if (!tmpStr.isEmpty())
      setNumeric(Double.parseDouble(tmpStr));
    else
      setNumeric(getDefaultNumeric());

    tmpStr = Utils.getOption(DATE, options);
    if (!tmpStr.isEmpty())
      setDate(tmpStr);
    else
      setDate(getDefaultDate());

    tmpStr = Utils.getOption(DATE_FORMAT, options);
    if (!tmpStr.isEmpty())
      setDateFormat(tmpStr);
    else
      setDateFormat(getDefaultDateFormat());

    tmpStr = Utils.getOption(NOMINAL, options);
    if (!tmpStr.isEmpty())
      setNominal(tmpStr);
    else
      setNominal(getDefaultNominal());

    super.setOptions(options);
  }

  /**
   * Returns the default numeric value.
   *
   * @return		the default
   */
  protected double getDefaultNumeric() {
    return 0.0;
  }

  /**
   * Sets the numeric replacement value.
   *
   * @param value 	the value
   */
  public void setNumeric(double value) {
    m_Numeric = value;
  }

  /**
   * Returns the numeric replacement value.
   *
   * @return		the value
   */
  public double getNumeric() {
    return m_Numeric;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String numericTipText() {
    return "The replacement value for missing values of numeric attributes.";
  }

  /**
   * Returns the default date value.
   *
   * @return		the default
   */
  protected String getDefaultDate() {
    return "2000-01-01";
  }

  /**
   * Sets the date replacement value.
   *
   * @param value 	the value
   */
  public void setDate(String value) {
    m_Date = value;
  }

  /**
   * Returns the date replacement value.
   *
   * @return		the value
   */
  public String getDate() {
    return m_Date;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String dateTipText() {
    return "The replacement value for missing values of date attributes.";
  }

  /**
   * Returns the default date format.
   *
   * @return		the default
   */
  protected String getDefaultDateFormat() {
    return "yyyy-MM-dd";
  }

  /**
   * Sets the format of the date replacement value.
   *
   * @param value 	the format
   */
  public void setDateFormat(String value) {
    m_DateFormat = value;
  }

  /**
   * Returns the format of the date replacement value.
   *
   * @return		the format
   */
  public String getDateFormat() {
    return m_DateFormat;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String dateFormatTipText() {
    return "The format string for parsing the date replacement value.";
  }

  /**
   * Returns the default label index.
   *
   * @return		the default
   */
  protected String getDefaultNominal() {
    return "first";
  }

  /**
   * Sets the index of the replacement label.
   *
   * @param value 	the index
   */
  public void setNominal(String value) {
    m_Nominal.setSingleIndex(value);
  }

  /**
   * Returns the index of the replacement label.
   *
   * @return		the index
   */
  public String getNominal() {
    return m_Nominal.getSingleIndex();
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String nominalTipText() {
    return "The index of the label to use as replacement.";
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
    SimpleDateFormat	formatter;

    formatter   = new SimpleDateFormat(m_DateFormat);
    m_DateValue = formatter.parse(m_Date).getTime();

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
    boolean		missing;
    int			index;
    Attribute		att;

    result = (Instance) inst.copy();

    // any missing?
    missing = false;
    for (i = 0; i < inst.numValues(); i++) {
      if (inst.isMissingSparse(i)) {
	missing = true;
	break;
      }
    }

    if (missing) {
      for (i = 0; i < result.numValues(); i++) {
	index = result.index(i);
	if (m_IndicesSet.contains(index) && result.isMissingSparse(i)) {
	  att = result.attributeSparse(i);
	  switch (att.type()) {
	    case Attribute.NUMERIC:
	      result.setValueSparse(i, m_Numeric);
	      break;

	    case Attribute.DATE:
	      result.setValueSparse(i, m_DateValue);
	      break;

	    case Attribute.NOMINAL:
	      m_Nominal.setUpper(att.numValues() - 1);
	      result.setValueSparse(i, m_Nominal.getIndex());
	      break;

	    default:
	      throw new IllegalStateException("Unhandled attribute type: " + Attribute.typeToString(att.type()));
	  }
	}
      }
    }

    return result;
  }
}
