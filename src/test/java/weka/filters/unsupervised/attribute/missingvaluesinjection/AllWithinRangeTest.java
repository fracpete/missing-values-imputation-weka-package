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
 * AllWithinRangeTest.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package weka.filters.unsupervised.attribute.missingvaluesinjection;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Tests the AllWithinRange algorithm.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class AllWithinRangeTest
  extends AbstractInjectionTest {

  /**
   * Constructs the <code>AbstractInjectionTest</code>. Called by subclasses.
   *
   * @param name the name of the test class
   */
  public AllWithinRangeTest(String name) {
    super(name);
  }

  /**
   * Returns the path of the default dataset to use.
   *
   * @return the path
   */
  @Override
  protected String getDefaultDataset() {
    return "weka/filters/unsupervised/attribute/missingvaluesinjection/data/iris.arff";
  }

  /**
   * Used to create an instance of a specific imputation.
   *
   * @return a suitably configured <code>Injection</code> value
   */
  @Override
  public Injection getInjection() {
    AllWithinRange 	result;

    result = new AllWithinRange();
    result.setAttributeIndices("1,3");

    return result;
  }

  /**
   *
   * Returns a test suite.
   *
   * @return		the test suite
   */
  public static Test suite() {
    return new TestSuite(AllWithinRangeTest.class);
  }

  /**
   * Runs the test from commandline.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    TestRunner.run(suite());
  }
}
