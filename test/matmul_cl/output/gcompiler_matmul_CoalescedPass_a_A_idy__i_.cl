#define bidx (get_group_id(0))
#define bidy (get_group_id(1))
#define tidx (get_local_id(0))
#define tidy (get_local_id(1))
#define gridDimX (get_num_groups(0))
#define gridDimY (get_num_groups(1))
#define COALESCED_NUM 16
#define blockDimX 16
#define blockDimY 1
#define idx (bidx*blockDimX+tidx)
#define idy (bidy*blockDimY+tidy)
#define merger_y 1
#define coalesced_idy (bidy/(COALESCED_NUM/(merger_y*blockDimY))*COALESCED_NUM)
#define B(y,x) B[(y)*WIDTH_B+(x)]
#define WIDTH_C 2048
#define WIDTH_B 2048
#define C(y,x) C[(y)*WIDTH_C+(x)]
#define WIDTH_A 2048
#define A(y,x) A[(y)*WIDTH_A+(x)]
__kernel void matmul(__global float * A, __global float * B, __global float * C, int width, int height)
{
	__local float shared_0[16];
	int i;
	float sum;
	sum=0;
	for (i=0; i<width; i=(i+16))
	{
		int it_1;
		shared_0[(tidx+0)]=A(idy, (i+tidx));
		barrier(CLK_LOCAL_MEM_FENCE);
		#pragma unroll 
		for (it_1=0; it_1<16; it_1=(it_1+1))
		{
			float a;
			float b;
			a=shared_0[it_1];
			b=B((it_1+i), idx);
			sum+=(a*b);
		}
		barrier(CLK_LOCAL_MEM_FENCE);
	}
	{
		C(idy, idx)=sum;
	}
}
