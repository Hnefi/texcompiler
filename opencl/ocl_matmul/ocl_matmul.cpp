
#include <oclUtils.h>

const char* cSourceFile = "matmul.cl";

void *srcA, *srcB, *dst;        // Host buffers for OpenCL test
void* Golden;                   // Host buffer for host golden processing cross check

int width0;
int height0;
int width1;
int height1;

// OpenCL Vars
cl_context cxGPUContext;        // OpenCL context
cl_command_queue cqCommandQue;  // OpenCL command que
cl_device_id* cdDevices;        // OpenCL device list
cl_program cpProgram;           // OpenCL program
cl_mem cmDevSrcA;               // OpenCL device source buffer A
cl_mem cmDevSrcB;               // OpenCL device source buffer A
cl_mem cmDevDst;                // OpenCL device destination buffer
size_t szGlobalWorkSize[2];        // 1D var for Total # of work items
size_t szLocalWorkSize[2];		    // 1D var for # of work items in the work group
size_t szParmDataBytes;			// Byte size of context information
size_t szKernelLength;			// Byte size of kernel code
cl_int ciErr1, ciErr2;			// Error code var
char* cPathAndName = NULL;      // var for full paths to data, src, etc.
char* cSourceCL = NULL;         // Buffer to hold source for compilation


// Forward Declarations
// *********************************************************************
void cpuGold(float* C, const float* A, const float* B, unsigned int hA, unsigned int wA, unsigned int wB);

void Cleanup(int iExitCode);


