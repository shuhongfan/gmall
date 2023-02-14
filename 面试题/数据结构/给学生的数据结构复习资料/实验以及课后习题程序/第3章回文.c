#include<stdio.h>
#define max 50
typedef char StackElementType;
typedef struct
{
	StackElementType elem[max];
	int top;
}SeqStack;
void csh(SeqStack *S)
{
	S->top=-1;
}
int main()
{
	SeqStack S1,S2;
	int a;
	csh(&S1);
	csh(&S2);
	S1.top++;
	printf("请输入字符串(以@结束):");
	scanf("%c",&S1.elem[S1.top]);
	while(S1.elem[S1.top]!='@')
	{
		S1.top++;
		scanf("%c",&S1.elem[S1.top]);
	}
	S1.top--;//让top指向最后一个而不是最后一个元素的后面
	a=S1.top;
	while(S1.top!=-1)
	{
		S2.top++;
		S2.elem[S2.top]=S1.elem[S1.top];
		S1.top--;
	}
	S1.top=a;
	while(S1.top!=-1)//标记位置
	{
		if(S2.elem[S2.top]==S1.elem[S1.top])
		{
			S2.top--;
			S1.top--;
		}
		else
			break;
	}
	if(S1.top==-1)
		printf("该字符串是回文!\n");
	else
		printf("该字符串不是回文!\n");
}




	//方二：多定义一个int i;（重标记位置改）
	for(i=0;i<=a;i++)
	{
		if(S2.elem[i]!=S1.elem[i])
			break;
	}
	if(i-1==a)//因为循环完了i要多加一次所以写i-1
		printf("该字符串是回文!\n");
	else
		printf("该字符串不是回文!\n");