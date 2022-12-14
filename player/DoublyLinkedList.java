package player;

public class DoublyLinkedList
{
    private DoublyLinkedNode head;

    public DoublyLinkedList()
    {
        this.head=new DoublyLinkedNode(null);
        this.head.setNext(head);
        this.head.setPrev(head);
    }

    public void addNode(DoublyLinkedNode node)
    {
        if(node.getItem()==null) return;
        node.setNext(this.head.getNext());
        node.setPrev(this.head);
        this.head.getNext().setPrev(node);
        this.head.setNext(node);
    }

    static void delNode(DoublyLinkedNode node)
    {
        if(node.getItem()==null) return;
        node.getPrev().setNext(node.getNext());
        node.getNext().setPrev(node.getPrev());
        node.setNext(null);
        node.setPrev(null);
    }

    public boolean isEmpty()
    {
        if(this.head.getNext()==this.head) return true;
        return false;
    }
}