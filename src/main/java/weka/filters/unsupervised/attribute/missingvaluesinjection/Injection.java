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
 *    Injection.java
 *    Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute.missingvaluesinjection;

import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;

/** 
 * Interface for injection algorithms.
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public interface Injection
  extends Serializable, CapabilitiesHandler {

  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  public String globalInfo();

  /**
   * Initializes the injection algorithm with the training data.
   *
   * @param data the training data
   * @return the potentially modified header
   * @throws Exception if the training fails
   */
  public Instances buildInjection(Instances data) throws Exception;

  /**
   * Performs the injection. Must be initialized beforehand.
   *
   * @param data the data to perform injection on
   * @return the updated dataset
   * @throws Exception if the injection fails, eg if not initialized.
   * @see #buildInjection(Instances)
   */
  public Instances inject(Instances data) throws Exception;

  /**
   * Performs the injection. Must be initialized beforehand.
   *
   * @param inst the instance to perform injection on
   * @return the updated instance
   * @throws Exception if the injection fails, eg if not initialized.
   * @see #buildInjection(Instances)
   */
  public Instance inject(Instance inst) throws Exception;
}