void execute(char* kernelName)
{
    // Create the kernel
	cl_kernel kernel = clCreateKernel(cpProgram, kernelName, &ciErr1);
    printf("clCreateKernel (%s)...\n", kernelName);
    if (ciErr1 != CL_SUCCESS)
    {
        printf("Error in clCreateKernel, Line %u in file %s !!!\n\n", __LINE__, __FILE__);
        Cleanup(EXIT_FAILURE);
    }

    // Set the Argument values
    ciErr1 = clSetKernelArg(kernel, 0, sizeof(cl_mem), (void*)&cmDevSrcA);
    ciErr1 |= clSetKernelArg(kernel, 1, sizeof(cl_mem), (void*)&cmDevSrcB);
    ciErr1 |= clSetKernelArg(kernel, 2, sizeof(cl_mem), (void*)&cmDevDst);
    ciErr1 |= clSetKernelArg(kernel, 3, sizeof(int), (void*)&width0);
    ciErr1 |= clSetKernelArg(kernel, 4, sizeof(int), (void*)&height0);
    if (ciErr1 != CL_SUCCESS)
    {
        printf("Error in Set Arguments, Line %u in file %s !!!\n\n", __LINE__, __FILE__);
        Cleanup(EXIT_FAILURE);
    }


	for (int i=0; i<3; i++)
	{
		cl_event event;
		ciErr1 = clEnqueueNDRangeKernel(cqCommandQue, kernel, 2, NULL, szGlobalWorkSize, szLocalWorkSize, 0, NULL, &event);
		ciErr1 = clWaitForEvents(1, &event);
        cl_ulong start, end;
        ciErr1 = clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_END, sizeof(cl_ulong), &end, NULL);
        ciErr1 |= clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_START, sizeof(cl_ulong), &start, NULL);
        float seconds = (end - start) * 1.0e-9f;

		double gflops = 2.0*width0*height0*width1/seconds/1e9;
		printf("seconds=%f,gflops=%f\n", seconds,gflops);

	    clReleaseEvent(event);
	}

    ciErr1 = clEnqueueReadBuffer(cqCommandQue, cmDevDst, CL_TRUE, 0, sizeof(cl_float) * width1*height0, dst, 0, NULL, NULL);
    if (ciErr1 != CL_SUCCESS)
    {
        printf("Error in buffer transfer, Line %u in file %s !!!\n\n", __LINE__, __FILE__);
        Cleanup(EXIT_FAILURE);
    }

    cpuGold ((float*)Golden, (float*)srcA, (float*)srcB, height0, width0, width1);

    shrBOOL res = shrCompareL2fe((const float*)dst, (const float*)Golden, width0*height0, 1e-5f);
    printf("TEST %s \n\n", (1 == res) ? "PASSED" : "FAILED !!!");

    clReleaseKernel(kernel);
}
// Main function
// *********************************************************************
int main(int argc, char **argv)
{
    cl_platform_id cpPlatform = NULL;
    cl_uint ciDeviceCount = 0;


	height1 = width1 = 1024;
	width0 = height0 = 1024;


    srcA = (void *)malloc((width0) * (height0) * sizeof(cl_float));
    srcB = (void *)malloc((width1) * (height1) * sizeof(cl_float));
    dst = (void *)malloc((width1) * (height0) * sizeof(cl_float));
    Golden = (void *)malloc((width1) * (height0) * sizeof(cl_float));

    shrFillArray((float*)srcA, (width0) * (height0));
    shrFillArray((float*)srcB, (width1) * (height1));



    //Get the NVIDIA platform
    ciErr1 = oclGetPlatformID(&cpPlatform);
    if (ciErr1 != CL_SUCCESS)
    {
    	printf("Error: Failed to create OpenCL context!\n");
        return ciErr1;
    }

    //Get the devices
    ciErr1 = clGetDeviceIDs(cpPlatform, CL_DEVICE_TYPE_GPU, 0, NULL, &ciDeviceCount);
    cdDevices = (cl_device_id *)malloc(ciDeviceCount * sizeof(cl_device_id) );
    ciErr1 = clGetDeviceIDs(cpPlatform, CL_DEVICE_TYPE_GPU, ciDeviceCount, cdDevices, NULL);
    printf("clGetContextInfo...\n");
    if (ciErr1 != CL_SUCCESS)
    {
        printf("Error in clGetContextInfo, Line %u in file %s !!!\n\n", __LINE__, __FILE__);
        Cleanup(EXIT_FAILURE);
    }

    //Create the context
    cxGPUContext = clCreateContext(0, ciDeviceCount, cdDevices, NULL, NULL, &ciErr1);
    if (ciErr1 != CL_SUCCESS)
    {
    	printf("Error: Failed to create OpenCL context!\n");
        return ciErr1;
    }



    // Create a command-queue
    cqCommandQue = clCreateCommandQueue(cxGPUContext, cdDevices[0], CL_QUEUE_PROFILING_ENABLE, &ciErr1);
    printf("clCreateCommandQueue...\n");
    if (ciErr1 != CL_SUCCESS)
    {
        printf("Error in clCreateCommandQueue, Line %u in file %s !!!\n\n", __LINE__, __FILE__);
        Cleanup(EXIT_FAILURE);
    }

    // Allocate the OpenCL buffer memory objects for source and result on the device GMEM
    cmDevSrcA = clCreateBuffer(cxGPUContext, CL_MEM_READ_WRITE, sizeof(cl_float) *  (width0) * (height0), NULL, &ciErr1);
    cmDevSrcB = clCreateBuffer(cxGPUContext, CL_MEM_READ_WRITE, sizeof(cl_float) *  (width1) * (height1), NULL, &ciErr1);
    cmDevDst = clCreateBuffer(cxGPUContext, CL_MEM_READ_WRITE, sizeof(cl_float) * width1 * height0, NULL, &ciErr2);
    ciErr1 |= ciErr2;
    printf("clCreateBuffer...\n");
    if (ciErr1 != CL_SUCCESS)
    {
        printf("Error in clCreateBuffer, Line %u in file %s !!!\n\n", __LINE__, __FILE__);
        Cleanup(EXIT_FAILURE);
    }

    // Read the OpenCL kernel in from source file
    printf("oclLoadProgSource (%s)...\n", cSourceFile);
    cPathAndName = shrFindFilePath(cSourceFile, argv[0]);
    cSourceCL = oclLoadProgSource(cPathAndName, "", &szKernelLength);

    // Create the program
    cpProgram = clCreateProgramWithSource(cxGPUContext, 1, (const char **)&cSourceCL, &szKernelLength, &ciErr1);
    if (ciErr1 != CL_SUCCESS)
    {
        printf("Error in clCreateProgramWithSource, Line %u in file %s !!!\n\n", __LINE__, __FILE__);
        Cleanup(EXIT_FAILURE);
    }

    // Build the program
    ciErr1 = clBuildProgram(cpProgram, 0, NULL, NULL, NULL, NULL);
    if (ciErr1 != CL_SUCCESS)
    {
        printf("Error in clBuildProgram, Line %u in file %s !!!\n\n", __LINE__, __FILE__);
        Cleanup(EXIT_FAILURE);
    }

    ciErr1 = clEnqueueWriteBuffer(cqCommandQue, cmDevSrcA, CL_FALSE, 0, sizeof(cl_float) * (width0) * (height0), srcA, 0, NULL, NULL);
    ciErr1 |= clEnqueueWriteBuffer(cqCommandQue, cmDevSrcB, CL_FALSE, 0, sizeof(cl_float) * (width1) * (height1), srcB, 0, NULL, NULL);
	clFinish(cqCommandQue);
    if (ciErr1 != CL_SUCCESS)
    {
        printf("Error in buffer transfer, Line %u in file %s !!!\n\n", __LINE__, __FILE__);
        Cleanup(EXIT_FAILURE);
    }


    // set and log Global and Local work size dimensions
    szLocalWorkSize[0] = 16;
	szLocalWorkSize[1] = 16;
    szGlobalWorkSize[0] = width1;
	szGlobalWorkSize[1] = height0;
	execute("matmul");

    szLocalWorkSize[0] = 256;
	szLocalWorkSize[1] = 1;
    szGlobalWorkSize[0] = width1;
	szGlobalWorkSize[1] = height0/16;
	execute("matmul_opt");

	// Cleanup and leave
    Cleanup (EXIT_SUCCESS);
}

void Cleanup (int iExitCode)
{
    // Cleanup allocated objects
    if(cdDevices)free(cdDevices);
    if(cPathAndName)free(cPathAndName);
    if(cSourceCL)free(cSourceCL);
    if(cpProgram)clReleaseProgram(cpProgram);
    if(cqCommandQue)clReleaseCommandQueue(cqCommandQue);
    if(cxGPUContext)clReleaseContext(cxGPUContext);
    if(cmDevSrcA)clReleaseMemObject(cmDevSrcA);
    if(cmDevSrcB)clReleaseMemObject(cmDevSrcB);
    if(cmDevDst)clReleaseMemObject(cmDevDst);

    // Free host memory
    free(srcA);
    free(srcB);
    free (dst);
    free(Golden);

    exit (iExitCode);
}


void
cpuGold(float* C, const float* A, const float* B, unsigned int hA, unsigned int wA, unsigned int wB)
{
    for (unsigned int i = 0; i < hA; ++i)
        for (unsigned int j = 0; j < wB; ++j) {
            double sum = 0;
            for (unsigned int k = 0; k < wA; ++k) {
                double a = A[i * wA + k];
                double b = B[k * wB + j];
                sum += a * b;
            }
            C[i * wB + j] = (float)sum;
        }
}

