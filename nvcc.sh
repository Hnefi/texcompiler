CUDA_HOME=/usr/bin
$CUDA_HOME/nvcc --ptxas-options=-v -c $1
