#include<stdio.h>
#include<stdlib.h>
#include<time.h>


typedef struct stu
{
	int score;
}grade;

//1.录入数据
void Input(grade r[],int n)
{
	int i;
	srand((unsigned)time(NULL));  //设置种子
	printf("\n");
	for(i = 1;i <= n;i++){
		r[i].score = rand() % 91 + 10;
 		printf("%d ",r[i].score );//产生10--100之间的数
		if( i %10 == 0 )
			printf("\n");
	}
}

//录入数据保存在数组b中
void FirstData(grade b[],grade r[],int n)
{
	int i;
	for(i = 1;i <= n;i++){
		b[i].score = r[i].score ;
	}
}

//2.直接插入类排序
int move,compare,m,c;    //move是移动变量，compare是比较次数变量
void Insort(grade r[],int n)//r是结构体数组，n 是待排序数组长度
{
	int j,i;
	for(i = 2;i <= n;i++){
		r[0] = r[i];
		move ++;      //移动
		j = i-1;
		while(r[0].score >= r[j].score && j >= 0){
			compare++; //比较
			j--;
		}
		j = i - 1;
		while(r[0].score < r[j].score){
			compare++;//比较
			r[j+1].score = r[j].score;
			move++;  //移动
			j--;
		}
		r[j+1].score = r[0].score ;
		move ++;    //移动
	}
	m = move;
	c = compare;
}

//3.折半插入排序
int move1,compare1,m1,c1;      //move1是移动变量，compare1是比较次数变量
void Binsort(grade r[],int n)//r是结构体数组，n是待排序数组长度
{
	int i,low,high,mid,x,j;
	for(i = 2;i <= n; i++){
		x = r[i].score ;
		move1 ++;   //移动
		low = 1;
		high = i -1;
		while(low <= high){
			mid = (low + high)/2;
			if(x < r[mid].score ){
				high = mid -1;
				compare1++;  //比较
			}
			else{
				compare1++;  //比较
				low = mid + 1;
			}
		}
		for(j = i - 1;j >= low; j--){
			r[j+1].score = r[j].score ;
			move1++;     //移动
		}
		r[low].score = x;
		move1++;     //移动
	}
	m1 = move1;
	c1 = compare1;
}

//4.冒泡排序
int move2 ,compare2 ,m2,c2; //move2是移动变量，compare2是比较次数变量
void BubbleSort(grade r[],int n)//r 是结构体数组，n是数组长度
{
	int i,j,x,change = 1;
	for( i = 1; i <= n -1 && change; i++ ){
		change = 0;
		for( j = 1; j <= n-1;j++ ){
			if(r[j].score > r[j + 1].score ){
				compare2 ++;//比较
				x = r[j].score ;
				r[j].score = r[j + 1].score ;
				r[j + 1].score  = x;
				change =1 ;
				move2 = move2 + 3;
			}
			else{
				compare2++; //比较
			}
		}
	}
	c2 = compare2;
	m2 = move2;
} 

//5.快速排序
int move3,compare3,m3,c3;
int QKPass(grade r[],int low,int high)
{
	int x;
	x = r[low].score ;
	move3++;    //移动
	while ( low < high ){
		while (low < high && r[high].score >= x){
			high--;
			compare3++;//比较
		}
		if( low < high ){
			r[low].score = r[high].score ;
			low++;
			move3++;     //移动
		}
		while (low < high && r[low].score < x){
			low++;
			compare3++;   //比较
		}
		if(low < high){
			r[high].score = r[low].score ;
			high--;
			move3++;     //移动
		}
	}
	r[low].score = x;
	move3++;     //移动
	m3 = move3;
	c3 = compare3;
	return low;
}
void QKSort(grade r[],int low,int high)
{
	int pos;
	if(low < high){
		pos = QKPass(r,low,high); //调用一趟快速排序，以枢轴元素为界划分两个子表
		QKSort(r,low,pos - 1);   //对左子部表快速排序
		QKSort(r,pos+1,high);   //对右子部表快速排序
	}
}

//6.希尔排序
//r 是结构体数组，length 是数组长度，delta为步长,数组a是暂存移动次数，与比较次数
int move4,compare4,m4,c4;
void ShellInsert(grade r[],int length,int delta)
{
	int i, j;
	for( i = 1 + delta ; i <= length ; i++ ){
		if( r[i].score < r[i-delta].score ){
			compare4++;//比较
			r[0] = r[i];
			move4++;//移动
			for(j = i - delta ; j > 0 && r[0].score < r[j].score ; j-=delta){
				r[j + delta] = r[j];
				move4++;//移动
			}
			r[j + delta] = r[0];
			move4++;    //移动
		}
		else compare4++;//比较
	}
	m4 = move4;
	c4 = compare4;
}
// r 是结构体数组，length 是r数组长度，delta为步长数组，n是delta数组长度
void ShellSort(grade r[],int length,int delta[],int n)
{
	int i;
	for(i = 0;i <= n-1;i++)
		ShellInsert(r,length,delta[i]);
	//printf("move------>%d\ncompare--->%d\n",move4,compare4);
}

