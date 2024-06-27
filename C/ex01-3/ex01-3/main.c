#include <stdio.h>
#include <stdlib.h>

typedef struct node {
	struct node* prev;
	struct node* next;
	int value;
};
// ��� ��� ���� �� �ʱ�ȭ
void init(struct node* head) {
	head->next = head;
	head->prev = head;
	head->value = 0;
}
// ��� ����
void put(struct node* current, int value) {
	struct node* new = (struct node*)malloc(sizeof(struct node));
	// �� ����� next�� ���� ����� ������ ����Ű��, 
	// prev�� ���� ��带 ����Ų��.
	new->next = current->next;
	new->prev = current;
	new->value = value;

	// current�� next�� new�� ����Ų��.
	current->next = new;
}
// ��� ����
void delete(struct node* target) {
	struct node* prev = target->prev;
	struct node* next = target->next;
	prev->next = next;
	next->prev = prev;
	printf("\n���ŵ� ��尪: %d\n", target->value);
	free(target);
}
// ��� ��ȸ
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

	printf("�ؽ�Ʈ�� �Է�");
	scanf_s("%s", p_text, 40);
	printf("\n�Է¹��� �ؽ�Ʈ��: %s", p_text);
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
