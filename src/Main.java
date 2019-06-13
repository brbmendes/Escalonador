import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


/* 
 Alunos: Bruno Mendes e Ezequiel Rinco
 Data: 30/09/2018
 Descrição do problema:
 
Implementar o algoritmo preemptivo Round Robin com prioridade.

Dado um arquivo de entrada com seguintes informações, nesta ordem: 
	número de processos, 
	tamanho de fatia de tempo, e para cada processo, 
	tempo de chegada,
	tempo de execução, 
	prioridade (1 até 9 - prioridade 1 é a melhor) e 
	tempos de acesso a operações de E/S (tempo correspondente a sua execução).

Imprimir os tempos médios de resposta e espera para o algoritmo supra-citado.
	
Incluir também o gráfico de saída do processador.


Exemplo de arquivo de entrada:

5
3
3 10 2
5 12 1
9 15 2
11 15 1
12 8 5 2

Exemplo de gráfico a ser exibido para o exemplo acima:

---C1C222C222C444C222C444C222C444C444C444C11C333C111C333C111C333C1C333C333C55C----C5C555C55
	*/

public class Main {

	public static void main(String[] args) {
		
		String nomeArquivo = "EntradaTeste5.txt";
		
		final int fatiaTempo = ObterFatiaTempo(nomeArquivo);

		Fila listaProcessos = ObterListaProcessos(nomeArquivo);
		int numeroProcessos = listaProcessos.size();
		
		Processador processador = new Processador(listaProcessos, numeroProcessos, fatiaTempo);
		processador.Escalonar();

	}

	public static int ObterFatiaTempo(String nomeArquivo){
			
		int fatiaTempo = 0;
		try {
	    	
			FileReader arq = new FileReader(nomeArquivo);
		    BufferedReader lerArq = new BufferedReader(arq);
		 
		    String linha = lerArq.readLine();	      
		    linha = lerArq.readLine();
			    
		    fatiaTempo = Integer.parseInt(linha);
		    arq.close();
			    
		    } catch (IOException e) {
		    	System.err.printf("Erro na abertura do arquivo: %s.\n",
		          e.getMessage());
		    	System.exit(1);
		    }
		return fatiaTempo;
	}
		
	public static Fila ObterListaProcessos(String nomeArquivo){
		
		Fila listaProcessos = new Fila();
		try {
	    	
			FileReader arq = new FileReader(nomeArquivo);
		    BufferedReader lerArq = new BufferedReader(arq);
		 
		    String linha = lerArq.readLine(); // leitura do número de processos
		    int numProcessos = Integer.parseInt(linha);
		      
		    linha = lerArq.readLine(); // Leitura da Fatia de tempo
		    
		    linha = lerArq.readLine(); // leitura da primeira linha
	
		    int contadorNomeProcesso = 1;
		      
		    while (contadorNomeProcesso <= numProcessos) {
		    	  
		    String[] parametrosProcesso = linha.split(" ");
		    ArrayList<Integer> operacaoIO = new ArrayList<>();
		    
		    int tempoChegada = Integer.parseInt(parametrosProcesso[0]);
		    int tempoExecucao = Integer.parseInt(parametrosProcesso[1]);
		    int prioridade = Integer.parseInt(parametrosProcesso[2]);
		    int semOperacaoIO = -1;
		    if(parametrosProcesso.length == 3) {
		    	operacaoIO.add(semOperacaoIO);
		    } else {
		    	for(int i = 3; i < parametrosProcesso.length ; i++) {
		    		int op = Integer.parseInt(parametrosProcesso[i]);
		    		operacaoIO.add(op);
		    	}
		    }
		    	
		    Processo proc = new Processo(String.valueOf(contadorNomeProcesso) , tempoChegada,tempoExecucao,prioridade,operacaoIO);
		        
		    listaProcessos.insertionSort(proc, "chegada");
		    contadorNomeProcesso++;
		    linha = lerArq.readLine();
		    }
		 
		    arq.close();
		    } catch (IOException e) {
		    	System.err.printf("Erro na abertura do arquivo: %s.\n",
		          e.getMessage());
		    	System.exit(1);
		    }
		return listaProcessos;
	}
}
