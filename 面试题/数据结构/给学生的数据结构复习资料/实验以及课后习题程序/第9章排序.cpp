#include<stdio.h>
#include<stdlib.h>
#include<time.h>


typedef struct stu
{
	int score;
}grade;

//1.¼������
void Input(grade r[],int n)
{
	int i;
	srand((unsigned)time(NULL));  //��������
	printf("\n");
	for(i = 1;i <= n;i++){
		r[i].score = rand() % 91 + 10;
 		printf("%d ",r[i].score );//����10--100֮�����
		if( i %10 == 0 )
			printf("\n");
	}
}

//¼�����ݱ���������b��
void FirstData(grade b[],grade r[],int n)
{
	int i;
	for(i = 1;i <= n;i++){
		b[i].score = r[i].score ;
	}
}

//2.ֱ�Ӳ���������
int move,compare,m,c;    //move���ƶ�������compare�ǱȽϴ�������
void Insort(grade r[],int n)//r�ǽṹ�����飬n �Ǵ��������鳤��
{
	int j,i;
	for(i = 2;i <= n;i++){
		r[0] = r[i];
		move ++;      //�ƶ�
		j = i-1;
		while(r[0].score >= r[j].score && j >= 0){
			compare++; //�Ƚ�
			j--;
		}
		j = i - 1;
		while(r[0].score < r[j].score){
			compare++;//�Ƚ�
			r[j+1].score = r[j].score;
			move++;  //�ƶ�
			j--;
		}
		r[j+1].score = r[0].score ;
		move ++;    //�ƶ�
	}
	m = move;
	c = compare;
}

//3.�۰��������
int move1,compare1,m1,c1;      //move1���ƶ�������compare1�ǱȽϴ�������
void Binsort(grade r[],int n)//r�ǽṹ�����飬n�Ǵ��������鳤��
{
	int i,low,high,mid,x,j;
	for(i = 2;i <= n; i++){
		x = r[i].score ;
		move1 ++;   //�ƶ�
		low = 1;
		high = i -1;
		while(low <= high){
			mid = (low + high)/2;
			if(x < r[mid].score ){
				high = mid -1;
				compare1++;  //�Ƚ�
			}
			else{
				compare1++;  //�Ƚ�
				low = mid + 1;
			}
		}
		for(j = i - 1;j >= low; j--){
			r[j+1].score = r[j].score ;
			move1++;     //�ƶ�
		}
		r[low].score = x;
		move1++;     //�ƶ�
	}
	m1 = move1;
	c1 = compare1;
}

//4.ð������
int move2 ,compare2 ,m2,c2; //move2���ƶ�������compare2�ǱȽϴ�������
void BubbleSort(grade r[],int n)//r �ǽṹ�����飬n�����鳤��
{
	int i,j,x,change = 1;
	for( i = 1; i <= n -1 && change; i++ ){
		change = 0;
		for( j = 1; j <= n-1;j++ ){
			if(r[j].score > r[j + 1].score ){
				compare2 ++;//�Ƚ�
				x = r[j].score ;
				r[j].score = r[j + 1].score ;
				r[j + 1].score  = x;
				change =1 ;
				move2 = move2 + 3;
			}
			else{
				compare2++; //�Ƚ�
			}
		}
	}
	c2 = compare2;
	m2 = move2;
} 

//5.��������
int move3,compare3,m3,c3;
int QKPass(grade r[],int low,int high)
{
	int x;
	x = r[low].score ;
	move3++;    //�ƶ�
	while ( low < high ){
		while (low < high && r[high].score >= x){
			high--;
			compare3++;//�Ƚ�
		}
		if( low < high ){
			r[low].score = r[high].score ;
			low++;
			move3++;     //�ƶ�
		}
		while (low < high && r[low].score < x){
			low++;
			compare3++;   //�Ƚ�
		}
		if(low < high){
			r[high].score = r[low].score ;
			high--;
			move3++;     //�ƶ�
		}
	}
	r[low].score = x;
	move3++;     //�ƶ�
	m3 = move3;
	c3 = compare3;
	return low;
}
void QKSort(grade r[],int low,int high)
{
	int pos;
	if(low < high){
		pos = QKPass(r,low,high); //����һ�˿�������������Ԫ��Ϊ�绮�������ӱ�
		QKSort(r,low,pos - 1);   //�����Ӳ����������
		QKSort(r,pos+1,high);   //�����Ӳ����������
	}
}

//6.ϣ������
//r �ǽṹ�����飬length �����鳤�ȣ�deltaΪ����,����a���ݴ��ƶ���������Ƚϴ���
int move4,compare4,m4,c4;
void ShellInsert(grade r[],int length,int delta)
{
	int i, j;
	for( i = 1 + delta ; i <= length ; i++ ){
		if( r[i].score < r[i-delta].score ){
			compare4++;//�Ƚ�
			r[0] = r[i];
			move4++;//�ƶ�
			for(j = i - delta ; j > 0 && r[0].score < r[j].score ; j-=delta){
				r[j + delta] = r[j];
				move4++;//�ƶ�
			}
			r[j + delta] = r[0];
			move4++;    //�ƶ�
		}
		else compare4++;//�Ƚ�
	}
	m4 = move4;
	c4 = compare4;
}
// r �ǽṹ�����飬length ��r���鳤�ȣ�deltaΪ�������飬n��delta���鳤��
void ShellSort(grade r[],int length,int delta[],int n)
{
	int i;
	for(i = 0;i <= n-1;i++)
		ShellInsert(r,length,delta[i]);
	//printf("move------>%d\ncompare--->%d\n",move4,compare4);
}

