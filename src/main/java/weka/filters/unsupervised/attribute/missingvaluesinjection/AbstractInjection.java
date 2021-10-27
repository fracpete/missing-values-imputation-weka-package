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
 *    AbstractInjection.java
 *    Copyright (C) 2016-2021 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute.missingvaluesinjection;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.core.WekaException;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

/** 
 * Ancestor for injection algorithms.
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractInjection
  implements Injection, OptionHandler {

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
    Utils.checkForRemainingOptions(options);
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
   * Hook method to perform some initializations before 
   * {@link #doBuildInjection(Instances)} is called.
   * <p/>
   * Default implementation does nothing.
   * 
   * @param data	the training data
   */
  protected void initInjection(Instances data) {
  }

  /**
   * Performs the actual initialization of the injection algorithm with the
   * training data.
   *
   * @param data the training data
   * @return the potentially modified header
   * @throws Exception if the training fails
   */
  protected abstract Instances doBuildInjection(Instances data) throws Exception;

  /**
   * Initializes the injection algorithm with the training data.
   *
   * @param data the training data
   * @return the potentially modified header
   * @throws Exception if the training fails
   * @see #doBuildInjection(Instances)
   */
  @Override
  public Instances buildInjection(Instances data) throws Exception {
    m_OutputFormat = null;
    getCapabilities(data).testWithFail(data);
    initInjection(data);
    m_OutputFormat = doBuildInjection(data);
    m_Initialized  = true;
    
    return m_OutputFormat;
  }

  /**
   * Performs the actual injection.
   *
   * @param data the data to perform injection on
   * @return the updated dataset
   * @throws Exception if the injection fails, eg if not initialized.
   * @see #buildInjection(Instances)
   */
  protected Instances doInject(Instances data) throws Exception {
    Instances result;
    int i;

    result = new Instances(data, data.numInstances());
    for (i = 0; i < data.numInstances(); i++)
      result.add(doInject(data.instance(i)));

    return result;
  }

  /**
   * Performs the injection. Must be initialized beforehand.
   *
   * @param data the data to perform injection on
   * @return the updated dataset
   * @throws Exception if the injection fails, eg if not initialized.
   * @see #buildInjection(Instances)
   * @see #doInject(Instances)
   */
  @Override
  public Instances inject(Instances data) throws Exception {
    if (!m_Initialized)
      throw new WekaException("Injection algorithm '" + getClass().getName() + "' not initialized!");
    return doInject(data);
  }

  /**
   * Performs the actual injection.
   *
   * @param inst the instance to perform injection on
   * @return the updated instance
   * @throws Exception if the injection fails, eg if not initialized.
   * @see #buildInjection(Instances)
   */
  protected abstract Instance doInject(Instance inst) throws Exception;

  /**
   * Performs the injection. Must be initialized beforehand.
   *
   * @param inst the instance to perform injection on
   * @return the updated instance
   * @throws Exception if the injection fails, eg if not initialized.
   * @see #buildInjection(Instances)
   */
  @Override
  public Instance inject(Instance inst) throws Exception {
    if (!m_Initialized)
      throw new WekaException("Injection algorithm '" + getClass().getName() + "' not initialized!");
    return doInject(inst);
  }
}
