import java.util.ArrayList;

public class Processo {
	/**
	 * Nome do processo
	 */
	String nome;
	
	/**
	 * Tempo que o processo chega no processador
	 */
	int tempoChegada;
	
	/**
	 * Tempo de execução do processo
	 */
	int tempoExecucao;
	
	/**
	 * Prioridade do processo
	 */
	int prioridade;
	
	/**
	 * Array contendo os tempos de operações de IO do processo
	 */
	ArrayList<Integer> operacaoIO;
	
	/**
	 * Tempo de execução restante do processo
	 */
	int tempoExecucaoRestante;
	
	/**
	 * Tempo de resposta médio do processo
	 */
	float tempoMedioResposta;
	
	/**
	 * Tempo de espera médio do processo
	 */
	float tempoMedioEspera;
	
	/**
	 * Tempo total que o processo fica no processador, contando todas as operações
	 */
	int turnaround;
	
	/**
	 * Tempo corrente da operação de IO
	 */
	int tempoIOCorrente;
	
	/**
	 * Conta quantas vezes o processo foi executado. Vai de 0 até a fatia de tempo
	 */
	int contadorExecucao;
	
	/**
	 * Controla se o processo já foi iniciado pelo processador ou não
	 */
	boolean processojaIniciado;
	
	/**
	 * Instancia um processo
	 * @param nome Nome do processo
	 * @param tempoChegada Tempo de chegada do processo
	 * @param tempoExecucao Tempo de execução do processo
	 * @param prioridade Prioridade do processo
	 * @param operacaoIO Lista com os instantes de IO de um processo
	 */
	public Processo(String nome, int tempoChegada, int tempoExecucao, int prioridade, ArrayList<Integer> operacaoIO) {
		this.nome = nome;
		this.tempoChegada = tempoChegada;
		this.tempoExecucao = tempoExecucao;
		this.prioridade = prioridade;
		this.operacaoIO = operacaoIO;
		this.tempoExecucaoRestante = tempoExecucao;
		this.processojaIniciado = false;
	}

	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public int getTempoChegada() {
		return tempoChegada;
	}
	
	public void setTempoChegada(int tempoChegada) {
		this.tempoChegada = tempoChegada;
	}
	
	public int getTempoExecucao() {
		return tempoExecucao;
	}
	
	public void setTempoExecucao(int tempoExecucao) {
		this.tempoExecucao = tempoExecucao;
	}
	
	public int getPrioridade() {
		return prioridade;
	}
	
	public void setPrioridade(int prioridade) {
		this.prioridade = prioridade;
	}
	
	public ArrayList<Integer> getTempoIO() {
		return operacaoIO;
	}
	
	public void setTempoIO(ArrayList<Integer> operacaoIO) {
		this.operacaoIO = operacaoIO;
	}
	
	public int getTempoExecucaoRestante() {
		return tempoExecucaoRestante;
	}
	
	public void setTempoExecucaoRestante(int tempo) {
		this.tempoExecucaoRestante += tempo;
	}

	public ArrayList<Integer> getOperacaoIO() {
		return operacaoIO;
	}

	public void setOperacaoIO(ArrayList<Integer> operacaoIO) {
		this.operacaoIO = operacaoIO;
	}

	public float getTempoMedioResposta() {
		return tempoMedioResposta;
	}

	public void setTempoMedioResposta(float tempo) {
		this.tempoMedioResposta += tempo;
	}

	public float getTempoMedioEspera() {
		return tempoMedioEspera;
	}

	public void setTempoMedioEspera(float tempo) {
		this.tempoMedioEspera += tempo;
	}

	public int getTurnaround() {
		return turnaround;
	}

	public void setTurnaround(int turnaround) {
		this.turnaround += turnaround;
	}

	public int getTempoIOCorrente() {
		return tempoIOCorrente;
	}

	public void setTempoIOCorrente(int setTempoIOCorrente) {
		this.tempoIOCorrente += setTempoIOCorrente;
	}

	public int getContadorExecucao() {
		return contadorExecucao;
	}

	public void setContadorExecucao(int contadorExecucao) {
		this.contadorExecucao += contadorExecucao;
	}

	public boolean isProcessojaIniciado() {
		return processojaIniciado;
	}

	public void setProcessojaIniciado(boolean processojaIniciado) {
		this.processojaIniciado = processojaIniciado;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ Nome: " + nome + " , ");
		sb.append("Tempo de chegada: UT " + tempoChegada + ", ");
		sb.append("Tempo de execução: " + tempoExecucao + " UT, ");
		sb.append("Tempo restante de execução: " + tempoExecucaoRestante + " UT, ");
		sb.append("Prioridade: " + prioridade + ", ");
		sb.append("Operação de IO: UT ");
		for(int item : operacaoIO) {
			sb.append(item + " ");
		}
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * Imprime as métricas do processo
	 * @return String contendo as métricas do processo
	 */
	public String getMetricas() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ Nome: " + nome + " , ");
		sb.append("Turnaround: " + turnaround + " UT , ");
		sb.append("Tempo de resposta médio: " + tempoMedioResposta + " UT, ");
		sb.append("Tempo de espera médio: " + tempoMedioEspera + " UT }");
		return sb.toString();
	}
	
	
}
