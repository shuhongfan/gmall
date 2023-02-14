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
	printf("创建链表\n");
	creatfromtail(L);
	printf("\n");
	print_list(L);
    ///////////////
	printf("请输入要删除的区间值[min ,max]，（中间用空格隔开）");
	scanf("%d %d",&min,&max);

    Delet( L, min, max);
	printf("\n");
	printf("输出删除后的列表");
	print_list(L);



	printf("\n按值查找操作\nplease intput a number:");
	scanf("%d",&a);
	k=Locate(L,a);
	printf("the location of the number is:%d\n",k);
	printf("\n");
	
    printf("\n删除操作\nplease intput a number:");
	scanf("%d",&b);
	DelList(L,b);
	printf("\n");
	print_list(L);


	printf("\n插入操作\nplease intput the laction:");
	scanf("%d",&i);
	printf("\nplease intput a number:");
	scanf("%d",&w);
	InsList(L,i,w);
	printf("\n");
	print_list(L);

    


}
//尾插法创建单链表
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

//在单链表中查找值为e的元素
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

//在单链表中删除值为x的元素
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

//在单链表中第i和元素前插入值为w的元素
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
//在单链表中删除值在区间[mink,maxk]的元素
int Delet(LinkList L,int mink,int maxk)
{ Node *p,*q,*temp;
    if(mink>maxk)
    { printf("参数不合法");
     return ERROR;
     }
  else
     { p=L;
      while(p-> next &&p->next->data<=mink)
          p=p->next;        /* 循环作用：使得p指向第一个值大于mink结点的前驱*/
      q=p->next;				/* q指向p的后继，即第一个大于mink的结点*/
      while(q&&q->data<maxk)   /* 循环作用：若q的值符合被删条件，则删除q  */
         {// p->next=q->next;
          temp=q;
          q=q->next;
           free(temp);
           
          }
      p->next=q;
      return OK;
      }
}

//打印单链表中的元素
void print_list(LinkList L)
{Node *p;
p=L->next;
while(p!=NULL)
{printf("%d ",p->data);
p=p->next;
}

}