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
	printf("�������ַ���(��@����):");
	scanf("%c",&S1.elem[S1.top]);
	while(S1.elem[S1.top]!='@')
	{
		S1.top++;
		scanf("%c",&S1.elem[S1.top]);
	}
	S1.top--;//��topָ�����һ�����������һ��Ԫ�صĺ���
	a=S1.top;
	while(S1.top!=-1)
	{
		S2.top++;
		S2.elem[S2.top]=S1.elem[S1.top];
		S1.top--;
	}
	S1.top=a;
	while(S1.top!=-1)//���λ��
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
		printf("���ַ����ǻ���!\n");
	else
		printf("���ַ������ǻ���!\n");
}




	//�������ඨ��һ��int i;���ر��λ�øģ�
	for(i=0;i<=a;i++)
	{
		if(S2.elem[i]!=S1.elem[i])
			break;
	}
	if(i-1==a)//��Ϊѭ������iҪ���һ������дi-1
		printf("���ַ����ǻ���!\n");
	else
		printf("���ַ������ǻ���!\n");