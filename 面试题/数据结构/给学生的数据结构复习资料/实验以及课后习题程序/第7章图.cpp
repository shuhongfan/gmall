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
	VertexData vertexData[MAX_VERTEX_NUM];//顶点数据
	ArcNode arcNode[MAX_VERTEX_NUM][MAX_VERTEX_NUM];//弧
	int vernum, arcnum;
	GraphKind kind;
}AdjMatrix;

//初始化链队
void initLinkQueue(LinkQueue *q)
{
	QueueNode *node = (QueueNode*)malloc(sizeof(QueueNode));
	if (node == NULL)return;
	q->front = q->rear = node;
	q->rear->next = NULL;
}

//入队
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

//出队
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

//获取队首首元素
int getFirst(LinkQueue *q, ElementType *data)
{
	if (q->front == q->rear)return 0;
	*data = q->front->next->data;
	return 1;
}

//遍历
void traverse(LinkQueue *q)
{
	QueueNode *p = q->front->next;
	while (p != NULL)
	{
		printf("%d\n", p->data);
		p = p->next;
	}
}

//判断是否为空队
int isEmpty(LinkQueue *q)
{
	if (q->front == q->rear)return 1;
	return 0;
}


//定位顶点位置
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

//自动创建有向网
int createDN(AdjMatrix *g, int n)
{int i,j;
int arrays[MAX_VERTEX_NUM][MAX_VERTEX_NUM];
	int a = 0;
	//初始化矩阵数据
	for ( i = 0; i < g->vernum; i++){
		for ( j = 0; j < g->vernum; j++)
			g->arcNode[i][j].adj = -1;
	}
	//随机权值
	
	for ( i = 0; i < n; i++)
	for ( j = 0; j < n; j++)
	{
		if (i == j)
		{
			g->arcNode[i][j].adj = -1;
			continue;
		}
		//利用随机函数rand，随机产生权值
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
	printf("顶点数目：%d和弧数目：%d\n", g->vernum, g->arcnum);
	//录入图的顶点
	VertexData vertexData;
	printf("依次输出顶点：\n");
	for ( i = 0, j = 65; i < g->vernum; i++, j++)
	{
		g->vertexData[i] = (char)j;
		printf("%c ", g->vertexData[i]);
	}
	//建立弧
	printf("\n输出相关顶点以及权值：\n");
	for ( i = 0; i < g->vernum; i++)
	{
		for ( j = 0; j < g->vernum; j++)
		{
			if (g->arcNode[i][j].adj != -1)
			{
				//g->arcNode[i][j].adj = arrays[i][j];
				printf("%c->%c 权值：%d\t", g->vertexData[i], g->vertexData[j], g->arcNode[i][j].adj);
			}
		}
		printf("\n");
	}
	
	printf("\n\t");
	for ( i = 0; i < g->vernum; i++)
		printf("%c\t", g->vertexData[i]);
	printf("\n\t");
	for ( i = 0; i < g->vernum; i++)
		printf("—\t");
	printf("\n");
	//构成简易邻接矩阵
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

//创建无向网
int createUDN(AdjMatrix *g)
{int i,j;
 int weight, locate1, locate2;
	printf("请输入顶点数目和弧数目：");
	scanf("%d,%d%c", &g->vernum, &g->arcnum);
	//初始化矩阵数据
	for ( i = 0; i < g->vernum; i++){
		for ( j = 0; j < g->vernum; j++)
			g->arcNode[i][j].adj = -1;
	}
	//录入图的顶点
	VertexData vertexData;
	printf("请依次输入顶点：\n");
	for ( i = 0; i < g->vernum; i++)
		scanf("%c%c", &g->vertexData[i]);
	//建立弧
	for ( i = 0; i < g->arcnum; i++)
	{
		
		VertexData v1, v2;
		printf("请输入相关顶点以及权值：\n");
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
	//访问值
	printf("%c ", g.vertexData[v]);
	//标记为访问过
	visited[v] = 1;
	w = firstAdjVertex(g, v);
	while (w != -1)
	{
		if (!visited[v])deathFirstSearch(g, w);
		w = nextAdjVertex(g, v, w);
	}
}

//深度优先遍历
void traverserGraph_1(AdjMatrix g)
{int i;
	for ( i = 0; i < g.vernum; i++)
		visited[i] = 0;
	for ( i = 0; i < g.vernum; i++)
	if (!visited[i])deathFirstSearch(g, i);
}

void breadthFirstSearch(AdjMatrix g, int v)
{
	//访问数据
	printf("%c ", g.vertexData[v]);
	//在已访问数组中修改值
	visited[v] = 1;
	//初始化空队
	LinkQueue queue, *q = &queue;
	initLinkQueue(q);
	//v进队
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

//广度优先遍历
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
	printf("请输入顶点数目：");
	scanf("%d", &count);
	createDN(g, count);

	printf("深度优先遍历：\n");
	traverserGraph_1(adjMatrix);

	printf("\n广度优先遍历：\n");
	traverserGraph_2(adjMatrix);

	return 0;
}