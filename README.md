drawing-app-algorithm
=====================

## About

*The development of algorithm is still in early phase. The text below describes the intended functionality.*

An point cloud based algorithm for comparing similarity of two pictures. Written in Scala and prepared for interoperability with Java. Features include:

* Manipulation of the first picture by translating, rotating and scaling it to match the second picture as well as possible. The target is to make those two pictures easily comparable.
* Comparision algorithm that takes two images and returns a number in scale 0-100 telling how similar those pictures are.

Created for the needs of master's thesis of Ilmari Arnkil.

## Example of use

Algorithm can be easily used from an Android app:

```java
  int[] drawedPixels, examplePixels;
  canvasView.getDrawingCache().getPixels(drawedPixels);
  exampleView.getDrawingCache().getPixels(examplePixels);

  PointCloud drawedCloud = PointCloud.fromImagePixelArray(pixels, canvasView.width(), Color.BLACK);

  PointCloud normalizedDrawedCloud = drawedCloud
    .centerToOrigoByGravity()
    .scaleByMean()
    .downsample();

  PointCloud normalizedExampleCloud = exampleCloud
    .centerToOrigoByGravity()
    .scaleByMean()
    .downsample();
  
  PointCloud matchingDrawedCloud = normalizedDrawedCloud
    .runCMAES(normalizedExampleCloud);

  int[] newPixels = matchingDrawedCloud.toImagePixelArray(Color.WHITE, Color.BLACK);
  int matchResult = matchingDrawedCloud.compareTo(normalizedExampleCloud);
  
  // show match result etc.
```

## Setup

Intellij IDEA is recommended IDE for algorithm development.

### Clone repository

Intellij IDEA toolbar: `VCS -> Checkout from Version Control -> Git` Repo address is `https://github.com/atk-partio/ilmomasiina.git`.

### Install sbt-android

Install sbt-android to local Ivy repository: 

```
  git clone https://github.com/jberkel/android-plugin
  cd android-plugin
  sbt publish-local
```

### Update Intellij IDEA project settings & dependencies

Run `sbt gen-idea` in project folder.

### Build

Run `sbt combile` to build the JAR file. Copy it from `target/xxx/xxx/PointCloud.jar` to Android project classpath.

## Licence

We should decide licence for source code ASAP. :)

