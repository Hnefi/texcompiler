#define bidx (blockIdx.x)
#define bidy (blockIdx.y)
#define tidx (threadIdx.x)
#define tidy (threadIdx.y)
#define gridDimX (gridDim.x)
#define gridDimY (gridDim.y)
#define COALESCED_NUM 16
#define blockDimX 128
#define blockDimY 1
#define idx (bidx*blockDimX+tidx)
#define idy (bidy*blockDimY+tidy)
#define merger_y 32
#define coalesced_idy (bidy/(COALESCED_NUM/(merger_y*blockDimY))*COALESCED_NUM)
#define A(y,x) A[(y)*WIDTH_A+(x)]
#define B(y,x) B[(y)*WIDTH_B+(x)]
#define C(y,x) C[(y)*WIDTH_C+(x)]
#define WIDTH_C 2048
#define WIDTH_B 2048
#define WIDTH_A 2048
__global__ void matmul(float * A, float * B, float * C, int width, int height)
{
	__shared__ float shared_0[16][33];
	int i;
	float sum_0;
	float sum_1;
	float sum_2;
	float sum_3;
	float sum_4;
	float sum_5;
	float sum_6;
	float sum_7;
	float sum_8;
	float sum_9;
	float sum_10;
	float sum_11;
	float sum_12;
	float sum_13;
	float sum_14;
	float sum_15;
	float sum_16;
	float sum_17;
	float sum_18;
	float sum_19;
	float sum_20;
	float sum_21;
	float sum_22;
	float sum_23;
	float sum_24;
	float sum_25;
	float sum_26;
	float sum_27;
	float sum_28;
	float sum_29;
	float sum_30;
	float sum_31;
	sum_0=0;
	sum_1=0;
	sum_2=0;
	sum_3=0;
	sum_4=0;
	sum_5=0;
	sum_6=0;
	sum_7=0;
	sum_8=0;
	sum_9=0;
	sum_10=0;
	sum_11=0;
	sum_12=0;
	sum_13=0;
	sum_14=0;
	sum_15=0;
	sum_16=0;
	sum_17=0;
	sum_18=0;
	sum_19=0;
	sum_20=0;
	sum_21=0;
	sum_22=0;
	sum_23=0;
	sum_24=0;
	sum_25=0;
	sum_26=0;
	sum_27=0;
	sum_28=0;
	sum_29=0;
	sum_30=0;
	sum_31=0;
	for (i=0; i<width; i=(i+16))
	{
		int it_1;
		shared_0[((tidx%16)+0)][(tidx/16)]=A(((((bidy*32)+tidy)+(tidx/16))+0), (i+(tidx%16)));
		shared_0[((tidx%16)+0)][(tidx/16)]=A(((((bidy*32)+tidy)+(tidx/16))+8), (i+(tidx%16)));
		shared_0[((tidx%16)+0)][(tidx/16)]=A(((((bidy*32)+tidy)+(tidx/16))+16), (i+(tidx%16)));
		shared_0[((tidx%16)+0)][(tidx/16)]=A(((((bidy*32)+tidy)+(tidx/16))+24), (i+(tidx%16)));
		__syncthreads();
		#pragma unroll 
		for (it_1=0; it_1<16; it_1=(it_1+1))
		{
			float a_0;
			float a_1;
			float a_2;
			float a_3;
			float a_4;
			float a_5;
			float a_6;
			float a_7;
			float a_8;
			float a_9;
			float a_10;
			float a_11;
			float a_12;
			float a_13;
			float a_14;
			float a_15;
			float a_16;
			float a_17;
			float a_18;
			float a_19;
			float a_20;
			float a_21;
			float a_22;
			float a_23;
			float a_24;
			float a_25;
			float a_26;
			float a_27;
			float a_28;
			float a_29;
			float a_30;
			float a_31;
			float b;
			a_0=shared_0[it_1][0];
			a_1=shared_0[it_1][1];
			a_2=shared_0[it_1][2];
			a_3=shared_0[it_1][3];
			a_4=shared_0[it_1][4];
			a_5=shared_0[it_1][5];
			a_6=shared_0[it_1][6];
			a_7=shared_0[it_1][7];
			a_8=shared_0[it_1][8];
			a_9=shared_0[it_1][9];
			a_10=shared_0[it_1][10];
			a_11=shared_0[it_1][11];
			a_12=shared_0[it_1][12];
			a_13=shared_0[it_1][13];
			a_14=shared_0[it_1][14];
			a_15=shared_0[it_1][15];
			a_16=shared_0[it_1][16];
			a_17=shared_0[it_1][17];
			a_18=shared_0[it_1][18];
			a_19=shared_0[it_1][19];
			a_20=shared_0[it_1][20];
			a_21=shared_0[it_1][21];
			a_22=shared_0[it_1][22];
			a_23=shared_0[it_1][23];
			a_24=shared_0[it_1][24];
			a_25=shared_0[it_1][25];
			a_26=shared_0[it_1][26];
			a_27=shared_0[it_1][27];
			a_28=shared_0[it_1][28];
			a_29=shared_0[it_1][29];
			a_30=shared_0[it_1][30];
			a_31=shared_0[it_1][31];
			b=B((it_1+i), idx);
			sum_0+=(a_0*b);
			sum_1+=(a_1*b);
			sum_2+=(a_2*b);
			sum_3+=(a_3*b);
			sum_4+=(a_4*b);
			sum_5+=(a_5*b);
			sum_6+=(a_6*b);
			sum_7+=(a_7*b);
			sum_8+=(a_8*b);
			sum_9+=(a_9*b);
			sum_10+=(a_10*b);
			sum_11+=(a_11*b);
			sum_12+=(a_12*b);
			sum_13+=(a_13*b);
			sum_14+=(a_14*b);
			sum_15+=(a_15*b);
			sum_16+=(a_16*b);
			sum_17+=(a_17*b);
			sum_18+=(a_18*b);
			sum_19+=(a_19*b);
			sum_20+=(a_20*b);
			sum_21+=(a_21*b);
			sum_22+=(a_22*b);
			sum_23+=(a_23*b);
			sum_24+=(a_24*b);
			sum_25+=(a_25*b);
			sum_26+=(a_26*b);
			sum_27+=(a_27*b);
			sum_28+=(a_28*b);
			sum_29+=(a_29*b);
			sum_30+=(a_30*b);
			sum_31+=(a_31*b);
		}
		__syncthreads();
	}
	{
		C((((bidy*32)+tidy)+0), idx)=sum_0;
	}
	{
		C((((bidy*32)+tidy)+1), idx)=sum_1;
	}
	{
		C((((bidy*32)+tidy)+2), idx)=sum_2;
	}
	{
		C((((bidy*32)+tidy)+3), idx)=sum_3;
	}
	{
		C((((bidy*32)+tidy)+4), idx)=sum_4;
	}
	{
		C((((bidy*32)+tidy)+5), idx)=sum_5;
	}
	{
		C((((bidy*32)+tidy)+6), idx)=sum_6;
	}
	{
		C((((bidy*32)+tidy)+7), idx)=sum_7;
	}
	{
		C((((bidy*32)+tidy)+8), idx)=sum_8;
	}
	{
		C((((bidy*32)+tidy)+9), idx)=sum_9;
	}
	{
		C((((bidy*32)+tidy)+10), idx)=sum_10;
	}
	{
		C((((bidy*32)+tidy)+11), idx)=sum_11;
	}
	{
		C((((bidy*32)+tidy)+12), idx)=sum_12;
	}
	{
		C((((bidy*32)+tidy)+13), idx)=sum_13;
	}
	{
		C((((bidy*32)+tidy)+14), idx)=sum_14;
	}
	{
		C((((bidy*32)+tidy)+15), idx)=sum_15;
	}
	{
		C((((bidy*32)+tidy)+16), idx)=sum_16;
	}
	{
		C((((bidy*32)+tidy)+17), idx)=sum_17;
	}
	{
		C((((bidy*32)+tidy)+18), idx)=sum_18;
	}
	{
		C((((bidy*32)+tidy)+19), idx)=sum_19;
	}
	{
		C((((bidy*32)+tidy)+20), idx)=sum_20;
	}
	{
		C((((bidy*32)+tidy)+21), idx)=sum_21;
	}
	{
		C((((bidy*32)+tidy)+22), idx)=sum_22;
	}
	{
		C((((bidy*32)+tidy)+23), idx)=sum_23;
	}
	{
		C((((bidy*32)+tidy)+24), idx)=sum_24;
	}
	{
		C((((bidy*32)+tidy)+25), idx)=sum_25;
	}
	{
		C((((bidy*32)+tidy)+26), idx)=sum_26;
	}
	{
		C((((bidy*32)+tidy)+27), idx)=sum_27;
	}
	{
		C((((bidy*32)+tidy)+28), idx)=sum_28;
	}
	{
		C((((bidy*32)+tidy)+29), idx)=sum_29;
	}
	{
		C((((bidy*32)+tidy)+30), idx)=sum_30;
	}
	{
		C((((bidy*32)+tidy)+31), idx)=sum_31;
	}
}
