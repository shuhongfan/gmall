#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#define MAX_VERTEX_NUM 20
#include <stdlib.h>
#include <stdio.h>
typedef int ElementType;

typedef struct Node
{
	ElementType data;
	struct Node *next;
}QueueNode;

typedef struct
{
	QueueNode *front;
	QueueNode *rear;
}LinkQueue;


int visited[MAX_VERTEX_NUM];
typedef int AdjType;
typedef enum{DG, DN, UDG, UDN}GraphKind;
typedef char VertexData;

typedef struct{
	AdjType adj;
}ArcNode;

typedef struct{
	VertexData vertexData[MAX_VERTEX_NUM];//��������
	ArcNode arcNode[MAX_VERTEX_NUM][MAX_VERTEX_NUM];//��
	int vernum, arcnum;
	GraphKind kind;
}AdjMatrix;

//��ʼ������
void initLinkQueue(LinkQueue *q)
{
	QueueNode *node = (QueueNode*)malloc(sizeof(QueueNode));
	if (node == NULL)return;
	q->front = q->rear = node;
	q->rear->next = NULL;
}

//���
int EnterQueue(LinkQueue *q, ElementType data)
{
	QueueNode *node = (QueueNode*)malloc(sizeof(QueueNode));
	if (node == NULL)return 0;
	node->data = data;
	node->next = q->rear->next;
	q->rear->next = node;
	q->rear = node;
	return 1;
}

//����
int DeleteQueue(LinkQueue *q, ElementType *data)
{
	if (q->front == q->rear)return 0;
	QueueNode *temp = q->front->next;
	if (data != NULL)
		*data = temp->data;
	q->front->next = temp->next;
	if (temp == q->rear) q->rear = q->front;
	free(temp);
	return 1;
}

//��ȡ������Ԫ��
int getFirst(LinkQueue *q, ElementType *data)
{
	if (q->front == q->rear)return 0;
	*data = q->front->next->data;
	return 1;
}

//����
void traverse(LinkQueue *q)
{
	QueueNode *p = q->front->next;
	while (p != NULL)
	{
		printf("%d\n", p->data);
		p = p->next;
	}
}

//�ж��Ƿ�Ϊ�ն�
int isEmpty(LinkQueue *q)
{
	if (q->front == q->rear)return 1;
	return 0;
}


//��λ����λ��
int locateVertex(AdjMatrix *g, VertexData data)
{
	int i, result = -1;
	for (i = 0; i < g->vernum; i++)
	{
		if (g->vertexData[i] == data)
		{
			result = i;
			break;
		}
	}
	return result;
}

//�Զ�����������
int createDN(AdjMatrix *g, int n)
{int i,j;
int arrays[MAX_VERTEX_NUM][MAX_VERTEX_NUM];
	int a = 0;
	//��ʼ����������
	for ( i = 0; i < g->vernum; i++){
		for ( j = 0; j < g->vernum; j++)
			g->arcNode[i][j].adj = -1;
	}
	//���Ȩֵ
	
	for ( i = 0; i < n; i++)
	for ( j = 0; j < n; j++)
	{
		if (i == j)
		{
			g->arcNode[i][j].adj = -1;
			continue;
		}
		//�����������rand���������Ȩֵ
		srand((unsigned int)time(NULL) + a++);
		int temp = rand() % 10;
		int temp_2 = rand() % 20 + 1;
		if (temp >= 6)
			g->arcNode[i][j].adj = temp_2;
		else g->arcNode[i][j].adj = -1;
	}
	g->vernum = n;
	g->arcnum = 0;
	for ( i = 0; i < n; i++)
	for ( j = 0; j < n; j++)
	if (g->arcNode[i][j].adj != -1)g->arcnum++;
	printf("������Ŀ��%d�ͻ���Ŀ��%d\n", g->vernum, g->arcnum);
	//¼��ͼ�Ķ���
	VertexData vertexData;
	printf("����������㣺\n");
	for ( i = 0, j = 65; i < g->vernum; i++, j++)
	{
		g->vertexData[i] = (char)j;
		printf("%c ", g->vertexData[i]);
	}
	//������
	printf("\n�����ض����Լ�Ȩֵ��\n");
	for ( i = 0; i < g->vernum; i++)
	{
		for ( j = 0; j < g->vernum; j++)
		{
			if (g->arcNode[i][j].adj != -1)
			{
				//g->arcNode[i][j].adj = arrays[i][j];
				printf("%c->%c Ȩֵ��%d\t", g->vertexData[i], g->vertexData[j], g->arcNode[i][j].adj);
			}
		}
		printf("\n");
	}
	
	printf("\n\t");
	for ( i = 0; i < g->vernum; i++)
		printf("%c\t", g->vertexData[i]);
	printf("\n\t");
	for ( i = 0; i < g->vernum; i++)
		printf("��\t");
	printf("\n");
	//���ɼ����ڽӾ���
	for ( i = 0; i < g->vernum; i++)
	{
		printf("%c|\t", g->vertexData[i]);
		for ( j = 0; j < g->vernum; j++)
		{

			if (g->arcNode[i][j].adj != -1)
			{
				//g->arcNode[i][j].adj = arrays[i][j];
				printf("%d\t", g->arcNode[i][j].adj);
			}
			else
				printf(".\t");
		}
		printf("\n");
	}
	printf("\n");
	return 1;
}

