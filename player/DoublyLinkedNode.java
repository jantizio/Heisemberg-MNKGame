package player;

public class DoublyLinkedNode {
    private CellEnhanced item;
    private DoublyLinkedNode next, prev;

    /**
     * Constructor.
     * 
     * @param item item.
     */
    public DoublyLinkedNode(CellEnhanced item) {
        this.item = item;
    }

    public boolean isSentinella() {
        if (this.item == null)
            return true;
        return false;
    }

    public CellEnhanced getItem() {
        return this.item;
    }

    public DoublyLinkedNode getNext() {
        return this.next;
    }

    public void setNext(DoublyLinkedNode next) {
        this.next = next;
    }

    public DoublyLinkedNode getPrev() {
        return this.prev;
    }

    public void setPrev(DoublyLinkedNode prev) {
        this.prev = prev;
    }
}
