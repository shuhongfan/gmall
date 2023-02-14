#include <stdio.h>
#include <conio.h>//使用getch（）函数
#include <stdlib.h>


typedef struct
{
	int elem[100];
	int last;
}SeqList;

SeqList list;

//删除i之后k个元素              
int DelList(SeqList *q, int i, int k)
{int t;
	if (i < 1 && q->last + 1) return 0;
	
	for ( t =i+k-1 ; t<=q->last; t++)
	{
		q->elem[t-k] = q->elem[t];
		
	}
	q->last -= k;
	return 1;
}


//同上方法二               
int del(SeqList *q, int i, int k)
{
	int length = q->last + 1;
	if (i > length && i<1)return 0;
	while (length - i - k + 1)
	{
		q->elem[i - 1] = q->elem[i - 1 + k];
		i++;
	}
	q->last -= k;
	return 1;
}
//排序 选择排序
/*void sort(SeqList *q)
{	int i,min,k,tmp;
	for ( i =0; i < q->last; i++)
	{
		min = q->elem[i];
		k=i;
		while(min >q->elem[i+1])
		{
		   min=q->elem[i+1];
	       k=i+1;}
	if(k!=i)
	{tmp=q->elem[k];
	q->elem[k]=q->elem[i];
    q->elem[i]=tmp;}
	}
}

  */
//删除值x到y的值  
            
int delxtoy(SeqList *L, int x, int y)
{
  int i,j;
  j=0;
for(i=0;i<=L->last;i++)
{if(L->elem[i]<x||L->elem[i]>y)
L->elem[j++]=L->elem[i];
}
L->last=j-1;
return 1;

}

//删除非递减顺序表中所有值相等的元素  
            
int delchongfu(SeqList *L)
{int i,j;
  j=0;
for(i=1;i<=L->last;i++)
{if(L->elem[j]!=L->elem[i])
L->elem[++j]=L->elem[i];

}
L->last=j;
return 1;

}



//录入数据
int setData(SeqList *q,int n)
{int i;
	q->last = -1;
	for (i = 0; i < n; i++)
	{
		scanf("%d",&q->elem[i]);
		q->last += 1;
	}	
	return 1;
}

//排序
void sort(SeqList *q)
{int m,tmp;
	for ( m = 1; m < q->last + 1; m++)
	{
		tmp = q->elem[m];
		while (tmp < q->elem[m - 1])
		{
			q->elem[m] = q->elem[m - 1];
			m--;
		}
		q->elem[m] = tmp;
	}
}
//把x插入到非递减有序的顺序表中，仍保持有序
int insert_x(SeqList *L, int x)
{
  int i;
  sort(L);
  for(i=L->last;i>=0;i--)
{if(L->elem[i]>x)
L->elem[i+1]=L->elem[i];
else
break;
  }
L->elem[i+1]=x;
L->last++;
return 1;

}

//查询（按数据）
int found1(SeqList *q,int sech)
{
	int i = 0;
	while (sech != q->elem[i] && q->last>=i)
	{
		i++;
	}
	if (i <= q->last)
		return i;  //查询成功
	return -1;     //查询失败
}

//查询（按索引）
int found2(SeqList *q, int sech)
{
	if (sech<1 || sech>(q->last + 1)) 
	{
		printf("\n索引输入错误"); 
		return -1;
	}
	return q->elem[sech-1];
}


//插入
int insert(SeqList *q, int n, int data)//n表示插入到那个位置
{int i;
	if (n < 1 || n>q->last + 2) return 0;//插入位置错误
	
	for (i = q->last+1; i > n - 1; i--)
	{
		q->elem[i] = q->elem[i - 1];
	}
	q->elem[i] = data;
	q->last += 1;
	return 1;
}

//输出数据
void print(SeqList *q)
{int i;
	printf("---输出数据---\n");
	for (i = 0; i < q->last + 1; i++)
		printf("%d  ", q->elem[i]);
	printf("\n");
}

void menu()
{
	printf(" * * * * * * * * * * * * * * * * * * * * * * * * * \n");
	printf(" *                 1、录入数据                   * \n");
	printf(" *                 2、查看所有数据               * \n");
	printf(" *                 3、查找（按索引）             * \n");
	printf(" *                 4、查找（按数据）             * \n");
	printf(" *                 5、插入（按索引）             * \n");
	printf(" *                 6、删除（按索引）             * \n");
	printf(" *                 7、退出                       * \n");
	printf(" *                 8、删除从第i个位置起的k个元素 * \n");
	printf(" *                 9、删除从x到y的所有元素 * \n");
	printf(" *                 10、排序  \n");
	printf(" *                 11、删除非递减顺序表中的所有重复元素 * \n");
    printf("在非递减有序的顺序表插入x后保持有序\n");
	printf(" * * * * * * * * * * * * * * * * * * * * * * * * * *\n");
	printf("--请输入功能序号：");
	
}

void bak()
{	char a;
	printf("\n请按任意键退出");

	a = getch();
	return;
}



int main()
{
	SeqList *q = &list;
	int n,k;
	int a;
	int a1, data1;	
	int a2, data2;
	int  data;
	int i,x,y;
	while (1)
	{
		menu();
		scanf("%d",&n);
		switch (n)
		{
		case 1:
			system("cls");
			
			printf("---正在插入数据---\n");
			printf("你需要几个数据：");
			scanf("%d",&a);
			printf("请输入数据：\n");
			setData(q, a);
			bak();
			system("cls");
			break;
		case 2:
			system("cls");
			print(q);
			bak();
			system("cls");
			break;
		case 3:
			system("cls");
			
			printf("---正在查询数据---\n");
			printf("请输入你要查询第几个数：\n");
			scanf("%d",&a1);
			data1 = found2(q, a1);
			if (data1 != -1) printf("%d\n", data1);
			bak();
			system("cls");
			break;
		case 4:
			system("cls");
		
			printf("---正在查询数据---\n");
			printf("请输入你要查询的数据：\n");
			scanf("%d", &a2);
			data2 = found1(q, a2);
			if (data2 != -1) printf("此数是第 %d 个数\n", data2+1);
			else printf("没有这个数\n");
			bak();
			system("cls"); 
			break;
		case 5:
			
			system("cls");
			printf("---正在插入数据---\n");
			printf("请输入你要插入的数据：\n");
			scanf("%d", &data);
			printf("请输入你要插入的位置：\n");
			scanf("%d",&n);
			insert(q,  n, data); 
			bak();
			system("cls");
			break;
		case 6:
			
			system("cls");
			printf("请输入你要删除的数的位置：\n");
			scanf("%d",&i);
			del(q, i, 1);
			bak();
			system("cls");
			break;
		case 7:
			exit(0);
			break;
		case 8:printf("请输入你要删除的位置i和k的值：\n");
			print(q);
			scanf("%d%d",&i,&k);
			DelList(q, i, k);
			print(q);
				break;
		case 9:	print(q);
			printf("请输入x和y的值：\n");
			scanf("%d%d",&x,&y);
			delxtoy(q,x,y);
			print(q);
			break;
		case 10:print(q);
			printf("排序\n");
			sort(q);
			print(q);
			break;
		case 11:printf("删除非递减顺序表中的重复元素\n");
			delchongfu(q);
			print(q);
			break;
        case 12:printf("在非递减有序的顺序表插入x后保持有序\n");
			print(q);
            printf("输入要插入的x的值\n");
			scanf("%d",&x);
			insert_x(q,x);
				print(q);break;
		}
	}
	return 0;


}

