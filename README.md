Missing Values Imputation
=========================

Weka package for missing values imputation using various techniques.

Implemented techniques:

* `NullImputation` - dummy
* `MeansAndModes` - like WEKA's `ReplaceMissingValues` filter
* `MultiImputation` - applies the specified imputation algorithms sequentially
* `SimpleNearestNeighbor` - uses nearest neighbor approach to determine most 
   common label or average (date/numeric)


Releases
--------

Click on one of the following links to download the corresponding Weka package:

* [2016.6.9](https://github.com/fracpete/missing-values-imputation-weka-package/releases/download/v2016.6.9/missing-values-imputation-2016.6.9.zip)


How to use packages
-------------------

For more information on how to install the package, see:

http://weka.wikispaces.com/How+do+I+use+the+package+manager%3F


Maven
-----

Add the following dependency in your `pom.xml` to include the package:

```xml
    <dependency>
      <groupId>com.github.fracpete</groupId>
      <artifactId>missing-values-imputation-weka-package</artifactId>
      <version>2016.6.9</version>
      <type>jar</type>
      <exclusions>
        <exclusion>
          <groupId>nz.ac.waikato.cms.weka</groupId>
          <artifactId>weka-dev</artifactId>
        </exclusion>
      </exclusions>
    </dependency>
```

