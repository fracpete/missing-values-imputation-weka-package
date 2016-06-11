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
 * RandomPercentage.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package weka.filters.unsupervised.attribute.missingvaluesinjection;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Randomizable;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Simple algorithm for setting random values to missing, using java.util.Random:<br>
 * - Initialization:<br>
 *   The seed value is used to initialize a random number that provides<br>
 *   seed value for a random number generator per attribute in selected<br>
 *   range using Random.nextInt().<br>
 * <br>
 * - Injection:<br>
 *   For each instance:<br>
 *     For each attribute in selected range:<br>
 *       If Random.nextDouble() is &lt; percentage set value to missing<br>
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p>
 * 
 * <pre> -seed &lt;int&gt;
 *  The seed value for initializing the random number generators..
 *  (default: 1)</pre>
 * 
 * <pre> -percentage &lt;0.0-1.0&gt;
 *  The percentage of random numbers..
 *  (default: 0.2)</pre>
 * 
 * <pre> -R &lt;col1,col2,...&gt;
 *  The selection of columns to use in the injection process, 'first' and 'last' are valid indices..
 *  (default: first-last)</pre>
 * 
 * <pre> -V
 *  Inverts the matching sense.</pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class RandomPercentage
  extends AbstractInjectionWithRange
  implements Randomizable {

  /** the flag for the seed. */
  public final static String SEED = "seed";

  /** the flag for the percentage. */
  public final static String PERCENTAGE = "percentage";

  /** the seed. */
  protected int m_Seed = getDefaultSeed();

  /** the percentage to use (0-1). */
  protected double m_Percentage = getDefaultPercentage();

  /** the random number generators. */
  protected Random[] m_Random;

  /**
   * Returns general information on the algorithm.
   *
   * @return the information on the algorithm
   */
  @Override
  public String globalInfo() {
    return
      "Simple algorithm for setting random values to missing, using java.util.Random:"
	+ "\n"
	+ "- Initialization:\n"
	+ "  The seed value is used to initialize a random number that provides\n"
	+ "  seed value for a random number generator per attribute in selected\n"
        + "  range using Random.nextInt().\n"
	+ "\n"
	+ "- Injection:\n"
	+ "  For each instance:\n"
	+ "    For each attribute in selected range:\n"
	+ "      If Random.nextDouble() is < supplied percentage set value to missing\n";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  @Override
  public Enumeration<Option> listOptions() {
    Vector<Option> result = new Vector<Option>();

    result.addElement(new Option(
      "\t" + seedTipText() + ".\n"
        + "\t(default: " + getDefaultSeed() + ")",
        SEED, 1, "-" + SEED + " <int>"));

    result.addElement(new Option(
      "\t" + percentageTipText() + ".\n"
        + "\t(default: " + getDefaultPercentage() + ")",
        PERCENTAGE, 1, "-" + PERCENTAGE + " <0.0-1.0>"));

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

    result.add("-" + SEED);
    result.add("" + m_Seed);

    result.add("-" + PERCENTAGE);
    result.add("" + m_Percentage);

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

    tmpStr = Utils.getOption(SEED, options);
    if (!tmpStr.isEmpty())
      setSeed(Integer.parseInt(tmpStr));
    else
      setSeed(getDefaultSeed());

    tmpStr = Utils.getOption(PERCENTAGE, options);
    if (!tmpStr.isEmpty())
      setPercentage(Double.parseDouble(tmpStr));
    else
      setPercentage(getDefaultPercentage());

    super.setOptions(options);

    Utils.checkForRemainingOptions(options);
  }

  /**
   * Returns the default seed.
   *
   * @return the default
   */
  protected int getDefaultSeed() {
    return 1;
  }

  /**
   * Set the seed for random number generation.
   *
   * @param value the seed
   */
  public void setSeed(int value) {
    m_Seed = value;
  }

  /**
   * Gets the seed for the random number generations
   *
   * @return the seed for the random number generation
   */
  public int getSeed() {
    return m_Seed;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String seedTipText() {
    return "The seed value for initializing the random number generators.";
  }

  /**
   * Returns the default seed.
   *
   * @return the default
   */
  protected double getDefaultPercentage() {
    return 0.2;
  }

  /**
   * Set the percentage of random values to introduce.
   *
   * @param value the percentage (0-1)
   */
  public void setPercentage(double value) {
    if ((value >= 0) && (value <= 1.0))
      m_Percentage = value;
    else
      System.err.println("Percentage must be 0 <= x <= 1, provided: " + value);
  }

  /**
   * Gets the percentage of random values to introduce.
   *
   * @return the percentage (0-1)
   */
  public double getPercentage() {
    return m_Percentage;
  }

  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String percentageTipText() {
    return "The percentage of random numbers.";
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
    Random	rand;
    int		i;

    rand     = new Random(m_Seed);
    m_Random = new Random[m_Indices.length];
    for (i = 0; i < m_Random.length; i++)
      m_Random[i] = new Random(rand.nextInt());

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
    Instance 	result;
    int		i;
    double	val;

    result = (Instance) inst.copy();
    for (i = 0; i < m_Indices.length; i++) {
      val = m_Random[i].nextDouble();
      if (val < m_Percentage)
	result.setMissing(m_Indices[i]);
    }

    return result;
  }
}
