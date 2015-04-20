#define COALESCED_NUM 16
#define blockDimX 16
#define blockDimY 1
#define gridDimX (gridDim.x)
#define gridDimY (gridDim.y)
#define idx (blockIdx.x*blockDimX+threadIdx.x)
#define idy (blockIdx.y*blockDimY+threadIdx.y)
#define bidy (blockIdx.y)
#define bidx (blockIdx.x)
#define tidx (threadIdx.x)
#define tidy (threadIdx.y)
#define merger_y 1
#define coalesced_idy (bidy/(COALESCED_NUM/(merger_y*blockDimY))*COALESCED_NUM)
#define globalDimY 1
#define A(y,x) A[(y)*WIDTH_A+(x)]
#define C(y,x) C[(y)*WIDTH_C+(x)]
#define WIDTH_C 2048
#define WIDTH_A 2048
__global__ void dct(float * A, float * B, float * C, int width)
{
	int i;
	float sum;
	__shared__ float shared0[16];
	sum=0;
	{
		shared0[tidx]=A(idy, idx);
	}
	#pragma unroll 
	for (i=0; i<8; i=(i+1))
	{
		float a;
		float b;
		a=shared0[i];
		b=1;
		sum+=(a*b);
	}
	{
		C(idy, idx)=sum;
	}
}
