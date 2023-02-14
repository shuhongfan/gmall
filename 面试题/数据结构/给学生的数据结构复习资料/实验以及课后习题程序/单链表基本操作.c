#include <stdio.h>
#include<stdlib.h>

#define OK 1
#define ERROR 0

typedef struct Node
{
	int data;
	struct Node *next;
}Node,*LinkList;


void main()
{
	void creatfromtail(LinkList L);
	int Locate(LinkList L,int e);
	void DelList(LinkList L,int x);
	void InsList(LinkList L,int i,int w);
	void print_list(LinkList L);

	int a,b,k,i,w;
	int min,max;
	LinkList L;
	L=(LinkList)malloc(sizeof(Node));
	L->next=NULL;
	printf("��������\n");
	creatfromtail(L);
	printf("\n");
	print_list(L);
    ///////////////
	printf("������Ҫɾ��������ֵ[min ,max]�����м��ÿո������");
	scanf("%d %d",&min,&max);

    Delet( L, min, max);
	printf("\n");
	printf("���ɾ������б�");
	print_list(L);



	printf("\n��ֵ���Ҳ���\nplease intput a number:");
	scanf("%d",&a);
	k=Locate(L,a);
	printf("the location of the number is:%d\n",k);
	printf("\n");
	
    printf("\nɾ������\nplease intput a number:");
	scanf("%d",&b);
	DelList(L,b);
	printf("\n");
	print_list(L);


	printf("\n�������\nplease intput the laction:");
	scanf("%d",&i);
	printf("\nplease intput a number:");
	scanf("%d",&w);
	InsList(L,i,w);
	printf("\n");
	print_list(L);

    


}
//β�巨����������
void creatfromtail(LinkList L)
{
	Node *r,*s;
	int flag=1;
	int c;
	r=L;
	printf("number:");
	while(flag)
	{
		scanf("%d",&c);
		if(c!=-1)
		{
			s=(Node *)malloc(sizeof(Node));
			s->data=c;
			r->next=s;
			r=s;
		}
		else
		{
			flag=0;
			r->next=NULL;
		}
	}
}

//�ڵ������в���ֵΪe��Ԫ��
int Locate(LinkList L,int e)
{
	Node *p;
	int j=1;
	p=L->next;
	while(p!=NULL)
	{
		if(p->data!=e)
		{
			p=p->next;
			j++;
		}
		else 
			break;
	}
	return j;
}

//�ڵ�������ɾ��ֵΪx��Ԫ��
void DelList(LinkList L,int x)
{
	LinkList p,pre;
	pre=L->next;
	p=L;
	while(pre->data!=x)
	{
		pre=pre->next;
		p=p->next;
	}
    p->next=pre->next;
	free(pre);	

	p=L->next;
	while(p!=NULL)
	{
		printf("%d ",p->data);
		p=p->next;
		
	}
	printf("\n");
}

//�ڵ������е�i��Ԫ��ǰ����ֵΪw��Ԫ��
void InsList(LinkList L,int i,int w)
{
	Node *pre,*s;
	int k=1;
	pre=L->next; 
	if(i<=0)
	{
		printf("the location is illegal.\n");
		
	}

	while(pre!=NULL&&k<i-1)
	{
		pre=pre->next;
		k++;
	}
	if(!pre)
	{
		printf("the location isillegal.\n");
		
	}
	s=(Node*)malloc(sizeof(Node));
	s->data=w;
	s->next=pre->next;
	pre->next=s;
	
	pre=L->next;
	while(pre!=NULL)
	{
		printf("%d ",pre->data);
		pre=pre->next;
		
	}
	printf("\n");
}
//�ڵ�������ɾ��ֵ������[mink,maxk]��Ԫ��
int Delet(LinkList L,int mink,int maxk)
{ Node *p,*q,*temp;
    if(mink>maxk)
    { printf("�������Ϸ�");
     return ERROR;
     }
  else
     { p=L;
      while(p-> next &&p->next->data<=mink)
          p=p->next;        /* ѭ�����ã�ʹ��pָ���һ��ֵ����mink����ǰ��*/
      q=p->next;				/* qָ��p�ĺ�̣�����һ������mink�Ľ��*/
      while(q&&q->data<maxk)   /* ѭ�����ã���q��ֵ���ϱ�ɾ��������ɾ��q  */
         {// p->next=q->next;
          temp=q;
          q=q->next;
           free(temp);
           
          }
      p->next=q;
      return OK;
      }
}

//��ӡ�������е�Ԫ��
void print_list(LinkList L)
{Node *p;
p=L->next;
while(p!=NULL)
{printf("%d ",p->data);
p=p->next;
}

}