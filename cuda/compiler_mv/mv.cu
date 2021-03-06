
// includes, system
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

// includes, project
#include <cutil.h>

// includes, kernels
#include <mv_kernel.cu>
#include <cublas.h>
////////////////////////////////////////////////////////////////////////////////
// declaration, forward
void runTest();
void randomInit(float*, int);
void printDiff(float*, float*, int, int);

extern "C"
void computeGold(float*, const float*, const float*, unsigned int, unsigned int, unsigned int);


////////////////////////////////////////////////////////////////////////////////
// Program main
////////////////////////////////////////////////////////////////////////////////
int
main(int argc, char** argv)
{
	CUT_DEVICE_INIT(argc, argv);

	runTest();

    CUT_EXIT(argc, argv);
}

////////////////////////////////////////////////////////////////////////////////
//! Run a simple test for CUDA
////////////////////////////////////////////////////////////////////////////////
void
runTest()
{

	char* p[32];
	float result[32];
    cublasStatus status;
    status = cublasInit();
    if (status != CUBLAS_STATUS_SUCCESS) {
        fprintf (stderr, "!!!! CUBLAS initialization error\n");
	exit (1);
    }
    // set seed for rand()
    srand(2006);

    // allocate host memory for matrices A and B
    unsigned int size_A = WA * HA;
    unsigned int mem_size_A = sizeof(float) * size_A;
    float* h_A = (float*) malloc(mem_size_A);
    unsigned int size_B = WB * HB;
    unsigned int mem_size_B = sizeof(float) * size_B;
    float* h_B = (float*) malloc(mem_size_B);

    // initialize host memory
    randomInit(h_A, size_A);
    randomInit(h_B, size_B);

    // allocate device memory
    float* d_A;
    CUDA_SAFE_CALL(cudaMalloc((void**) &d_A, mem_size_A));
    float* d_B;
    CUDA_SAFE_CALL(cudaMalloc((void**) &d_B, mem_size_B));

    // copy host memory to device
    CUDA_SAFE_CALL(cudaMemcpy(d_A, h_A, mem_size_A,
                              cudaMemcpyHostToDevice) );
    CUDA_SAFE_CALL(cudaMemcpy(d_B, h_B, mem_size_B,
                              cudaMemcpyHostToDevice) );

    // allocate device memory for result
    unsigned int size_C = WC * HC;
    unsigned int mem_size_C = sizeof(float) * size_C;

    // allocate host memory for the result
    float* h_C = (float*) malloc(mem_size_C);

    // create and start timer
    unsigned int timer = 0;

    // compute reference solution
    float* reference = (float*) malloc(mem_size_C);
    computeGold(reference, h_A, h_B, HA, WA, WB);
    CUTBoolean res;
    int iterator = 16;

    int pid = 0;
    {
        free(h_C);
        h_C = (float*) malloc(mem_size_C);
        float* d_C;
        CUDA_SAFE_CALL(cudaMalloc((void**) &d_C, mem_size_C));
		// setup execution parameters
		dim3 threads(256, 1);
		dim3 grid(WC / threads.x, HC / threads.y);

		CUT_SAFE_CALL(cutCreateTimer(&timer));
		cudaThreadSynchronize();
		CUT_SAFE_CALL(cutStartTimer(timer));
		for (int i=0; i<iterator; i++) {
			// execute the kernel
			mv_naive<<< grid, threads >>>(d_A, d_B, d_C, WA);
			// stop and destroy timer
		}
		cudaThreadSynchronize();
		CUT_SAFE_CALL(cutStopTimer(timer));

		// check if kernel execution generated and error
		CUT_CHECK_ERROR("Kernel execution failed");

		// copy result from device to host
		CUDA_SAFE_CALL(cudaMemcpy(h_C, d_C, mem_size_C,
								  cudaMemcpyDeviceToHost) );
		p[pid] = "naive";
		result[pid] = 2000.0*HA*WA/cutGetTimerValue(timer)*iterator/1024/1024/1024;
		pid++;
		printf("mv_naive Processing time: %f (ms), %f Gflops \n", cutGetTimerValue(timer), 2000.0*HA*WA*iterator/cutGetTimerValue(timer)/1024/1024/1024);
		CUT_SAFE_CALL(cutDeleteTimer(timer));
	    CUDA_SAFE_CALL(cudaFree(d_C));
    }
    // check result
    res = cutCompareL2fe(reference, h_C, size_C, 1e-5f);
    printf("Test %s \n", (1 == res) ? "PASSED" : "FAILED");

    {
        free(h_C);
        h_C = (float*) malloc(mem_size_C);
        float* d_C;
        CUDA_SAFE_CALL(cudaMalloc((void**) &d_C, mem_size_C));

		CUT_SAFE_CALL(cutCreateTimer(&timer));
		cudaThreadSynchronize();
		CUT_SAFE_CALL(cutStartTimer(timer));
        for (int i=0; i<iterator; i++) {
			// execute the kernel
			cublasSgemv('t', WA, HA, 1.0f, d_A,
					WA, d_B, 1, 0.0f, d_C, 1);
			/*
			cublasSgemm('n', 'n', , 1, , 1.0f, d_A,
					, d_B, , 0.0f, d_C, );*/
			// stop and destroy timer
        }
		cudaThreadSynchronize();
		CUT_SAFE_CALL(cutStopTimer(timer));

		// check if kernel execution generated and error
		CUT_CHECK_ERROR("Kernel execution failed");

		// copy result from device to host
		CUDA_SAFE_CALL(cudaMemcpy(h_C, d_C, mem_size_C,
								  cudaMemcpyDeviceToHost) );
		result[pid] = 2000.0*HA*WA/cutGetTimerValue(timer)*iterator/1024/1024/1024;
		p[pid] = "cublasSgemm";
		pid++;

		printf("cublasSgemm Processing time: %f (ms), %f Gflops \n", cutGetTimerValue(timer), 2000.0*WA*HA*iterator/cutGetTimerValue(timer)/1024/1024/1024);
		CUT_SAFE_CALL(cutDeleteTimer(timer));
	    CUDA_SAFE_CALL(cudaFree(d_C));

    }
    res = cutCompareL2fe(reference, h_C, size_C, 1e-5f);
    printf("Test %s \n", (1 == res) ? "PASSED" : "FAILED");


    {
        free(h_C);
        h_C = (float*) malloc(mem_size_C);
        float* d_C;
        CUDA_SAFE_CALL(cudaMalloc((void**) &d_C, mem_size_C));
        CUDA_SAFE_CALL(cudaMemcpy(d_C, h_C, mem_size_C,
                                  cudaMemcpyHostToDevice) );

        // setup execution parameters
		dim3 threads(32, 1);
		dim3 grid(WC / threads.x, HC / threads.y);

		CUT_SAFE_CALL(cutCreateTimer(&timer));
		cudaThreadSynchronize();
		CUT_SAFE_CALL(cutStartTimer(timer));
		for (int i=0; i<iterator; i++) {
			// execute the kernel
			mv_coalesced<<< grid, threads >>>(d_A, d_B, d_C, WA);
			// stop and destroy timer
		}
		cudaThreadSynchronize();
		CUT_SAFE_CALL(cutStopTimer(timer));

		// check if kernel execution generated and error
		CUT_CHECK_ERROR("Kernel execution failed");

		// copy result from device to host
		CUDA_SAFE_CALL(cudaMemcpy(h_C, d_C, mem_size_C,
								  cudaMemcpyDeviceToHost) );

		result[pid] = 2000.0*HA*WA/cutGetTimerValue(timer)*iterator/1024/1024/1024;
		p[pid] = "mv_coalesced";
		pid++;
		printf("mv_coalesced Processing time: %f (ms), %f Gflops \n", cutGetTimerValue(timer), 2000.0*WA*HA*iterator/cutGetTimerValue(timer)/1024/1024/1024);
		CUT_SAFE_CALL(cutDeleteTimer(timer));
	    CUDA_SAFE_CALL(cudaFree(d_C));

    }

    res = cutCompareL2fe(reference, h_C, size_C, 1e-5f);
    printf("Test %s \n", (1 == res) ? "PASSED" : "FAILED");

    {
        free(h_C);
        h_C = (float*) malloc(mem_size_C);
        float* d_C;
        CUDA_SAFE_CALL(cudaMalloc((void**) &d_C, mem_size_C));
        CUDA_SAFE_CALL(cudaMemcpy(d_C, h_C, mem_size_C,
                                  cudaMemcpyHostToDevice) );

        // setup execution parameters
		dim3 threads(32, 1);
		dim3 grid(WC / threads.x, HC / threads.y);

		CUT_SAFE_CALL(cutCreateTimer(&timer));
		cudaThreadSynchronize();
		CUT_SAFE_CALL(cutStartTimer(timer));
		for (int i=0; i<iterator; i++) {
			// execute the kernel
			mv_opt<<< grid, threads >>>(d_A, d_B, d_C, WA);
			// stop and destroy timer
		}
		cudaThreadSynchronize();
		CUT_SAFE_CALL(cutStopTimer(timer));

		// check if kernel execution generated and error
		CUT_CHECK_ERROR("Kernel execution failed");

		// copy result from device to host
		CUDA_SAFE_CALL(cudaMemcpy(h_C, d_C, mem_size_C,
								  cudaMemcpyDeviceToHost) );

		result[pid] = 2000.0*HA*WA/cutGetTimerValue(timer)*iterator/1024/1024/1024;
		p[pid] = "mv_opt";
		pid++;
		printf("mv_opt Processing time: %f (ms), %f Gflops \n", cutGetTimerValue(timer), 2000.0*WA*HA*iterator/cutGetTimerValue(timer)/1024/1024/1024);
		CUT_SAFE_CALL(cutDeleteTimer(timer));
	    CUDA_SAFE_CALL(cudaFree(d_C));

    }

    res = cutCompareL2fe(reference, h_C, size_C, 1e-5f);
    printf("Test %s \n", (1 == res) ? "PASSED" : "FAILED");



    // clean up memory
    free(h_A);
    free(h_B);
    free(h_C);
    free(reference);
    CUDA_SAFE_CALL(cudaFree(d_A));
    CUDA_SAFE_CALL(cudaFree(d_B));
    status = cublasShutdown();
    if (status != CUBLAS_STATUS_SUCCESS) {
        fprintf (stderr, "!!!! shutdown error\n");
    }
//    CUDA_SAFE_CALL(cudaFree(d_C));
}

// Allocates a matrix with random float entries.
void randomInit(float* data, int size)
{
    for (int i = 0; i < size; ++i)
        data[i] = rand() / (float)RAND_MAX;
}

void printDiff(float *data1, float *data2, int width, int height)
{
  int i,j,k;
  int error_count=0;
  for (j=0; j<height; j++) {
    for (i=0; i<width; i++) {
      k = j*width+i;
      if (data1[k] != data2[k]) {
         printf("diff(%d,%d) CPU=%4.4f, GPU=%4.4f n", i,j, data1[k], data2[k]);
         error_count++;
      }
    }
  }
  printf(" nTotal Errors = %d n", error_count);
}

