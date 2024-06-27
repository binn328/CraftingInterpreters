#include <stdio.h>
#include <stdlib.h>

typedef struct node {
	struct node* prev;
	struct node* next;
	int value;
};
// 헤드 노드 생성 및 초기화
void init(struct node* head) {
	head->next = head;
	head->prev = head;
	head->value = 0;
}
// 노드 삽입
void put(struct node* current, int value) {
	struct node* new = (struct node*)malloc(sizeof(struct node));
	// 새 노드의 next는 기존 노드의 다음을 가리키고, 
	// prev는 현재 노드를 가리킨다.
	new->next = current->next;
	new->prev = current;
	new->value = value;

	// current의 next는 new를 가리킨다.
	current->next = new;
}
// 노드 삭제
void delete(struct node* target) {
	struct node* prev = target->prev;
	struct node* next = target->next;
	prev->next = next;
	next->prev = prev;
	printf("\n제거된 노드값: %d\n", target->value);
	free(target);
}
// 노드 조회
void view(struct node* head) {
	printf("head");
	struct node* temp = head;
	temp = temp->next;
	while (temp != head) {
		printf(" <-> %d", temp->value);
		temp = temp->next;
	}
}
int main() {
	/*char* p_text = (char*)malloc(sizeof(char) * 40);

	printf("텍스트를 입력");
	scanf_s("%s", p_text, 40);
	printf("\n입력받은 텍스트는: %s", p_text);
	free(p_text);*/

	struct node* head = (struct node*)malloc(sizeof(struct node));
	init(head);
	put(head, 1);
	put(head->next, 2);
	put(head->next->next, 3);
	put(head, 4);
	view(head);
	delete(head->next);
	view(head);
	return 0;
}
