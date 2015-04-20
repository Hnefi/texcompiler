#define COALESCED_NUM 16
#define blockDimX 256
#define blockDimY 1
#define gridDimX (gridDim.x)
#define gridDimY (gridDim.y)
#define idx (blockIdx.x*blockDimX+threadIdx.x)
#define idy (blockIdx.y*blockDimY+threadIdx.y)
#define bidy (blockIdx.y)
#define bidx (blockIdx.x)
#define tidx (threadIdx.x)
#define tidy (threadIdx.y)
#define merger_y 8
#define coalesced_idy (bidy/(COALESCED_NUM/(merger_y*blockDimY))*COALESCED_NUM)
#define WIDTH_C 2048
#define C(y,x) C[(y)*WIDTH_C+(x)]
#define WIDTH_A (2048+16)
#define A(y,x) A[(y)*WIDTH_A+(x)]
__global__ void demosaic(float * A, float * C, int width)
{
	__shared__ float shared_0[272];
	float temp_0[9];
	float temp_1[9];
	float temp_2[9];
	float temp_3[9];
	float temp_4[9];
	float temp_5[9];
	float temp_6[9];
	float temp_7[9];
	int t_0;
	int t_1;
	int t_2;
	int t_3;
	int t_4;
	int t_5;
	int t_6;
	int t_7;
	int it_1;
	t_0=0;
	t_1=0;
	t_2=0;
	t_3=0;
	t_4=0;
	t_5=0;
	t_6=0;
	t_7=0;
	if ((tidx<16))
	{
		{
			shared_0[(tidx+0)]=A((((idy*8)+(( - 1)*(3-1)))+16), (idx+(( - 1)*0)));
		}
	}
	{
		shared_0[(tidx+16)]=A((((idy*8)+(( - 1)*(3-1)))+16), ((idx+(( - 1)*0))+16));
	}
	__syncthreads();
	#pragma unroll 
	for (it_1=0; it_1<3; it_1=(it_1+1))
	{
		float a;
		a=shared_0[((tidx+(( - 1)*it_1))+16)];
		temp_0[t_0]=a;
		t_0=(t_0+1);
	}
	__syncthreads();
	if ((tidx<16))
	{
		{
			shared_0[(tidx+0)]=A((((idy*8)+(( - 1)*(3-2)))+16), (idx+(( - 1)*0)));
		}
	}
	{
		shared_0[(tidx+16)]=A((((idy*8)+(( - 1)*(3-2)))+16), ((idx+(( - 1)*0))+16));
	}
	__syncthreads();
	#pragma unroll 
	for (it_1=0; it_1<3; it_1=(it_1+1))
	{
		float a;
		a=shared_0[((tidx+(( - 1)*it_1))+16)];
		temp_0[t_0]=a;
		temp_1[t_1]=a;
		t_0=(t_0+1);
		t_1=(t_1+1);
	}
	__syncthreads();
	if ((tidx<16))
	{
		{
			shared_0[(tidx+0)]=A((((idy*8)+(( - 1)*(3-3)))+16), (idx+(( - 1)*0)));
		}
	}
	{
		shared_0[(tidx+16)]=A((((idy*8)+(( - 1)*(3-3)))+16), ((idx+(( - 1)*0))+16));
	}
	__syncthreads();
	#pragma unroll 
	for (it_1=0; it_1<3; it_1=(it_1+1))
	{
		float a;
		a=shared_0[((tidx+(( - 1)*it_1))+16)];
		temp_0[t_0]=a;
		temp_1[t_1]=a;
		temp_2[t_2]=a;
		t_0=(t_0+1);
		t_1=(t_1+1);
		t_2=(t_2+1);
	}
	C(((idy*8)+0), idx)=cal(temp_0);
	__syncthreads();
	if ((tidx<16))
	{
		{
			shared_0[(tidx+0)]=A((((idy*8)+(( - 1)*(0-1)))+16), (idx+(( - 1)*0)));
		}
	}
	{
		shared_0[(tidx+16)]=A((((idy*8)+(( - 1)*(0-1)))+16), ((idx+(( - 1)*0))+16));
	}
	__syncthreads();
	#pragma unroll 
	for (it_1=0; it_1<3; it_1=(it_1+1))
	{
		float a;
		a=shared_0[((tidx+(( - 1)*it_1))+16)];
		temp_1[t_1]=a;
		temp_2[t_2]=a;
		temp_3[t_3]=a;
		t_1=(t_1+1);
		t_2=(t_2+1);
		t_3=(t_3+1);
	}
	C(((idy*8)+1), idx)=cal(temp_1);
	__syncthreads();
	if ((tidx<16))
	{
		{
			shared_0[(tidx+0)]=A((((idy*8)+(( - 1)*(0-2)))+16), (idx+(( - 1)*0)));
		}
	}
	{
		shared_0[(tidx+16)]=A((((idy*8)+(( - 1)*(0-2)))+16), ((idx+(( - 1)*0))+16));
	}
	__syncthreads();
	#pragma unroll 
	for (it_1=0; it_1<3; it_1=(it_1+1))
	{
		float a;
		a=shared_0[((tidx+(( - 1)*it_1))+16)];
		temp_2[t_2]=a;
		temp_3[t_3]=a;
		temp_4[t_4]=a;
		t_2=(t_2+1);
		t_3=(t_3+1);
		t_4=(t_4+1);
	}
	C(((idy*8)+2), idx)=cal(temp_2);
	__syncthreads();
	if ((tidx<16))
	{
		{
			shared_0[(tidx+0)]=A((((idy*8)+(( - 1)*(0-3)))+16), (idx+(( - 1)*0)));
		}
	}
	{
		shared_0[(tidx+16)]=A((((idy*8)+(( - 1)*(0-3)))+16), ((idx+(( - 1)*0))+16));
	}
	__syncthreads();
	#pragma unroll 
	for (it_1=0; it_1<3; it_1=(it_1+1))
	{
		float a;
		a=shared_0[((tidx+(( - 1)*it_1))+16)];
		temp_3[t_3]=a;
		temp_4[t_4]=a;
		temp_5[t_5]=a;
		t_3=(t_3+1);
		t_4=(t_4+1);
		t_5=(t_5+1);
	}
	C(((idy*8)+3), idx)=cal(temp_3);
	__syncthreads();
	if ((tidx<16))
	{
		{
			shared_0[(tidx+0)]=A((((idy*8)+(( - 1)*(0-4)))+16), (idx+(( - 1)*0)));
		}
	}
	{
		shared_0[(tidx+16)]=A((((idy*8)+(( - 1)*(0-4)))+16), ((idx+(( - 1)*0))+16));
	}
	__syncthreads();
	#pragma unroll 
	for (it_1=0; it_1<3; it_1=(it_1+1))
	{
		float a;
		a=shared_0[((tidx+(( - 1)*it_1))+16)];
		temp_4[t_4]=a;
		temp_5[t_5]=a;
		temp_6[t_6]=a;
		t_4=(t_4+1);
		t_5=(t_5+1);
		t_6=(t_6+1);
	}
	C(((idy*8)+4), idx)=cal(temp_4);
	__syncthreads();
	if ((tidx<16))
	{
		{
			shared_0[(tidx+0)]=A((((idy*8)+(( - 1)*(0-5)))+16), (idx+(( - 1)*0)));
		}
	}
	{
		shared_0[(tidx+16)]=A((((idy*8)+(( - 1)*(0-5)))+16), ((idx+(( - 1)*0))+16));
	}
	__syncthreads();
	#pragma unroll 
	for (it_1=0; it_1<3; it_1=(it_1+1))
	{
		float a;
		a=shared_0[((tidx+(( - 1)*it_1))+16)];
		temp_5[t_5]=a;
		temp_6[t_6]=a;
		temp_7[t_7]=a;
		t_5=(t_5+1);
		t_6=(t_6+1);
		t_7=(t_7+1);
	}
	C(((idy*8)+5), idx)=cal(temp_5);
	__syncthreads();
	if ((tidx<16))
	{
		{
			shared_0[(tidx+0)]=A((((idy*8)+(( - 1)*(0-6)))+16), (idx+(( - 1)*0)));
		}
	}
	{
		shared_0[(tidx+16)]=A((((idy*8)+(( - 1)*(0-6)))+16), ((idx+(( - 1)*0))+16));
	}
	__syncthreads();
	#pragma unroll 
	for (it_1=0; it_1<3; it_1=(it_1+1))
	{
		float a;
		a=shared_0[((tidx+(( - 1)*it_1))+16)];
		temp_6[t_6]=a;
		temp_7[t_7]=a;
		t_6=(t_6+1);
		t_7=(t_7+1);
	}
	C(((idy*8)+6), idx)=cal(temp_6);
	__syncthreads();
	if ((tidx<16))
	{
		{
			shared_0[(tidx+0)]=A((((idy*8)+(( - 1)*(0-7)))+16), (idx+(( - 1)*0)));
		}
	}
	{
		shared_0[(tidx+16)]=A((((idy*8)+(( - 1)*(0-7)))+16), ((idx+(( - 1)*0))+16));
	}
	__syncthreads();
	#pragma unroll 
	for (it_1=0; it_1<3; it_1=(it_1+1))
	{
		float a;
		a=shared_0[((tidx+(( - 1)*it_1))+16)];
		temp_7[t_7]=a;
		t_7=(t_7+1);
	}
	C(((idy*8)+7), idx)=cal(temp_7);
	__syncthreads();
	{
		
	}
	{
		
	}
	{
		
	}
	{
		
	}
	{
		
	}
	{
		
	}
	{
		
	}
	{
		
	}
}
