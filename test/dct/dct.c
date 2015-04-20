#define WIDTH_A 2048
#define WIDTH_C 2048
#define COALESCED_NUM  16
#define A(y,x) A[(y)*WIDTH_A+(x)]
#define C(y,x) C[(y)*WIDTH_C+(x)]
#define globalDimY 1
#define blockDimX 16
#define blockDimY 1
__global__ void dct(float *A, float *B, float *C, int width) {
	int i;
	float sum;
	sum = 0;

	__shared__ float shared0[16];
	shared0[tidx] = A(idy, idx);

	for (i=0; i<8; i=i+1) {
		float a;
		float b;
		a = shared0[i];
		b = 1;
		sum += a*b;
	}
	C(idy, idx) = sum;
}


