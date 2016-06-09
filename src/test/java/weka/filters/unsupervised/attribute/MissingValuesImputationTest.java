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
 * Copyright (C) 2002-2016 University of Waikato
 */

package weka.filters.unsupervised.attribute;

import junit.framework.Test;
import junit.framework.TestSuite;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.AbstractFilterTest;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.missingvaluesimputation.MeansAndModes;
import weka.filters.unsupervised.attribute.missingvaluesimputation.NullImputation;

/**
 * Tests MissingValuesImputation. Run from the command line with:
 * <pre>
 * java weka.filters.unsupervised.attribute.MissingValuesImputationTest
 * </pre>
 * Use the following parameter for the JVM to set the correct directory
 * for the regression reference files:
 * <pre>
 * -Dweka.test.Regression.root=src/test/resources
 * </pre>
 *
 * @author Len Trigg
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class MissingValuesImputationTest
  extends AbstractFilterTest {
  
  /**
   * Initializes the test.
   * 
   * @param name 	the name of the test
   */
  public MissingValuesImputationTest(String name) { 
    super(name);
    System.setProperty("weka.test.Regression.root", "src/test/resources");
  }

  /**
   * Creates a default filter.
   * 
   * @return		the filter
   */
  @Override
  public Filter getFilter() {
    return new MissingValuesImputation();
  }

  /**
   * Tests the default setup, ie {@link NullImputation}.
   */
  public void testTypical() {
    Instances result = useFilter();
    // Number of attributes and instances shouldn't change
    assertEquals(m_Instances.numAttributes(), result.numAttributes());
    assertEquals(m_Instances.numInstances(), result.numInstances());
    for (int j = 0; j < m_Instances.numAttributes(); j++) {
      Attribute inatt = m_Instances.attribute(j);
      Attribute outatt = result.attribute(j);
      for (int i = 0; i < m_Instances.numInstances(); i++) {
        if (m_Instances.attribute(j).isString()) {
          if (m_Instances.instance(i).isMissing(j)) {
            assertTrue("Missing values in strings cannot be replaced",
                   result.instance(i).isMissing(j));
          } 
          else {
            assertEquals("String values should not have changed",
                         inatt.value((int)m_Instances.instance(i).value(j)),
                         outatt.value((int)result.instance(i).value(j)));
          }
        }
      }
    }
  }

  /**
   * Tests the filter using {@link MeansAndModes}.
   */
  public void testMeansAndModes() {
    m_Filter = new MissingValuesImputation();
    ((MissingValuesImputation) m_Filter).setAlgorithm(new MeansAndModes());
    Instances result = useFilter();
    // Number of attributes and instances shouldn't change
    assertEquals(m_Instances.numAttributes(), result.numAttributes());
    assertEquals(m_Instances.numInstances(), result.numInstances());
    for (int j = 0; j < m_Instances.numAttributes(); j++) {
      Attribute inatt = m_Instances.attribute(j);
      Attribute outatt = result.attribute(j);
      for (int i = 0; i < m_Instances.numInstances(); i++) {
        if (m_Instances.attribute(j).isString()) {
          if (m_Instances.instance(i).isMissing(j)) {
            assertTrue("Missing values in strings cannot be replaced",
                   result.instance(i).isMissing(j));
          } 
          else {
            assertEquals("String values should not have changed",
                         inatt.value((int)m_Instances.instance(i).value(j)),
                         outatt.value((int)result.instance(i).value(j)));
          }
        } 
        else {
          assertTrue("All non-string missing values should have been replaced",
                 !result.instance(i).isMissing(j));
        }
      }
    }
  }

  /**
   * Creates a test suite.
   * 
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(MissingValuesImputationTest.class);
  }

  /**
   * Executes the test from commandline.
   * 
   * @param args	ignored
   */
  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }
}
