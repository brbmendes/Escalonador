public class Fila {
    private Node head;

    public Fila(){
        head = null;
    }
    
    /**
     * Imprime a fila
     */
    public void print( )  {
        System.out.print( "[\n" );
        Node no = head;
        while ( no!= null ) {
            System.out.print("\t" + no.proc.toString() + "\n" );
            no = no.next;
        }
        System.out.println( "]" );
    }
    
    /**
     * Retorna se a fila est� vazia ou n�o.
     * @return true, se a fila est� vazia. false caso contr�rio.
     */
    public boolean isEmpty(){
        return head == null;
    }
    
    /**
     * Inser��o ordenada conforme o par�metro de compara��o definido na chamada do m�todo
     * @param proc Processo
     * @param parametroComparacao "prioridade" para ordenar por prioridade do processo ou "chegada" para ordenar pela ordem de chegada do processo.
     */
    public void insertionSort(Processo proc, String parametroComparacao){
    	Node no;
    	Node anterior;
    	Node proximo;
    	
    	switch(parametroComparacao) {
    	case "prioridade":
    		no = new Node(proc);
            if( head == null ){
                head = no;
                return;
            }
            
            proximo = head;
            if( no.proc.prioridade < proximo.proc.prioridade ){
                no.next = proximo;
                head = no;
                return;
            }
            
            proximo = proximo.next;
            anterior = head;
            
            while( proximo != null) {
                if( no.proc.prioridade < proximo.proc.prioridade ){ break; }
                else { proximo = proximo.next; anterior = anterior.next; }
            }
            if( no.proc.prioridade > anterior.proc.prioridade && proximo == null){ anterior.next = no; } 
            else { no.next = anterior.next; anterior.next = no; }
    		break;
    	
    	case "chegada":
    		no = new Node(proc);
            if( head == null ){
                head = no;
                return;
            }
            
            proximo = head;
            if( no.proc.tempoChegada < proximo.proc.tempoChegada ){
                no.next = proximo;
                head = no;
                return;
            }
            
            proximo = proximo.next;
            anterior = head;
            
            while( proximo != null) {
                if( no.proc.tempoChegada < proximo.proc.tempoChegada ){ break; }
                else { proximo = proximo.next; anterior = anterior.next; }
            }
            if( no.proc.tempoChegada > anterior.proc.tempoChegada && proximo == null){ anterior.next = no; } 
            else { no.next = anterior.next; anterior.next = no; }
    		break;
    	}
    }
    
    /**
     * Inser��o no final da fila.
     * @param proc Processo
     */
    public void add(Processo proc) {
    	Node no = new Node(proc);
        if( head == null ){
            head = no;
            return;
        }
        
        Node proximo = head;
        while(proximo.next != null) {
        	proximo = proximo.next;
        }
        
        proximo.next = no;
        
    }
    
    /**
     * Remove o primeiro elemento da fila.
     */
    public void removeFirst() {
        if (isEmpty()) {
            //System.out.println("Fila vazia");
        } else {
            head = head.next;
        }
    }
    
    /**
     * Remove o processo da fila na posi��o Index.
     * @param index Posi��o para remo��o do processo.
     */
    public void removeIndex(int index) {
    	if (isEmpty()) {
            
        } else if(this.size() == 1){
        	head = null;
        } else {
        	Node proximo = head;
        	Node anterior = head;
        	for(int i = 0 ; i < this.size() ; i++) {
        		if(i != index && proximo.next == null) {
        			return;
        			//System.out.println("N�o encontrado");
        		} else if(i != index && proximo.next != null) {
        			anterior = proximo;
        			proximo = proximo.next;
        		} else {
        			if(proximo.next == null) {
        				anterior.next = null;
        			} else {
        				anterior.next = proximo.next;
        				proximo.next = null;
        			}
        		}
        		
        	}
        }
    }
    
    /**
     * Retorna o primeiro processo da fila.
     * @return Processo na primeira posi��o da fila.
     */
    public Processo getFirst() {
        if (isEmpty()) {
            return null; 
        } else {
        	return head.proc;
        }
    }
    
    /**
     * Retorna o processo na posi��o index da fila.
     * @param index Posi��o do processo na fila.
     * @return Retorna o processo na posi��o index
     */
    public Processo getIndex(int index) {
        if (isEmpty()) {
            return null; 
        } else {
        	Node proximo = head;
        	for(int i = 0 ; i < this.size(); i++) {
        		if(i == index) {
        			return proximo.proc;
        		} else {
        			proximo  = proximo.next;
        		}
        	}
        	return null;
        }
    }

    /**
     * Retorna o tamanho da fila.
     * @return
     */
    public int size(){
        return size( head );
      }
    
    /**
     * M�todo recursivo para calcular o tamanho da fila.
     * @param aux processo
     * @return retorna o tamanho da fila.
     */
    private int size( Node aux ){
      if( aux == null )
        return 0;
      return 1 + size( aux.next );
    } 
}
