#include<stdio.h>
#include<stdlib.h> 

typedef char datatype;
typedef struct node
{
	datatype data;
	struct node *Lchild;
	struct node *Rchild;
}bitnode,*bitree;

//��ʼ����
int initlist (bitree *root)
{	*root=(bitree)malloc(sizeof(bitnode));
	(*root)->Lchild=NULL; 
	(*root)->Rchild=NULL; 
	return 0;
}
 
//������ 
void createbitree(bitree *bt)
{
	char ch; 
	ch=getchar();
  	if(ch=='.') *bt=NULL;
	else 
	{
		*bt=(bitree)malloc(sizeof(bitnode));
		(*bt)->data=ch;
		createbitree(&((*bt)->Lchild));
		createbitree(&((*bt)->Rchild));
	}
	 
}

//ͳ��Ҷ��
int leaf(bitree root)
{ 
	int c;
	if(root==NULL) c=0;
	else 
	if((root->Lchild==NULL)&&(root->Rchild==NULL))
	c=1;
	else 
	c=leaf(root->Lchild)+leaf(root->Rchild);
	return c;
 } 

//�����
void preorder(bitree root)
{
	if(root!=NULL)
	{
		printf("%c ",root->data);
		preorder(root->Lchild);
		preorder(root->Rchild);
	}
}

void inorder(bitree root)
{
	if(root!=NULL)
	{
	    inorder(root->Lchild);
		printf("%c ",root->data);		
		inorder(root->Rchild);
	}
}

void postorder(bitree root)
{
	if(root!=NULL)
	{
	    postorder(root->Lchild);		
		postorder(root->Rchild);
		printf("%c ",root->data);
	}
}

//���Ҷ��
void  preorder2(bitree root)
{
	if(root!=NULL)
	{
		if((root->Lchild==NULL)&&(root->Rchild==NULL))
		printf("%c ",root->data);
		preorder2(root->Lchild);
		preorder2(root->Rchild);		
	}
}

//�������
int posttree(bitree root)
{
	int hr,hl,max;
	if(root!=NULL)
	{
		hl=posttree(root->Lchild);
		hr=posttree(root->Rchild);
		max=hl>hr?hl:hr;
		return (max+1);
		
	}
	else return 0;

 } 

//��Ҷ�ӽ��ĸ���
int b(bitree p)
{
	int i,N,n;
	i=0;N=0;n=0;
    while(p!=NULL)
    {
    	if(p->Lchild!=NULL)
	   {
		i++;
		N=b(p->Lchild);
		
		return (i+N);
	   }
		else if(p->Rchild!=NULL)
		{
			i++;
			n=b(p->Rchild);
			return (i+n);
		}
	   else return (0);
	}
	
}

int main()
{
	bitree root;
	int c,u;
		    initlist (&root);
		    printf("������������:\n");
	       	printf("������: ");
	        createbitree(&root);	
	do{
        printf("\t\t*****************************\n");
        printf("\t\t*    [1]----NULL            *\n");
        printf("\t\t*    [2]----Ҷ�ӵ���Ŀ      *\n");
        printf("\t\t*    [3]----������          *\n");
        printf("\t\t*    [4]----��ʾҶ��        *\n");
        printf("\t\t*    [5]----�������        *\n");
        printf("\t\t*    [6]----��Ҷ�ӽ��ĸ���*\n");
        printf("\t\t*    [0]----�˳�ϵͳ        *\n");
        printf("\t\t*****************************\n");
        printf("\t\t���빦�ܼ���"); 
		scanf("%d",&u); printf("\n");
		switch(u)
		{
			case 0 :break;
			case 2 :
			c=leaf(root);
	        printf("Ҷ�ӵ���ĿΪ��%d\n",c); 
	        break;
			case 3 :
	     	printf("�����������"); 
         	preorder(root);
        	printf("\n"); 
        	printf("�����������"); 
         	inorder(root);
        	printf("\n"); 
        	printf("�����������"); 
         	postorder(root);
        	printf("\n"); 
        	break;
			case 4 :
		   	printf("Ҷ�ӣ�"); 
	        preorder2(root);
	        printf("\n");
	        break; 
	        case 5:
	        printf("�������Ϊ��"); 
			printf("%d",posttree(root));printf("\n");
	        break;
	        case 6:
	        printf("��Ҷ�ӽ��ĸ���Ϊ��"); 
			printf("%d",b(root));printf("\n");	 break;       	
			default : 
			printf("���ܼ���Ч");
			printf("\n");
			break;
		}
		   }while(u!=0);
return 0;}
