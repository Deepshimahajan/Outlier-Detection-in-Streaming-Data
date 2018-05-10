# Outlier detection in streaming data using grid-based methodology
A Java program for outlier detection in streaming data using grid-based methodology.

# Input
A window size w (32- bit integer), a host:port pair where you receive an input stream of fixed-d- dimensional integer data points in a comma separated values (CSV) format with timestamp added in front. 

# Output 
Outliers starting from the (w+1)th input data until end of input stream. Your program should handle concept drift too. The grid-based method is a special case of density-based approach. 

# Description

The idea is to determine sparse regions in the underlying data in order to report outliers Histograms can be viewed as 1-dimensional special cases of grid-based method. In the context of multivariate (i.e., d-dimensional) data, a natural generalization is the use of grid-structure. Each dimension is partitioned into p equal-width ranges (e.g., pd+1 is roughly equal to n, the number of input data (or the window size)). Data points that have density less than t (e.g., ⎡log p⎤) in any particular grid region are reported as outliers. It is often hard to determine the optimal bin-width well. When the bins are too narrow, the normal data points falling in these bins will be declared outliers; when the bins are too wide, anomalous data points and high-density regions may be merged into single bin. So, we need to use small bin-width to find densities of neighboring cells and merge neighbor cells with density ≥ t together, and points in isolated cell with density < t are reported as outliers. Both p and t are parameters which need to be tuned for the application at hand. To handle streaming data with sliding window, we extend density to incremental scenarios: 1) the statistic of the newly inserted data point is computed, 2) only the density of the affected data points by the newly inserted data point in the existing data points in the window are updated, and 3) similarly updated the deleted data points.
