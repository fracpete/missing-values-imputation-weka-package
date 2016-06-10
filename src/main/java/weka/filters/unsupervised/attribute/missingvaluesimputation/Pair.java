/*
 *   Licensed under cc by-sa 3.0
 *   https://creativecommons.org/licenses/by-sa/3.0/
 */

/**
 * Pair.java
 * Copyright (C) 2014 Alexey Malev and zzg (StackOverflow)
 */

package weka.filters.unsupervised.attribute.missingvaluesimputation;

/**
 * Class to make it possible to sort by index (by grabbing indices inside the Pair objects).
 * <br>
 * <a href="http://stackoverflow.com/questions/23587314/how-to-sort-an-array-and-keep-track-of-the-index-in-java"
 * target="_blank">http://stackoverflow.com/questions/23587314/how-to-sort-an-array-and-keep-track-of-the-index-in-java</a>
 *
 * @author cjb60
 */
public class Pair
  implements Comparable<Pair> {

  public final int value;

  public final int index;

  public Pair(int value, int index) {
    this.value = value;
    this.index = index;
  }

  @Override
  public int compareTo(Pair pair) {
    return -1 * Integer.valueOf(this.value).compareTo(pair.value);
  }
}
