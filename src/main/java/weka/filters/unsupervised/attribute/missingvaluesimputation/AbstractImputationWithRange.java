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
 * AbstractImputationWithRange.java
 * Copyright (C) 2014-2016 University of Waikato, Hamilton, New Zealand
 */
package weka.filters.unsupervised.attribute.missingvaluesimputation;

import weka.core.Instances;
import weka.core.Option;
import weka.core.Range;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

/**
 * Ancestor for imputation algorithms that work on a range of attributes only.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractImputationWithRange
  extends AbstractImputation {

  /** for serialization. */
  private static final long serialVersionUID = 3787465264006307917L;

  /** the flag for the range. */
  public final static String RANGE = "R";

  /** the flag for inverting the matching. */
  public final static String INVERT_MATCHING = "V";

  /** the range of attributes to work on. */
  protected Range m_Cols = new Range(getDefaultAttributeIndices());
  
  /** the indices to work on. */
  protected int[] m_Indices;

  /** the hashet of the indices to work on. */
  protected HashSet<Integer> m_IndicesSet;

  /**
   * Returns an enumeration describing the available options.
   * 
   * @return an enumeration of all the available options.
   */
  @Override
  public Enumeration<Option> listOptions() {
    Vector<Option> result = new Vector<Option>();

    result.addElement(new Option(
      "\t" + attributeIndicesTipText() + ".\n"
        + "\t(default: " + getDefaultAttributeIndices() + ")",
        RANGE, 1, "-" + RANGE + " <col1,col2,...>"));

    result.addElement(new Option(
	"\tInverts the matching sense.", 
	INVERT_MATCHING, 0, "-" + INVERT_MATCHING));

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

    result.add("-" + RANGE);
    result.add("" + m_Cols.getRanges());

    if (m_Cols.getInvert())
      result.add("-" + INVERT_MATCHING);

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

    tmpStr = Utils.getOption(RANGE, options);
    if (!tmpStr.isEmpty())
      setAttributeIndices(tmpStr);
    else
      setAttributeIndices(getDefaultAttributeIndices());

    setInvertSelection(Utils.getFlag(INVERT_MATCHING, options));

    super.setOptions(options);

    Utils.checkForRemainingOptions(options);
  }

  /**
   * Returns the default indices.
   *
   * @return the default
   */
  protected String getDefaultAttributeIndices() {
    return "first-last";
  }

  /**
   * Sets the columns to use, e.g., first-last or first-3,5-last
   * 
   * @param value the columns to use
   */
  public void setAttributeIndices(String value) {
    m_Cols.setRanges(value);
  }

  /**
   * Gets the selection of the columns, e.g., first-last or first-3,5-last
   * 
   * @return the selected indices
   */
  public String getAttributeIndices() {
    return m_Cols.getRanges();
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String attributeIndicesTipText() {
    return "The selection of columns to use in the imputation process, 'first' and 'last' are valid indices.";
  }

  /**
   * Sets whether the selection of the indices is inverted or not.
   * 
   * @param value the new invert setting
   */
  public void setInvertSelection(boolean value) {
    m_Cols.setInvert(value);
  }

  /**
   * Gets whether the selection of the columns is inverted.
   * 
   * @return true if the selection is inverted
   */
  public boolean getInvertSelection() {
    return m_Cols.getInvert();
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String invertSelectionTipText() {
    return "If enabled the selection of the columns is inverted.";
  }

  /**
   * Hook method to perform some initializations before 
   * {@link #doBuildImputation(Instances)} is called.
   * 
   * @param data	the training data
   */
  @Override
  protected void initImputation(Instances data) {
    super.initImputation(data);
    m_Cols.setUpper(data.numAttributes() - 1);
    m_Indices = m_Cols.getSelection();
    m_IndicesSet = new HashSet<Integer>();
    for (int index: m_Indices)
      m_IndicesSet.add(index);
  }
}