//7.��ѡ������
int move5,compare5,m5,c5;
void SelectSort(grade r[],int n)//n�����鳤��
{
	int i,j,t,k;
	for(i = 1; i <= n - 1;i++){
		k = i;
		for(j = i + 1;j <= n;j++){
			if(r[j].score < r[k].score ){
				k = j;
				compare5++;    //�ƶ�
			}
			else compare5++;   //�ƶ�
		}
		if( k != i){
			t = r[i].score ;
			r[i].score = r[k].score ;
			r[k].score = t;
			move5 = move5 + 3;   //�ƶ�
		}
	}
	m5 = move5;
	c5 = compare5;
}

//8.������
//�ؽ���
int move6,compare6,c6,m6;
void sift(grade r[],int k,int m)//k�Ǹ��ڵ��±꣬m�����һ���ڵ��±�
{
	int t,i,j,finished;
	t = r[k].score  ; //�ݴ����ֵ
	move6++;  //�ƶ�
	i = k;
	j = 2 * i; //�����±�
	finished = 0;
	while((j <= m ) && ( finished == 0)){
		if((j + 1 <= m) && (r[j].score < r[j + 1].score )){
			j++;                    //�������������������������Ĺؼ��ִ�������������ɸѡ
			compare6++;  //�Ƚ�
		}
		if(r[j].score >= r[j + 1].score )
			compare6++;  //�Ƚ�
		if( t >= r[j].score ){
			finished = 1;
			compare6++;  //�Ƚ�
		}
		else{
			compare6++;  //�Ƚ�
			r[i].score  = r[j].score  ;
			move6++;  //�ƶ�
			i = j;
			j = 2 * i;
		}//����ɸѡ
	}
	r[i].score  = t;
	move6++;  //�ƶ�
	
}

//������
void crt_heap(grade r[],int n)//n�����鳤��
{
	int i;
	for( i = n / 2 ; i >= 1;i-- )
		sift(r,i,n);
}

//������
void HeapSort(grade r[],int n)//n�����鳤��
{
	int b,i;
	crt_heap(r,n);  //������
	for( i = n; i >= 2;i--){
		b = r[1].score  ;
		r[1].score  = r[i].score  ;
		r[i].score   = b;     //�������н��Ѷ�Ԫ�����βԪ�ؽ��н���
		move6 = move6 + 3;    //�ƶ�
		sift(r,1,i - 1);     // �ؽ���
	}
	c6 = compare6;
	m6 = move6;
}

//��ʾԭʼ���
void print(grade r[],int length)
{
	int i;
	printf("\n���ԭʼ����:\n");
	for(i = 1;i <= length;i++){
		printf("%d ",r[i].score );
		if( i %10 == 0 )
			printf("\n");
	}
	printf("\n");
}

//�˵�����
void menu() 
{
	printf("\n*******************�����������************************\n\n");
	printf("           1.¼������\n");
	printf("           2.��������Ƚϴ������ƶ�����\n");
	printf("           3.��ʾԭʼ����\n");
	printf("           4.�˳�\n");
	printf("\n********************************************************\n");
}

int main()
{
	grade r[2000],b[2000];
	int n,low=1,high;
	int z,a,t = 1,delta[3] = { 5,3,1 };
	menu();
	printf("\n�����������: ");
	scanf("%d",&z);
	while(t){
		switch(z)
		{
		case 1:
			printf("\n�������ݵĸ���:");
			scanf("%6d",&n);
			high = n;  //���鳤��������õ�
			Input(r,n);
			break;
		case 2:
			//ֱ�Ӳ�������
			FirstData(b,r,n);   //¼�����ݱ�����b��
			Insort(b,n);  

			//�۰��������
			FirstData(b,r,n);
			Binsort(b,n);   //���δ��ʼ��������ݣ���Ҫ������������
			
			//ð������
			FirstData(b,r,n);
			BubbleSort(b,n);
			
			//��������
			FirstData(b,r,n);
			QKSort(b,low,high);
			
			//ϣ������
			FirstData(b,r,n);
			ShellSort(b,n,delta,3);
	        
			//��ѡ������
			FirstData(b,r,n);
			SelectSort(b,n);
	
			//������
			FirstData(b,r,n);
			HeapSort(b,n);

			printf("\n\n\t\tֱ��\t �۰�\t ð��\t ����\t ϣ��\t ѡ��\t ��\t\n\n");
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
			printf("\n��������,��������\n");
			break;
		}
		printf("\n\n1.�� 2.��\n");
		scanf("%d",&a);
		if(a == 1){
			move = move1 = move2 = move3 = move4 =move5 = move6 = 0; //��ʼ����������һ��ȫ�ֱ���
			compare = compare1 = compare2 = compare3 = compare4 = compare5 = compare6 = 0;
			t = 1;
			system("cls");
			menu();
			printf("�����������: ");
			scanf("%d",&z);
		}
		if(a == 2)
		{
			t = 0;
			exit(0);
		}
		if((a != 1) && (a != 2))
			printf("���������������\n");
	}
	printf("\n");
	system("pause");
	return 0;
}