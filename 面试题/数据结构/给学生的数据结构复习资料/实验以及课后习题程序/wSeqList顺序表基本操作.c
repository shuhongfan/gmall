#include <stdio.h>
#include <conio.h>//ʹ��getch��������
#include <stdlib.h>


typedef struct
{
	int elem[100];
	int last;
}SeqList;

SeqList list;

//ɾ��i֮��k��Ԫ��              
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


//ͬ�Ϸ�����               
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
//���� ѡ������
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
//ɾ��ֵx��y��ֵ  
            
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

//ɾ���ǵݼ�˳���������ֵ��ȵ�Ԫ��  
            
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



//¼������
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

//����
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
//��x���뵽�ǵݼ������˳����У��Ա�������
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

//��ѯ�������ݣ�
int found1(SeqList *q,int sech)
{
	int i = 0;
	while (sech != q->elem[i] && q->last>=i)
	{
		i++;
	}
	if (i <= q->last)
		return i;  //��ѯ�ɹ�
	return -1;     //��ѯʧ��
}

//��ѯ����������
int found2(SeqList *q, int sech)
{
	if (sech<1 || sech>(q->last + 1)) 
	{
		printf("\n�����������"); 
		return -1;
	}
	return q->elem[sech-1];
}


//����
int insert(SeqList *q, int n, int data)//n��ʾ���뵽�Ǹ�λ��
{int i;
	if (n < 1 || n>q->last + 2) return 0;//����λ�ô���
	
	for (i = q->last+1; i > n - 1; i--)
	{
		q->elem[i] = q->elem[i - 1];
	}
	q->elem[i] = data;
	q->last += 1;
	return 1;
}

//�������
void print(SeqList *q)
{int i;
	printf("---�������---\n");
	for (i = 0; i < q->last + 1; i++)
		printf("%d  ", q->elem[i]);
	printf("\n");
}

void menu()
{
	printf(" * * * * * * * * * * * * * * * * * * * * * * * * * \n");
	printf(" *                 1��¼������                   * \n");
	printf(" *                 2���鿴��������               * \n");
	printf(" *                 3�����ң���������             * \n");
	printf(" *                 4�����ң������ݣ�             * \n");
	printf(" *                 5�����루��������             * \n");
	printf(" *                 6��ɾ������������             * \n");
	printf(" *                 7���˳�                       * \n");
	printf(" *                 8��ɾ���ӵ�i��λ�����k��Ԫ�� * \n");
	printf(" *                 9��ɾ����x��y������Ԫ�� * \n");
	printf(" *                 10������  \n");
	printf(" *                 11��ɾ���ǵݼ�˳����е������ظ�Ԫ�� * \n");
    printf("�ڷǵݼ������˳������x�󱣳�����\n");
	printf(" * * * * * * * * * * * * * * * * * * * * * * * * * *\n");
	printf("--�����빦����ţ�");
	
}

void bak()
{	char a;
	printf("\n�밴������˳�");

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
			
			printf("---���ڲ�������---\n");
			printf("����Ҫ�������ݣ�");
			scanf("%d",&a);
			printf("���������ݣ�\n");
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
			
			printf("---���ڲ�ѯ����---\n");
			printf("��������Ҫ��ѯ�ڼ�������\n");
			scanf("%d",&a1);
			data1 = found2(q, a1);
			if (data1 != -1) printf("%d\n", data1);
			bak();
			system("cls");
			break;
		case 4:
			system("cls");
		
			printf("---���ڲ�ѯ����---\n");
			printf("��������Ҫ��ѯ�����ݣ�\n");
			scanf("%d", &a2);
			data2 = found1(q, a2);
			if (data2 != -1) printf("�����ǵ� %d ����\n", data2+1);
			else printf("û�������\n");
			bak();
			system("cls"); 
			break;
		case 5:
			
			system("cls");
			printf("---���ڲ�������---\n");
			printf("��������Ҫ��������ݣ�\n");
			scanf("%d", &data);
			printf("��������Ҫ�����λ�ã�\n");
			scanf("%d",&n);
			insert(q,  n, data); 
			bak();
			system("cls");
			break;
		case 6:
			
			system("cls");
			printf("��������Ҫɾ��������λ�ã�\n");
			scanf("%d",&i);
			del(q, i, 1);
			bak();
			system("cls");
			break;
		case 7:
			exit(0);
			break;
		case 8:printf("��������Ҫɾ����λ��i��k��ֵ��\n");
			print(q);
			scanf("%d%d",&i,&k);
			DelList(q, i, k);
			print(q);
				break;
		case 9:	print(q);
			printf("������x��y��ֵ��\n");
			scanf("%d%d",&x,&y);
			delxtoy(q,x,y);
			print(q);
			break;
		case 10:print(q);
			printf("����\n");
			sort(q);
			print(q);
			break;
		case 11:printf("ɾ���ǵݼ�˳����е��ظ�Ԫ��\n");
			delchongfu(q);
			print(q);
			break;
        case 12:printf("�ڷǵݼ������˳������x�󱣳�����\n");
			print(q);
            printf("����Ҫ�����x��ֵ\n");
			scanf("%d",&x);
			insert_x(q,x);
				print(q);break;
		}
	}
	return 0;


}