//7.简单选择排序
int move5,compare5,m5,c5;
void SelectSort(grade r[],int n)//n是数组长度
{
	int i,j,t,k;
	for(i = 1; i <= n - 1;i++){
		k = i;
		for(j = i + 1;j <= n;j++){
			if(r[j].score < r[k].score ){
				k = j;
				compare5++;    //移动
			}
			else compare5++;   //移动
		}
		if( k != i){
			t = r[i].score ;
			r[i].score = r[k].score ;
			r[k].score = t;
			move5 = move5 + 3;   //移动
		}
	}
	m5 = move5;
	c5 = compare5;
}

//8.堆排序
//重建堆
int move6,compare6,c6,m6;
void sift(grade r[],int k,int m)//k是根节点下标，m是最后一个节点下标
{
	int t,i,j,finished;
	t = r[k].score  ; //暂存根数值
	move6++;  //移动
	i = k;
	j = 2 * i; //左孩子下标
	finished = 0;
	while((j <= m ) && ( finished == 0)){
		if((j + 1 <= m) && (r[j].score < r[j + 1].score )){
			j++;                    //若存在右子树，且右子树跟的关键字大，则沿着右子树筛选
			compare6++;  //比较
		}
		if(r[j].score >= r[j + 1].score )
			compare6++;  //比较
		if( t >= r[j].score ){
			finished = 1;
			compare6++;  //比较
		}
		else{
			compare6++;  //比较
			r[i].score  = r[j].score  ;
			move6++;  //移动
			i = j;
			j = 2 * i;
		}//继续筛选
	}
	r[i].score  = t;
	move6++;  //移动
	
}

//建初堆
void crt_heap(grade r[],int n)//n是数组长度
{
	int i;
	for( i = n / 2 ; i >= 1;i-- )
		sift(r,i,n);
}

//堆排序
void HeapSort(grade r[],int n)//n是数组长度
{
	int b,i;
	crt_heap(r,n);  //建初堆
	for( i = n; i >= 2;i--){
		b = r[1].score  ;
		r[1].score  = r[i].score  ;
		r[i].score   = b;     //上面三行将堆顶元素与堆尾元素进行交换
		move6 = move6 + 3;    //移动
		sift(r,1,i - 1);     // 重建堆
	}
	c6 = compare6;
	m6 = move6;
}

//显示原始结果
void print(grade r[],int length)
{
	int i;
	printf("\n输出原始数据:\n");
	for(i = 1;i <= length;i++){
		printf("%d ",r[i].score );
		if( i %10 == 0 )
			printf("\n");
	}
	printf("\n");
}

//菜单函数
void menu() 
{
	printf("\n*******************排序基本操作************************\n\n");
	printf("           1.录入数据\n");
	printf("           2.各种排序比较次数与移动次数\n");
	printf("           3.显示原始数据\n");
	printf("           4.退出\n");
	printf("\n********************************************************\n");
}

int main()
{
	grade r[2000],b[2000];
	int n,low=1,high;
	int z,a,t = 1,delta[3] = { 5,3,1 };
	menu();
	printf("\n输入操作步骤: ");
	scanf("%d",&z);
	while(t){
		switch(z)
		{
		case 1:
			printf("\n输入数据的个数:");
			scanf("%6d",&n);
			high = n;  //数组长度上面会用到
			Input(r,n);
			break;
		case 2:
			//直接插入排序
			FirstData(b,r,n);   //录入数据保存在b中
			Insort(b,n);  

			//折半插入排序
			FirstData(b,r,n);
			Binsort(b,n);   //传参传最开始输入的数据，不要传排序后的数。
			
			//冒泡排序
			FirstData(b,r,n);
			BubbleSort(b,n);
			
			//快速排序
			FirstData(b,r,n);
			QKSort(b,low,high);
			
			//希尔排序
			FirstData(b,r,n);
			ShellSort(b,n,delta,3);
	        
			//简单选择排序
			FirstData(b,r,n);
			SelectSort(b,n);
	
			//堆排序
			FirstData(b,r,n);
			HeapSort(b,n);

			printf("\n\n\t\t直插\t 折半\t 冒泡\t 快速\t 希尔\t 选择\t 堆\t\n\n");
			printf("   move\t\t%d\t %d\t %d\t %d\t %d\t %d\t %d\t \n",m,m1,m2,m3,m4,m5,m6);
			printf("compare\t\t%d\t %d\t %d\t %d\t %d\t %d\t %d\t \n",c,c1,c2,c3,c4,c5,c6);
			break;
		case 3:
			FirstData(b,r,n);
			print(r,n);
			break;
		case 4:
			exit(0);
		default :
			printf("\n输入有误,重新输入\n");
			break;
		}
		printf("\n\n1.是 2.否\n");
		scanf("%d",&a);
		if(a == 1){
			move = move1 = move2 = move3 = move4 =move5 = move6 = 0; //初始化，可以用一个全局变量
			compare = compare1 = compare2 = compare3 = compare4 = compare5 = compare6 = 0;
			t = 1;
			system("cls");
			menu();
			printf("输入操作步骤: ");
			scanf("%d",&z);
		}
		if(a == 2)
		{
			t = 0;
			exit(0);
		}
		if((a != 1) && (a != 2))
			printf("输出有误，重新输入\n");
	}
	printf("\n");
	system("pause");
	return 0;
}