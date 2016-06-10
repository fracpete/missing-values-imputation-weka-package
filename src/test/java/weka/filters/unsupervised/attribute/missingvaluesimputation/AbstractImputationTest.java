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
 * Copyright (C) 2016 University of Waikato
 */

package weka.filters.unsupervised.attribute.missingvaluesimputation;

import junit.framework.TestCase;
import weka.core.Attribute;
import weka.core.CheckGOE;
import weka.core.CheckOptionHandler;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.SerializationHelper;
import weka.test.Regression;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Abstract Test class for Imputations.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 12375 $
 */
public abstract class AbstractImputationTest
  extends TestCase {

  /** The algorithm to be tested */
  protected Imputation m_Imputation;

  /** A set of instances to test with */
  protected Instances m_Instances;
  
  /** the OptionHandler tester */
  protected CheckOptionHandler m_OptionTester;

  /** for testing GOE stuff */
  protected CheckGOE m_GOETester;

  /**
   * Constructs the <code>AbstractImputationTest</code>. Called by subclasses.
   *
   * @param name the name of the test class
   */
  public AbstractImputationTest(String name) {
    super(name);
    System.setProperty("weka.test.Regression.root", "src/test/resources");
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception if an error occurs
   */
  protected void setUp() throws Exception {
    String	classIndex;

    super.setUp();

    m_Imputation   = getImputation();
    m_OptionTester = getOptionTester();
    m_GOETester    = getGOETester();
    m_Instances    = new Instances(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(getDefaultDataset()))));
    classIndex     = getDefaultClassIndex();
    if (classIndex.equals("first"))
      m_Instances.setClassIndex(0);
    else if (classIndex.equals("last"))
      m_Instances.setClassIndex(m_Instances.numAttributes() - 1);
    else if (!classIndex.isEmpty())
      m_Instances.setClassIndex(Integer.parseInt(classIndex) - 1);
  }

  /**
   * Called by JUnit after each test method.
   *
   * @throws Exception if an error occurs
   */
  protected void tearDown() throws Exception {
    m_Imputation   = null;
    m_Instances    = null;
    m_OptionTester = null;
    m_GOETester    = null;

    super.tearDown();
  }

  /**
   * Returns the path of the default dataset to use.
   *
   * @return the path
   */
  protected abstract String getDefaultDataset();

  /**
   * Returns the class index of the default dataset.
   *
   * @return the class index
   */
  protected String getDefaultClassIndex() {
    return "";
  }

  /**
   * Configures the CheckOptionHandler uses for testing the optionhandling.
   * Sets the scheme to test.
   * 
   * @return	the fully configured CheckOptionHandler
   */
  protected CheckOptionHandler getOptionTester() {
    CheckOptionHandler		result;
    
    result = new CheckOptionHandler();
    if (getImputation() instanceof OptionHandler)
      result.setOptionHandler((OptionHandler) getImputation());
    else
      result.setOptionHandler(null);
    result.setUserOptions(new String[0]);
    result.setSilent(true);
    
    return result;
  }
  
  /**
   * Configures the CheckGOE used for testing GOE stuff.
   * Sets the Imputation returned from the getImputation() method.
   * 
   * @return	the fully configured CheckGOE
   * @see	#getImputation()
   */
  protected CheckGOE getGOETester() {
    CheckGOE		result;
    
    result = new CheckGOE();
    result.setObject(getImputation());
    result.setSilent(true);
    
    return result;
  }

  /**
   * Used to create an instance of a specific imputation.
   *
   * @return a suitably configured <code>Imputation</code> value
   */
  public abstract Imputation getImputation();

  /**
   * Simple method to return the imputed set of test instances after
   * passing through the test algorithm. m_Imputation contains the algorithm
   * and m_Instances contains the test instances.
   *
   * @return the Instances after applying the algorithm we have set
   * up to test.  
   */
  protected Instances useImputation() {
    Instances result = null;
    Instances icopy = new Instances(m_Instances);
    try {
      result = m_Imputation.buildImputation(icopy);
      assertNotNull(result);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      fail("Exception thrown on buildImputation(): \n" + ex.getMessage());
    }
    try {
      result = m_Imputation.impute(icopy);
      assertNotNull(result);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      fail("Exception thrown on impute(): \n" + ex.getMessage());
    }
    return result;
  }

  /**
   * tests whether the scheme declares a serialVersionUID.
   */
  public void testSerialVersionUID() {
    if (SerializationHelper.needsUID(m_Imputation.getClass()))
      fail("Doesn't declare serialVersionUID!");
  }

  /**
   * Performs a regression test: creates reference file if necessary, if
   * regression file exists, it compares current output with stored one.
   */
  public void testRegression() {
    Regression reg = new Regression(this.getClass());
    Instances result = useImputation();
    reg.println(result.toString());
    try {
      String diff = reg.diff();
      if (diff == null) {
        System.err.println("Warning: No reference available, creating."); 
      } else if (!diff.equals("")) {
        fail("Regression test failed. Difference:\n" + diff);
      }
    } catch (java.io.IOException ex) {
      fail("Problem during regression testing.\n" + ex);
    }

    reg = new Regression(this.getClass());

    Instances icopy = new Instances(m_Instances);
    try {
      result = m_Imputation.buildImputation(icopy);
      assertNotNull(result);
    } catch (Exception ex) {
      ex.printStackTrace();
      fail("Exception thrown on buildImputation(): \n" + ex.getMessage());
    }
    try {
      result = m_Imputation.impute(icopy);
      assertNotNull(result);
    } catch (Exception ex) {
      ex.printStackTrace();
      fail("Exception thrown on impute(): \n" + ex.getMessage());
    }
    reg.println(result.toString());
    try {
      String diff = reg.diff();
      if (diff == null) {
        System.err.println("Warning: No reference available, creating."); 
      } else if (!diff.equals("")) {
        fail("Regression test failed when using deprecated methods. Difference:\n" + diff);
      }
    } catch (java.io.IOException ex) {
      fail("Problem during regression testing.\n" + ex);
    }
  }

  /**
   * tests the listing of the options
   */
  public void testListOptions() {
    if (m_OptionTester.getOptionHandler() != null) {
      if (!m_OptionTester.checkListOptions())
	fail("Options cannot be listed via listOptions.");
    }
  }
  
  /**
   * tests the setting of the options
   */
  public void testSetOptions() {
    if (m_OptionTester.getOptionHandler() != null) {
      if (!m_OptionTester.checkSetOptions())
	fail("setOptions method failed.");
    }
  }
  
  /**
   * tests whether the default settings are processed correctly
   */
  public void testDefaultOptions() {
    if (m_OptionTester.getOptionHandler() != null) {
      if (!m_OptionTester.checkDefaultOptions())
	fail("Default options were not processed correctly.");
    }
  }
  
  /**
   * tests whether there are any remaining options
   */
  public void testRemainingOptions() {
    if (m_OptionTester.getOptionHandler() != null) {
      if (!m_OptionTester.checkRemainingOptions())
	fail("There were 'left-over' options.");
    }
  }
  
  /**
   * tests the whether the user-supplied options stay the same after setting.
   * getting, and re-setting again.
   * 
   * @see 	#getOptionTester()
   */
  public void testCanonicalUserOptions() {
    if (m_OptionTester.getOptionHandler() != null) {
      if (!m_OptionTester.checkCanonicalUserOptions())
	fail("setOptions method failed");
    }
  }
  
  /**
   * tests the resetting of the options to the default ones
   */
  public void testResettingOptions() {
    if (m_OptionTester.getOptionHandler() != null) {
      if (!m_OptionTester.checkSetOptions())
	fail("Resetting of options failed");
    }
  }

  /**
   * tests for a globalInfo method
   */
  public void testGlobalInfo() {
    if (!m_GOETester.checkGlobalInfo())
      fail("No globalInfo method");
  }
  
  /**
   * tests the tool tips
   */
  public void testToolTips() {
    if (!m_GOETester.checkToolTips())
      fail("Tool tips inconsistent");
  }

  /**
   * Compares the two datasets.
   *
   * @param data1	first dataset
   * @param data2	second dataset
   * @return		null if the same, otherwise first difference
   */
  protected String compareDatasets(Instances data1, Instances data2) {
    int		x;
    int		y;
    Comparable	c1;
    Comparable	c2;

    if (data1.numInstances() != data2.numInstances())
      System.err.println(
	getName() + " [compareDatasets] datasets differ in number of instances: "
	  + data1.numInstances() + " != " + data2.numInstances());
    if (data1.numAttributes() != data2.numAttributes())
      System.err.println(
	getName() + " [compareDatasets] datasets differ in number of attributes: "
	  + data1.numAttributes() + " != " + data2.numAttributes());
    for (x = 0; x < data1.numAttributes() && x < data2.numAttributes(); x++) {
      if (data1.attribute(x).type() != data2.attribute(x).type()) {
	System.err.println(
	  getName() + " [compareDatasets] datasets differ in attribute type at " + (x+1) + ": "
	    + Attribute.typeToString(data1.attribute(x).type()) + " != " + Attribute.typeToString(data2.attribute(x).type()));
      }
    }

    for (y = 0; y < data1.numInstances() && y < data2.numInstances(); y++) {
      for (x = 0; x < data1.numAttributes() && x < data2.numAttributes(); x++) {
	if (data1.attribute(x).type() == data2.attribute(x).type()) {
	  switch (data1.attribute(x).type()) {
	    case Attribute.STRING:
	      c1 = data1.instance(y).stringValue(x);
	      c2 = data2.instance(y).stringValue(x);
	      break;
	    case Attribute.RELATIONAL:
	      c1 = data1.instance(y).relationalValue(x).toString();
	      c2 = data2.instance(y).relationalValue(x).toString();
	      break;
	    default:
	      c1 = data1.instance(y).value(x);
	      c2 = data2.instance(y).value(x);
	      break;
	  }
	  if (c1.compareTo(c2) != 0)
	    return "Values differ in instance " + (y + 1) + " at attribute " + (x + 1) + ":\n"
	      + c1 + "\n"
	      + "!=\n"
	      + c2;
	}
      }
    }

    return null;
  }

  /**
   * Tests whether applying imputation algorithm changes input data.
   */
  public void testChangesInputData() {
    Instances 	result;
    String 	msg;
    Instances 	icopy;

    icopy = new Instances(m_Instances);

    // 1st imputation
    try {
      m_Imputation.buildImputation(m_Instances);
      result = m_Imputation.impute(m_Instances);
      msg = compareDatasets(m_Instances, icopy);
      assertNotNull("Imputed data is null", result);
      assertNull("1st imputation changed input data: " + msg, msg);
    }
    catch (Exception e) {
      fail("Failed to apply imputation for 1st time: " + e);
    }

    // 2nd imputation
    try {
      result = m_Imputation.impute(m_Instances);
      msg = compareDatasets(m_Instances, icopy);
      assertNotNull("Imputed data is null", result);
      assertNull("2nd imputation changed input data: " + msg, msg);
    }
    catch (Exception e) {
      fail("Failed to apply imputation for 2nd time: " + e);
    }
  }
}
