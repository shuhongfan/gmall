#include<stdio.h>
#include<stdlib.h> 

typedef char datatype;
typedef struct node
{
	datatype data;
	struct node *Lchild;
	struct node *Rchild;
}bitnode,*bitree;

//初始化树
int initlist (bitree *root)
{	*root=(bitree)malloc(sizeof(bitnode));
	(*root)->Lchild=NULL; 
	(*root)->Rchild=NULL; 
	return 0;
}
 
//建立树 
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

//统计叶子
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

//输出树
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

//输出叶子
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

//树的深度
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

//非叶子结点的个数
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
		    printf("建立二叉链表:\n");
	       	printf("请输入: ");
	        createbitree(&root);	
	do{
        printf("\t\t*****************************\n");
        printf("\t\t*    [1]----NULL            *\n");
        printf("\t\t*    [2]----叶子的数目      *\n");
        printf("\t\t*    [3]----遍历树          *\n");
        printf("\t\t*    [4]----显示叶子        *\n");
        printf("\t\t*    [5]----树的深度        *\n");
        printf("\t\t*    [6]----非叶子结点的个数*\n");
        printf("\t\t*    [0]----退出系统        *\n");
        printf("\t\t*****************************\n");
        printf("\t\t输入功能键："); 
		scanf("%d",&u); printf("\n");
		switch(u)
		{
			case 0 :break;
			case 2 :
			c=leaf(root);
	        printf("叶子的数目为：%d\n",c); 
	        break;
			case 3 :
	     	printf("先序遍历树："); 
         	preorder(root);
        	printf("\n"); 
        	printf("中序遍历树："); 
         	inorder(root);
        	printf("\n"); 
        	printf("后序遍历树："); 
         	postorder(root);
        	printf("\n"); 
        	break;
			case 4 :
		   	printf("叶子："); 
	        preorder2(root);
	        printf("\n");
	        break; 
	        case 5:
	        printf("树的深度为："); 
			printf("%d",posttree(root));printf("\n");
	        break;
	        case 6:
	        printf("非叶子结点的个数为："); 
			printf("%d",b(root));printf("\n");	 break;       	
			default : 
			printf("功能键无效");
			printf("\n");
			break;
		}
		   }while(u!=0);
return 0;}
