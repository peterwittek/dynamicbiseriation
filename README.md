Dynamic Two-Way Seriation
==
Two-way Incremental Seriation based on Hamiltonian Path is a package that takes a sparse term-document matrix provided in libsvm format, and calculates the seriation of the term space. Several distance functions are available. 

Usage
==
Given an input matrix in sparse libsvm format, the class DynamicBiseriation will calculate the biseriation of the matrix. Given a sequence of matrices, it will calculate the updates to the biseration. Different distance functions and biseriation heuristics are available.

Usage:

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

Compilation
==
Issuing ant jar should compile the classes and create the jar file in build/jar.

Citation
==
If you use this code, please cite: 

Wittek, P. (2013). Two-Way Incremental Seriation in the Temporal Domain with Three-dimensional Visualization: Making Sense of Evolving High-Dimensional Data Sets. Computational Statistics and Data Analysis.

A preprint of the manuscript is available for download here:

http://hdl.handle.net/2320/12278
