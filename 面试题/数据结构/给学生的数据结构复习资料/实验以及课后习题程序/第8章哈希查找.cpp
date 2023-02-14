#include<stdio.h>
#define m 16
#define N 1000
typedef struct
{
	int key;
}recordtype;

typedef recordtype hashtable[m];

int hash(int K)
{
	int p,x;
	printf("输入小于表长的最大素数：");
	scanf("%d",&p) ;
	x=K%p ;
	return (x);
}

int hash2(int K,int p)
{
	int x;
	x=K%p;
	return x; 
}
int creat(hashtable ht)
{
	int i,x,q,q1,w,p,j;
	for(i=0;i<m;i++)
	ht[i].key=N;
	printf("输入小于表长的最大素数：");
	scanf("%d",&p) ;
	printf("元素个数：\n"); 
	scanf("%d",&x);
	printf("输入数据："); 
	for(i=0;i<x;i++)
{
	scanf("%d",&w);
	q=hash2(w,p);
	if(ht[q].key!=N)
	{
		for(j=1;j<=m-1;j++)
		{
			q1=(q+j)%m;
			if(ht[q1].key==N) {ht[q1].key=w; break;}
		}
	}
	else ht[q].key=w;

}
 } 
 
int hashsearch(hashtable ht,int K)
{
	int h0,i,hi;
	h0=hash(K); 
	if(ht[h0].key==N) return -1;
	else if(ht[h0].key==K) return (h0);
	else
	{
		for(i=1;i<=m-1;i++)
		{
			hi=(h0+i) %m;
			if(ht[hi].key==N) return -1;
			else if(ht[hi].key==K) return (hi);
		}
	}
 } 

int main()
{
	hashtable ht;
	int K,i,u;
	creat(ht);
	printf("输入要查找的数：\n"); 
	scanf("%d",&K); 
	u=hashsearch(ht, K);
	printf("查找成功，在哈希表的%d位置",u);
}
