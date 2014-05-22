/*
 * A dequeue
 * Does as a dequeue do
 *
 * Dan Collins 1183446
 * Severin Mahoney-Marsh 1181754
 */
public class Dequeue<T>{
	// Node for double linked deque
	private class Node<T>{
		T data;
		Node<T> left;
		Node<T> right;

		public Node(T data){
			this.data = data;
		}

		public T getData(){
			return data;
		}

		public void setLeft(Node<T> left){
			this.left = left;
		}

		public Node<T> getLeft(){
			return left;
		}

	public void setRight(Node<T> right){
			this.right = right;
		}

		public Node<T> getRight(){
			return right;
		}
	}
	Node<T> head;
	Node<T> tail;
	
	/*
	 * Appends data to tail
	 */
	public void push(T data){
		// Handle edge case with no data;
		if (tail == null){
			tail = new Node<T>(data);
			head = tail;
		} else {
			tail.setRight(new Node<T>(data));
			tail = tail.getRight();
		}
	}

	/*
	 * Appends data to head
	 */
	public void unshift(T data){
		// Handle edge case with no data
		if (head == null){
			head = new Node<T>(data);
			tail = head;
		} else {
			head.setLeft(new Node<T>(data));
			head = head.getLeft();
		}
	}

	/*
	 * Removes from tail
	 */
	public T pop(){
		// Is it empty?
		if (tail == null){
			throw new RuntimeException("The dequeue is empty.");
		} else {
			T value = tail.getData();
			tail = tail.getLeft();
			// Any data remaining?
			if (tail == null){
				head = null;
			} else {
				tail.setRight(null);
			}
			return value;
		}
	}

	/*
	 * Removes from head
	 */
	public T shift(){
		// Is it empty?
		if (head == null){
			throw new RuntimeException("The dequeue is empty.");
		} else {
			T value = head.getData();
			head = head.getRight();
			// Any data remaining?
			if (head == null){
				tail = null;
			} else {
				head.setLeft(null);
			}
			return value;
		}
	}
}
