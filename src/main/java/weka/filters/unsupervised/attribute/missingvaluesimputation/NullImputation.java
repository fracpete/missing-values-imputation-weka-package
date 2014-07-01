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
 *    NullImputation.java
 *    Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute.missingvaluesimputation;

import weka.core.Instance;
import weka.core.Instances;

/** 
 * <!-- globalinfo-start -->
 * * Dummy, performs no imputation.
 * * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start -->
 * <!-- options-end -->
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class NullImputation
  extends AbstractImputation {

  /** for serialization. */
  private static final long serialVersionUID = -5845592824265489849L;

  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  @Override
  public String globalInfo() {
    return "Dummy, performs no imputation.";
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
    return new Instances(data, 0);
  }

  /**
   * Performs the actual imputation.
   *
   * @param inst the instance to perform imputation on
   * @return the updated instance
   * @throws Exception if the imputation fails, eg if not initialized.
   * @see #buildImputation(Instances)
   */
  @Override
  protected Instance doImpute(Instance inst) throws Exception {
    return inst;
  }
}
