Dynamic Two-Way Seriation
=========================

Two-way seriation is a popular technique to analyse groups of similar instances and their features, as well as the connections between the groups themselves. The two-way seriated data may be visualized as a two-dimensional heat map or as a three-dimensional landscape where colour codes or height correspond to the values in the matrix. Ordinary two-way seriation can also be extended to deal with updates of both the row and column spaces. Combined with visualization, this method gives a powerful way of studying time-evolving data set [1-2].

This packages provides an implementation of the dynamic two-way incremental seriation based on a Hamiltonian path. It takes a sparse term-document matrix provided in libsvm format, and calculates the seriation of the term space. Several distance functions are available.

Usage
-----
Given an input matrix in sparse libsvm format, the class DynamicBiseriation will calculate the biseriation of the matrix. Given a sequence of matrices, it will calculate the updates to the biseration. Different distance functions and biseriation heuristics are available.

**Command-Line Interface**

The full functionality of the package is exposed though the command line:

    $ java -jar DynamicBiseriation.jar [options] collection_file [update_files]

Arguments:

    -d Distance   Distance function (default: Euclidean):
                     Correlation
                     Cosine
                     Euclidean
                     Manhattan
                     MutualInformation
    -h heuristic  Heuristic type (default: leftright):
                     leftright
                     insert

Examples:

    $ java -jar DynamicBiseriation.jar test_data/collection.dat
    $ java -jar -d Correlation -h insert DynamicBiseriation.jar \
        test_data/collection.dat test_data/collection-update.dat

**Application Programming Interface**

For integrating with a larger project, an API is available through the DynamicBiseriation class. The generic procedure is to first calculate the biseration on the initial subset of a data collection:

		DynamicBiseriation dynamicBiseriation = new DynamicBiseriation(
				collection, distance, heuristic);
		dynamicBiseriation.calculateBiseriationOfStaticPart();

This can be followed by an arbitrary number of updates to the collection:

		while (args.length>argc){
			String collectionUpdate = args[argc++];
			dynamicBiseriation.addCollectionUpdate(collectionUpdate);
			dynamicBiseriation.iterativelyUpdate();
		}

For full documentation on the functions, refer to the JavaDoc.

Compilation
-----------
Issuing ant jar compiles the classes and create the jar file in build/jar.

Acknowledgment
--------------
This work was supported by the European Commission Seventh Framework Programme under Grant Agreement Numbers ICT-216736 SHAMAN and FP7-601138 PERICLES.

References
--------
[1] Darányi, S. & Wittek, P. (2013). [Demonstrating Conceptual Dynamics in an Evolving Text Collection](http://dx.doi.org/10.1002/asi.22940). Journal of the American Society for Information Science and Technology, 64(12), pp. 2564--2572. [PDF](http://bada.hb.se/bitstream/2320/12614/1/Demonstrating%20Conceptual%20Dynamics.pdf)

[2] Wittek, P. (2013). [Two-Way Incremental Seriation in the Temporal Domain with Three-dimensional Visualization: Making Sense of Evolving High-Dimensional Data Sets](http://dx.doi.org/10.1016/j.csda.2013.03.026). Computational Statistics and Data Analysis, 66, pp. 193--201. [PDF](http://bada.hb.se/bitstream/2320/12278/3/Wittek-Two-way_Seration.pdf)