//����������
int createUDN(AdjMatrix *g)
{int i,j;
 int weight, locate1, locate2;
	printf("�����붥����Ŀ�ͻ���Ŀ��");
	scanf("%d,%d%c", &g->vernum, &g->arcnum);
	//��ʼ����������
	for ( i = 0; i < g->vernum; i++){
		for ( j = 0; j < g->vernum; j++)
			g->arcNode[i][j].adj = -1;
	}
	//¼��ͼ�Ķ���
	VertexData vertexData;
	printf("���������붥�㣺\n");
	for ( i = 0; i < g->vernum; i++)
		scanf("%c%c", &g->vertexData[i]);
	//������
	for ( i = 0; i < g->arcnum; i++)
	{
		
		VertexData v1, v2;
		printf("��������ض����Լ�Ȩֵ��\n");
		scanf("%c, %c, %d%c", &v1, &v2, &weight);
		locate1 = locateVertex(g, v1);
		locate2 = locateVertex(g, v2);
		g->arcNode[locate1][locate2].adj = weight;
	}
	return 1;
}



int firstAdjVertex(AdjMatrix g, int v)
{int i;
	for ( i = 0; i < g.vernum; i++)
	if (g.arcNode[v][i].adj != -1)return i;
	return -1;
}

int nextAdjVertex(AdjMatrix g, int v, int w)
{int i;
	for ( i = w + 1; i < g.vernum; i++)
	if (g.arcNode[v][i].adj != -1)return i;
	return -1;
}

void deathFirstSearch(AdjMatrix g, int v)
{
	int w;
	//����ֵ
	printf("%c ", g.vertexData[v]);
	//���Ϊ���ʹ�
	visited[v] = 1;
	w = firstAdjVertex(g, v);
	while (w != -1)
	{
		if (!visited[v])deathFirstSearch(g, w);
		w = nextAdjVertex(g, v, w);
	}
}

//������ȱ���
void traverserGraph_1(AdjMatrix g)
{int i;
	for ( i = 0; i < g.vernum; i++)
		visited[i] = 0;
	for ( i = 0; i < g.vernum; i++)
	if (!visited[i])deathFirstSearch(g, i);
}

void breadthFirstSearch(AdjMatrix g, int v)
{
	//��������
	printf("%c ", g.vertexData[v]);
	//���ѷ����������޸�ֵ
	visited[v] = 1;
	//��ʼ���ն�
	LinkQueue queue, *q = &queue;
	initLinkQueue(q);
	//v����
	EnterQueue(q, v);
	while (!isEmpty(q))
	{
		int temp;
		DeleteQueue(q, &temp);
		int w = firstAdjVertex(g, temp);
		while (w != -1)
		{
			if (!visited[w])
			{
				printf("%c ", g.vertexData[w]);
				visited[w] = 1;
				EnterQueue(q, w);
			}
			w = nextAdjVertex(g, v, w);
		}
	}
}

//������ȱ���
void traverserGraph_2(AdjMatrix g)
{int i;
	for ( i = 0; i < g.vernum; i++)
		visited[i] = 0;
	for ( i = 0; i < g.vernum; i++)
	if (!visited[i])breadthFirstSearch(g, i);
}

int main()
{
	AdjMatrix adjMatrix, *g = &adjMatrix;

	//createDN(g);
	/*int arrays[6][6] = { { -1, 5, -1, 7, -1, -1 },
	{ -1, -1, 4, -1, -1, -1 },
	{ 8, -1, -1, -1, -1, 9 },
	{ -1, -1, 5, -1, -1, 6 },
	{ -1, -1, -1, 5, -1, -1 },
	{ 2, -1, -1, -1, 1, -1 } };*/
	int count;
	printf("�����붥����Ŀ��");
	scanf("%d", &count);
	createDN(g, count);

	printf("������ȱ�����\n");
	traverserGraph_1(adjMatrix);

	printf("\n������ȱ�����\n");
	traverserGraph_2(adjMatrix);

	return 0;
}