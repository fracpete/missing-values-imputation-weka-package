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
 *    AbstractImputation.java
 *    Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute.missingvaluesimputation;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.WekaException;

/** 
 * Ancestor for imputation algorithms.
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractImputation
  implements Imputation, OptionHandler, CapabilitiesHandler, Serializable {

  /** for serialization. */
  private static final long serialVersionUID = 8509594940625168327L;

  /** whether the algorithm has been initialized. */
  protected boolean m_Initialized = false;

  /** the output format. */
  protected Instances m_OutputFormat;
  
  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  @Override
  public abstract String globalInfo();

  /**
   * Returns an enumeration describing the available options.
   * 
   * @return an enumeration of all the available options.
   */
  @Override
  public Enumeration<Option> listOptions() {
    return new Vector<Option>().elements();
  }

  /**
   * Gets the current settings of the filter.
   * 
   * @return an array of strings suitable for passing to setOptions
   */
  @Override
  public String[] getOptions() {
    return new String[0];
  }

  /**
   * Parses a given list of options.
   * 
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  @Override
  public void setOptions(String[] options) throws Exception {
  }

  /**
   * Returns the Capabilities of this filter. Derived filters have to override
   * this method to enable capabilities.
   * 
   * @return the capabilities of this object
   * @see Capabilities
   */
  @Override
  public Capabilities getCapabilities() {
    Capabilities result;

    result = new Capabilities(this);
    result.enableAll();
    result.enable(Capability.NO_CLASS);

    result.setMinimumNumberInstances(0);

    return result;
  }

  /**
   * Returns the Capabilities of this algorithm, customized based on the data.
   * I.e., if removes all class capabilities, in case there's not class
   * attribute present or removes the NO_CLASS capability, in case that there's
   * a class present.
   * 
   * @param data the data to use for customization
   * @return the capabilities of this object, based on the data
   * @see #getCapabilities()
   */
  public Capabilities getCapabilities(Instances data) {
    Capabilities result;
    Capabilities classes;
    Iterator<Capability> iter;
    Capability cap;

    result = getCapabilities();

    // no class? -> remove all class capabilites apart from NO_CLASS
    if (data.classIndex() == -1) {
      classes = result.getClassCapabilities();
      iter = classes.capabilities();
      while (iter.hasNext()) {
        cap = iter.next();
        if (cap != Capability.NO_CLASS) {
          result.disable(cap);
          result.disableDependency(cap);
        }
      }
    }
    // class? -> remove NO_CLASS
    else {
      result.disable(Capability.NO_CLASS);
      result.disableDependency(Capability.NO_CLASS);
    }

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
  protected abstract Instances doBuildImputation(Instances data) throws Exception;

  /**
   * Initializes the imputation algorithm with the training data.
   *
   * @param data the training data
   * @return the potentially modified header
   * @throws Exception if the training fails
   * @see #doBuildImputation(Instances)
   */
  @Override
  public Instances buildImputation(Instances data) throws Exception {
    m_OutputFormat = null;
    getCapabilities(data).testWithFail(data);
    m_OutputFormat = doBuildImputation(data);
    m_Initialized  = true;
    
    return m_OutputFormat;
  }

  /**
   * Performs the actual imputation.
   *
   * @param data the data to perform imputation on
   * @return the updated dataset
   * @throws Exception if the imputation fails, eg if not initialized.
   * @see #buildImputation(Instances)
   */
  protected Instances doImpute(Instances data) throws Exception {
    Instances result;
    int i;

    result = new Instances(data, data.numInstances());
    for (i = 0; i < data.numInstances(); i++)
      result.add(doImpute(data.instance(i)));

    return result;
  }

  /**
   * Performs the imputation. Must be initialized beforehand.
   *
   * @param data the data to perform imputation on
   * @return the updated dataset
   * @throws Exception if the imputation fails, eg if not initialized.
   * @see #buildImputation(Instances)
   * @see #doImpute(Instances)
   */
  @Override
  public Instances impute(Instances data) throws Exception {
    if (!m_Initialized)
      throw new WekaException("Imputation algorithm '" + getClass().getName() + "' not initialized!");
    return doImpute(data);
  }

  /**
   * Performs the actual imputation.
   *
   * @param inst the instance to perform imputation on
   * @return the updated instance
   * @throws Exception if the imputation fails, eg if not initialized.
   * @see #buildImputation(Instances)
   */
  protected abstract Instance doImpute(Instance inst) throws Exception;

  /**
   * Performs the imputation. Must be initialized beforehand.
   *
   * @param inst the instance to perform imputation on
   * @return the updated instance
   * @throws Exception if the imputation fails, eg if not initialized.
   * @see #buildImputation(Instances)
   */
  @Override
  public Instance impute(Instance inst) throws Exception {
    if (!m_Initialized)
      throw new WekaException("Imputation algorithm '" + getClass().getName() + "' not initialized!");
    return doImpute(inst);
  }
}
