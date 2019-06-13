public class Node {
	Node next;
    Processo proc;

    Node (Processo novoProcesso){
    	proc = novoProcesso;
        next = null;
    }
}
