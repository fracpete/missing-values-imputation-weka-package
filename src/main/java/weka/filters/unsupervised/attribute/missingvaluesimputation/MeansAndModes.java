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
 *    MeansAndModes.java
 *    Copyright (C) 1999-2014 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute.missingvaluesimputation;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.Utils;

/** 
 * <!-- globalinfo-start -->
 * * Replaces all missing values for nominal and numeric attributes in a dataset with the modes and means from the training data.
 * * <br><br>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start -->
 * <!-- options-end -->
 * 
 * @author Eibe Frank (eibe@cs.waikato.ac.nz) 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @see weka.filters.unsupervised.attribute.ReplaceMissingValues
 */
public class MeansAndModes
  extends AbstractImputation {
  
  /** for serialization. */
  private static final long serialVersionUID = -3584692959239101972L;
  
  /** The modes and means */
  protected double[] m_ModesAndMeans = null;

  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  @Override
  public String globalInfo() {
    return "Replaces all missing values for nominal and numeric attributes in a "
      + "dataset with the modes and means from the training data.";
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
    result.enableAllAttributes();
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
    double sumOfWeights = data.sumOfWeights();
    double[][] counts = new double[data.numAttributes()][];
    for (int i = 0; i < data.numAttributes(); i++) {
      if (data.attribute(i).isNominal()) {
        counts[i] = new double[data.attribute(i).numValues()];
        if (counts[i].length > 0)
          counts[i][0] = sumOfWeights;
      }
    }
    double[] sums = new double[data.numAttributes()];
    for (int i = 0; i < sums.length; i++) {
      sums[i] = sumOfWeights;
    }
    double[] results = new double[data.numAttributes()];
    for (int j = 0; j < data.numInstances(); j++) {
      Instance inst = data.instance(j);
      for (int i = 0; i < inst.numValues(); i++) {
        if (!inst.isMissingSparse(i)) {
          double value = inst.valueSparse(i);
          if (inst.attributeSparse(i).isNominal()) {
            if (counts[inst.index(i)].length > 0) {
              counts[inst.index(i)][(int)value] += inst.weight();
              counts[inst.index(i)][0] -= inst.weight();
            }
          } else if (inst.attributeSparse(i).isNumeric()) {
            results[inst.index(i)] += inst.weight() * inst.valueSparse(i);
          }
        } else {
          if (inst.attributeSparse(i).isNominal()) {
            if (counts[inst.index(i)].length > 0) {
              counts[inst.index(i)][0] -= inst.weight();
            }
          } else if (inst.attributeSparse(i).isNumeric()) {
            sums[inst.index(i)] -= inst.weight();
          }
        }
      }
    }
    m_ModesAndMeans = new double[data.numAttributes()];
    for (int i = 0; i < data.numAttributes(); i++) {
      if (data.attribute(i).isNominal()) {
        if (counts[i].length == 0)
          m_ModesAndMeans[i] = Utils.missingValue();
        else
          m_ModesAndMeans[i] = (double)Utils.maxIndex(counts[i]);
      } else if (data.attribute(i).isNumeric()) {
        if (Utils.gr(sums[i], 0)) {
          m_ModesAndMeans[i] = results[i] / sums[i];
        }
      }
    }
    
    return new Instances(data, 0);
  }

  /**
   * Performs the actual imputation.
   *
   * @param instance the instance to perform imputation on
   * @return the updated instance
   * @throws Exception if the imputation fails, eg if not initialized.
   * @see #buildImputation(Instances)
   */
  @Override
  protected Instance doImpute(Instance instance) throws Exception {
    Instance inst = null;
    if (instance instanceof SparseInstance) {
      double []vals = new double[instance.numValues()];
      int []indices = new int[instance.numValues()];
      int num = 0;
      for (int j = 0; j < instance.numValues(); j++) {
	if (instance.isMissingSparse(j) &&
	    (instance.classIndex() != instance.index(j)) &&
	    (instance.attributeSparse(j).isNominal() ||
	     instance.attributeSparse(j).isNumeric())) {
	  if (m_ModesAndMeans[instance.index(j)] != 0.0) {
	    vals[num] = m_ModesAndMeans[instance.index(j)];
	    indices[num] = instance.index(j);
	    num++;
	  } 
	} else {
	  vals[num] = instance.valueSparse(j);
	  indices[num] = instance.index(j);
	  num++;
	}
      } 
      if (num == instance.numValues()) {
	inst = new SparseInstance(instance.weight(), vals, indices,
                                  instance.numAttributes());
      } else {
	double []tempVals = new double[num];
	int []tempInd = new int[num];
	System.arraycopy(vals, 0, tempVals, 0, num);
	System.arraycopy(indices, 0, tempInd, 0, num);
	inst = new SparseInstance(instance.weight(), tempVals, tempInd,
                                  instance.numAttributes());
      }
    } else {
      double []vals = new double[instance.numAttributes()];
      for (int j = 0; j < instance.numAttributes(); j++) {
	if (instance.isMissing(j) &&
	    (instance.classIndex() != j) &&
	    (instance.attribute(j).isNominal() ||
		instance.attribute(j).isNumeric())) {
	  vals[j] = m_ModesAndMeans[j]; 
	} else {
	  vals[j] = instance.value(j);
	}
      } 
      inst = new DenseInstance(instance.weight(), vals);
    } 
    inst.setDataset(instance.dataset());
    return inst;
  }
}
