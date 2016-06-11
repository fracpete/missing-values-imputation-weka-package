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
 *    ClassOnly.java
 *    Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute.missingvaluesinjection;

import weka.core.Instance;
import weka.core.Instances;

/** 
 <!-- globalinfo-start -->
 * Only sets the class to missing.
 * <br><br>
 <!-- globalinfo-end -->
 * 
 <!-- options-start -->
 <!-- options-end -->
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ClassOnly
  extends AbstractInjection {

  /** for serialization. */
  private static final long serialVersionUID = -5845592824265489849L;

  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  @Override
  public String globalInfo() {
    return "Only sets the class to missing.";
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
    return new Instances(data, 0);
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

    if (inst.classIndex() == -1)
      return inst;

    result = (Instance) inst.copy();
    result.setClassMissing();

    return result;
  }
}
