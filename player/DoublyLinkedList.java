package player;

public class DoublyLinkedList {
    private DoublyLinkedNode head;
    private DoublyLinkedNode iterationStop;

    public DoublyLinkedList() {
        this.head = new DoublyLinkedNode(null);
        this.head.setNext(head);
        this.head.setPrev(head);
        this.iterationStop = null;
    }

    public void addNode(DoublyLinkedNode node) {
        if (node.getItem() == null)
            return;
        node.setNext(this.head.getNext());
        node.setPrev(this.head);
        this.head.getNext().setPrev(node);
        this.head.setNext(node);
    }

    private void addNodeTail(DoublyLinkedNode node) {
        if (node.getItem() == null)
            return;
        node.setNext(this.head);
        node.setPrev(this.head.getPrev());
        this.head.getPrev().setNext(node);
        this.head.setPrev(node);
    }

    static void delNode(DoublyLinkedNode node) {
        if (node.getItem() == null)
            return;
        if (node.getNext() == null || node.getPrev() == null)
            return;
        node.getPrev().setNext(node.getNext());
        node.getNext().setPrev(node.getPrev());
        node.setNext(null);
        node.setPrev(null);
    }

    private void startIteration() {
        this.iterationStop = this.head.getNext();
    }

    private void endIteration() {
        this.iterationStop = null;
    }

    public DoublyLinkedNode getNextIteration(boolean isFirst) {
        if (isFirst)
            this.startIteration(); // se(prima iterazione) memorizzo il primo elemento
        else if (this.iterationStop == null)
            return null; // errore di utilizzo
        DoublyLinkedNode node = this.head.getNext(); // salvo il nodo
        DoublyLinkedList.delNode(node); // elimino il nodo
        this.addNodeTail(node); // reinserisco il nodo in coda
        if (this.iterationStop == this.head.getNext())
            this.endIteration(); // se(prossimo nodo==primo nodo) ciclo completato => dimentico il primo nodo
        return node; // ritorno il nodo
    }

    public boolean isEmpty() {
        if (this.head.getNext() == this.head)
            return true;
        return false;
    }
}