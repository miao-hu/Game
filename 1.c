#define _CRT_SECURE_NO_WARNINGS 1
#include<stdio.h>
#include<stdlib.h>

//������(ѭ��)
#if 0
int fac(int n)
{
	int sum = 1;
	while (n > 1)
	{
		sum *= n;
		n -= 1;
	}
	return sum;
}
int main()
{
	int n;
	printf("������n��");
	scanf("%d", &n);
	int ret=fac(n);
	printf("%d\n", ret);
	system("pause");
	return 0;
}
#endif

//�ݹ鷨
int fac(int n)
{
	if (n == 1)
		return 1;
	else 
		return n*fac(n - 1);
}
int main()
{
	int n;
	printf("������n��");
	scanf("%d", &n);
	int ret = fac(n);
	printf("%d\n", ret);
	system("pause");
	return 0;
}
