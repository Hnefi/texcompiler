__global__ void matmul(float * A, float * B, float * C, int width, int height)
{
__shared__ float shared_0[16][17];
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
for (i=0; i<width; i=(i+16))
{
int it_1;
shared_0[((tidx%16)+0)][(tidx/16)]=A((((bidy*16)+tidy)+(tidx/16)), (i+(tidx%16)));
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
}
__syncthreads();
}
int it_0;
for (it_0=0; it_0<1; it_0=(it_0+1))
{
C((((bidy*16)+tidy)+0), idx)=sum_0;
}
int it_2;
for (it_2=0; it_2<1; it_2=(it_2+1))
{
C((((bidy*16)+tidy)+1), idx)=sum_1;
}
int it_3;
for (it_3=0; it_3<1; it_3=(it_3+1))
{
C((((bidy*16)+tidy)+2), idx)=sum_2;
}
int it_4;
for (it_4=0; it_4<1; it_4=(it_4+1))
{
C((((bidy*16)+tidy)+3), idx)=sum_3;
}
int it_5;
for (it_5=0; it_5<1; it_5=(it_5+1))
{
C((((bidy*16)+tidy)+4), idx)=sum_4;
}
int it_6;
for (it_6=0; it_6<1; it_6=(it_6+1))
{
C((((bidy*16)+tidy)+5), idx)=sum_5;
}
int it_7;
for (it_7=0; it_7<1; it_7=(it_7+1))
{
C((((bidy*16)+tidy)+6), idx)=sum_6;
}
int it_8;
for (it_8=0; it_8<1; it_8=(it_8+1))
{
C((((bidy*16)+tidy)+7), idx)=sum_7;
}
int it_9;
for (it_9=0; it_9<1; it_9=(it_9+1))
{
C((((bidy*16)+tidy)+8), idx)=sum_8;
}
int it_10;
for (it_10=0; it_10<1; it_10=(it_10+1))
{
C((((bidy*16)+tidy)+9), idx)=sum_9;
}
int it_11;
for (it_11=0; it_11<1; it_11=(it_11+1))
{
C((((bidy*16)+tidy)+10), idx)=sum_10;
}
int it_12;
for (it_12=0; it_12<1; it_12=(it_12+1))
{
C((((bidy*16)+tidy)+11), idx)=sum_11;
}
int it_13;
for (it_13=0; it_13<1; it_13=(it_13+1))
{
C((((bidy*16)+tidy)+12), idx)=sum_12;
}
int it_14;
for (it_14=0; it_14<1; it_14=(it_14+1))
{
C((((bidy*16)+tidy)+13), idx)=sum_13;
}
int it_15;
for (it_15=0; it_15<1; it_15=(it_15+1))
{
C((((bidy*16)+tidy)+14), idx)=sum_14;
}
int it_16;
for (it_16=0; it_16<1; it_16=(it_16+1))
{
C((((bidy*16)+tidy)+15), idx)=sum_15;
}
}

