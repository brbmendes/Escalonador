
public class Processador {
	/**
	 * Tempo que leva para executar operação de IO
	 */
	final int tempoIO = 4;
	
	/**
	 * Caractere indica processador parado
	 */
	final char processadorParado = '-';
		
	/**
	 * Caractere indica processador realizando troca de contexto
	 */
	final char trocaContexto = 'C';
	
	/**
	 * Fatia de tempo que processo fica no processador
	 */
	private int fatiaTempo;
	
	/**
	 * Indicador do número de processos
	 */
	private int numeroProcessos;

	/**
	 * Fila de processos que ainda não estão disponíveis para escalonamento.
	 */
	Fila processosNaoProntos;
	
	/**
	 * Fila de processos escalonáveis, prontos para serem executados pelo processador.
	 */
	Fila processosProntosExecucao;
	
	/**
	 * Fila de processos que estão aguardando operação de IO
	 */
	Fila processosAguardandoIO;
	
	/**
	 * Fila de processos finalizados pelo processador
	 */
	Fila processosFinalizados;
	
	/**
	 * Processo que está atualmente no processador
	 */
	Processo processoExecutando;

	/**
	 * Saída do processador
	 */
	StringBuilder saidaProcessador;
	
	/**
	 * Tempo do relógio
	 */
	int tempo;
	
	/**
	 * Variável indica se está ocorrendo troca de contexto
	 */
	boolean ehTrocaContexto;
	
	/**
	 * Construtor que instancia o processador
	 * @param processosNaoProntos Fila de processos não escalonáveis ainda.
	 * @param numeroProcessos Numero de processos que serão escalonados.
	 * @param fatiaTempo Fatia de tempo (quantum) que cada processo pode permanecer no processador.
	 */
	public Processador(Fila processosNaoProntos, int numeroProcessos, int fatiaTempo) {
		this.fatiaTempo = fatiaTempo;
		this.numeroProcessos = numeroProcessos;
		this.tempo = 1;
		this.ehTrocaContexto = false;
		this.saidaProcessador = new StringBuilder();
		this.processoExecutando = null;
		
		this.processosNaoProntos = processosNaoProntos;
		this.processosProntosExecucao = new Fila();
		this.processosAguardandoIO = new Fila();
		this.processosFinalizados = new Fila();
	}
	
	/**
	 * Inicia o processo de escalonamento
	 */
	public void Escalonar() {
		// Como não houve processo no tempo 0, adiciona marcação à saída.
		adicionaSaidaProcessadorParado();
		
		// Loop principal do programa.
		// Executa enquanto a quantidade de processos finalizados for menor que o número de processos
		while(processosFinalizados.size() < numeroProcessos ) {
			
			// Método que retira da lista de processos não prontos, e move para a fila de execução
			verificaProcessosDisponiveisExecucao();
			
			// Método que verifica se processo vai para IO;
			verificaProcessoVaiParaIO();
			
			// Método que verifica se tem processo para retornar de IO
			verificaProcessoRetornaIO();

			// Se não tem ninguem no processo executando
			if(processoExecutando == null) {
				// E a processos prontos para execução não está vazia
				if(!processosProntosExecucao.isEmpty()) {
					// Coloca processo no processador para execução
					colocaProcessoEmExecucao();
				}
				// Caso tenha processo em execução
			} else {
				// Se fila de processos prontos para execução não estiver vazia, e o primeiro processo disponível para execução tiver prioridade MELHOR que o processo atual em execução
				if(!processosProntosExecucao.isEmpty() && processosProntosExecucao.getFirst().getPrioridade() < processoExecutando.getPrioridade()) {
					// Devolve processo em execução para fila de processos disponíveis
					devolveProcessoFilaDisponiveis();
					// Coloca processo no processador para execução
					colocaProcessoEmExecucao();
				// Senão 
				// verifica se o contador de execução do processo em execução chegou a zero (indica que acabou seu tempo no processador) ou se o tempo de execução do processo acabou
				} else if(processoExecutando.getContadorExecucao() == 0 || processoExecutando.getTempoExecucaoRestante() == 0) {
					// Se o tempo de execução do processo for maior do que zero, indica que ele ainda não acabou seu processamento
					if(processoExecutando.getTempoExecucaoRestante() > 0) {
						// Devolve processo em execução para fila de processos disponíveis
						devolveProcessoFilaDisponiveis();
						// Retira o processo de execução, deixando o processador livre.
						retiraProcessoExecucao();
					// Senão (indica que acabou seu processamento)
					} else {
						// Envia processo para a fila de processos finalizados
						enviaProcessoFilaFinalizados();
						// Retira o processo de execução, deixando o processador livre.
						retiraProcessoExecucao();
					}
					
					// Se a fila de processos prontos para execução não está vazia, e o número de processos finalizdos for menor que o número total de processos a serem executados
					if(!processosProntosExecucao.isEmpty() && processosFinalizados.size() < numeroProcessos) {
						// Coloca processo em execução
						colocaProcessoEmExecucao();
					// Senão
					// Se o número de processos finalizdos for menor que o número total de processos a serem executados
					} else if(processosFinalizados.size() < numeroProcessos) {
						// Realiza a troca de contexto
						realizaTrocaContexto();
					// Senão, não tem mais processos para executar
					} else {
						// Encerra o loop
						break;
					}
				}
			}
			
			// Realiza o processamento de acordo com a situação do processador (ele pode estar realizando troca de contexto, processando algum processo ou parado)
			realizaProcessamento();
			
			tempo++;
		}
		
		imprimeMetricas(processosFinalizados, tempo);
		
		imprimeSaida();
		
	}
	
	/**
	 * Método que computa as métricas do processo em execução:
	 * Decrementa o tempo restante de execução
	 * Decrementa o contador de execução (fatia de tempo do processador)
	 * Incrementa o turnaround do processo
	 * Indica que o processo já foi iniciado
	 */
	private void computaMetricasProcessoExecucao() {
		processoExecutando.setTempoExecucaoRestante(-1);
		processoExecutando.setContadorExecucao(-1);
		processoExecutando.setTurnaround(1);
		processoExecutando.setProcessojaIniciado(true);
	}
	
	/**
	 * Método que computa as métricas se tiver processo em execução:
	 * Incrementa o turnaround do processo
	 * incrementa o tempo médio de espera do processo
	 * Caso o processo não tenha sido iniciado ainda, incrementa o tempo médio de resposta
	 */
	private void computaMetricasProcessoExecucaoTrocaContexto() {
		if(processoExecutando != null) {
			processoExecutando.setTurnaround(1);
			processoExecutando.setTempoMedioEspera(1);
			if(processoExecutando.isProcessojaIniciado() == false) {
				processoExecutando.setTempoMedioResposta(1);
			}
		}
	}
	
	/**
	 * Método que computa as métricas dos processos prontos para execução:
	 * Incrementa turnaround dos processos na fila de prontos para execução
	 * Incrementa tempo médio de resposta dos processos na fila de prontos para execução
	 * Incrementa tempo médio de espera dos processos na fila de prontos para execução
	 */
	private void computaMetricasProcessosProntosExeucao() {
		processosProntosExecucao = incrementaTurnaround(processosProntosExecucao); 
		processosProntosExecucao = incrementaTempoRespostaMedio(processosProntosExecucao); 
		processosProntosExecucao = incrementaTempoEsperaMedio(processosProntosExecucao); 
	}
	
	/**
	 * Método que computa as métricas dos processos que estão aguardando IO:
	 * Decrementa o tempo corrente de IO dos processos da fila
	 * Incrementa o turnaround dos processos da fila
	 * Incrementa o tempo médio de espera dos processos da fila
	 */
	private void computaMetricasProcessosAguardandoIO() {
		if(processosAguardandoIO.size() > 0) {
			for(int i = 0 ; i < processosAguardandoIO.size(); i++) {
				processosAguardandoIO.getIndex(i).setTempoIOCorrente(-1);
				processosAguardandoIO.getIndex(i).setTurnaround(1);
				processosAguardandoIO.getIndex(i).setTempoMedioEspera(1);
			}
		}
	}
	
	/**
	 * Método que incrementa o turnaround dos procesos de uma fila de uma fila
	 * @param fila Fila que vai ter o turnaround incrementado
	 * @return Fila com turnaround incrementado
	 */
	private Fila incrementaTurnaround(Fila fila) {
		for(int i = 0 ; i < fila.size() ; i++) {
			fila.getIndex(i).setTurnaround(1);
		}
		return fila;
	}
	
	/**
	 * Método que incrementa o tempo médio de resposta dos procesos de uma fila de uma fila
	 * @param fila Fila que vai ter o tempo médio de resposta incrementado
	 * @return Fila com tempo médio de resposta incrementado
	 */
	private Fila incrementaTempoRespostaMedio(Fila fila) {
		for(int i = 0 ; i < fila.size() ; i++) {
			if(fila.getIndex(i).isProcessojaIniciado() == false) {
				fila.getIndex(i).setTempoMedioResposta(1);
			}
		}
		return fila;
	}
	
	/**
	 * Método que incrementa o tempo médio de espera dos procesos de uma fila de uma fila
	 * @param fila Fila que vai ter o tempo médio de espera incrementado
	 * @return Fila com tempo médio de espera incrementado
	 */
	private Fila incrementaTempoEsperaMedio(Fila fila) {
		for(int i = 0 ; i < fila.size() ; i++) {
			fila.getIndex(i).setTempoMedioEspera(1);
		}
		return fila;
	}
	
	/**
	 * Método que imprime saída no console.
	 */
	private void imprimeSaida() {
		System.out.println(saidaProcessador.toString());
	}
	
	/**
	 * Método que imprime as métricas de uma fila, e o tempo total de execução do processador.
	 * @param fila Fila que vai ter as métricas impressas
	 * @param tempo Tempo total de execução do processador
	 */
	private void imprimeMetricas(Fila fila, int tempo) {
		System.out.println("Tempo de execução: " + tempo);
		
		float tempoRespostaTotal = 0;
		float tempoEsperaTotal = 0;
		
		for(int i = 0 ; i < fila.size() ; i++) {
			tempoRespostaTotal += fila.getIndex(i).getTempoMedioResposta();
			tempoEsperaTotal += fila.getIndex(i).getTempoMedioEspera();
		}
		
		float tempoRespostaMedio = tempoRespostaTotal / fila.size();
		float tempoEsperaMedio = tempoEsperaTotal / fila.size();
		
		System.out.println("Tempo de resposta médio: " + tempoRespostaMedio);
		System.out.println("Tempo de espera médio: " + tempoEsperaMedio);
		System.out.println("\n");
		for(int i = 0 ; i < fila.size() ; i++) {
			System.out.println(fila.getIndex(i).getMetricas());
		}
		System.out.println("\n");
	}
	
	/**
	 * Método que verifica quais processos estão disponíveis para execução
	 * remove da fila de não disponíveis
	 * e adiciona os disponíveis na fila de processos prontos para execução. 
	 */
	private void verificaProcessosDisponiveisExecucao() {
		if(!processosNaoProntos.isEmpty()) {
			while(processosNaoProntos.getFirst().getTempoChegada() == tempo) {
				processosProntosExecucao.insertionSort(processosNaoProntos.getFirst(), "prioridade");
				processosNaoProntos.removeFirst();
				if(processosNaoProntos.isEmpty()) break;
			}
		}
	}
	
	/**
	 * Método que verifica se o processo deve ir para IO
	 * Se tiver que ir, envia para fila de processos aguardando IO
	 * e retira processo do processador
	 */
	private void verificaProcessoVaiParaIO() {
		if(processoExecutando != null && ehTrocaContexto == false) {
			if(processoExecutando.getOperacaoIO().size() > 0) {
				int momentoIO = processoExecutando.getOperacaoIO().get(0);
				if(momentoIO == processoExecutando.getTempoExecucao() - processoExecutando.getTempoExecucaoRestante()) {
					enviaProcessoFilaAguardandoIO();
					realizaTrocaContexto();
					retiraProcessoExecucao();
				}
			}
		}
	}
	
	/**
	 * Método que verifica se algum processo deve retornar do IO
	 * Se tiver que retornar, remove da fila de processos em IO
	 * e adiciona na fila de processos prontos para execução
	 * Caso o processador esteja disponível
	 * adiciona o procesos para execução no processador
	 */
	private void verificaProcessoRetornaIO() {
		if(processosAguardandoIO.size() > 0) {
			for(int i = 0 ; i < processosAguardandoIO.size() ; i++) {
				if(processosAguardandoIO.getIndex(i).getTempoIOCorrente() == 0) {
					processosAguardandoIO.getIndex(i).operacaoIO.remove(0);
					processosProntosExecucao.insertionSort(processosAguardandoIO.getIndex(i), "prioridade");
					processosAguardandoIO.removeIndex(i);
					if(processoExecutando == null) {
						colocaProcessoEmExecucao();
					}			
				}
		    }
		}
	}
	
	/**
	 * Método que coloca o processo em execução no processador
	 * Caso o contador de execução tenha sido zerado (ou seja, o processo rodou toda sua fatia de tempo)
	 * restaura o contador de execução
	 */
	private void colocaProcessoEmExecucao() {
		realizaTrocaContexto();
		processoExecutando = processosProntosExecucao.getFirst();
		if(processoExecutando.getContadorExecucao() == 0) {
			processoExecutando.setContadorExecucao(fatiaTempo);
		}
		processosProntosExecucao.removeFirst();
	}
	
	/**
	 * Método que realiza a troca de contexto
	 */
	private void realizaTrocaContexto() {
		ehTrocaContexto = true;
	}
	
	/**
	 * Método que reseta a troca de contexto
	 */
	private void resetaTrocaContexto() {
		ehTrocaContexto = false;
	}
	
	/**
	 * Método que retira processo de execução do processador
	 */
	private void retiraProcessoExecucao() {
		processoExecutando = null;
	}
	
	/**
	 * Método que devolve um processo em execução para a fila de processos prontos para execução
	 * mas não remove o processo do processador
	 */
	private void devolveProcessoFilaDisponiveis() {
		processosProntosExecucao.insertionSort(processoExecutando, "prioridade");
	}
	
	/**
	 * Método que adiciona um processo em execução poara a fila de processos finalizados
	 */
	private void enviaProcessoFilaFinalizados() {
		processosFinalizados.add(processoExecutando);
	}
	
	/**
	 * Método que envia processo para a fila de processos aguardando IO
	 * e seta o tempo de IO do processo
	 */
	private void enviaProcessoFilaAguardandoIO() {
		processoExecutando.setTempoIOCorrente(tempoIO);
		processosAguardandoIO.add(processoExecutando);
	}
	
	/**
	 * Método que imprime a indicação de processador parado na saída
	 */
	private void adicionaSaidaProcessadorParado() {
		saidaProcessador.append(String.valueOf(processadorParado) + " ");
	}
	
	/**
	 * Método que imprime a indicação de troca de contexto na saída
	 */
	private void adicionaSaidaProcessadorTrocaContexto() {
		saidaProcessador.append(String.valueOf(trocaContexto) + " ");
	}
	
	/**
	 * Método que imprime a indicação do processo que está executando na saída
	 */
	private void adicionaSaidaProcessadorExecutando() {
		saidaProcessador.append(processoExecutando.nome.toString() + " ");
	}
	
	/**
	 * Método que realiza o processamento do processador.
	 */
	private void realizaProcessamento() {
		// Se estiver realizando troca de contexto
		if(ehTrocaContexto) {
			adicionaSaidaProcessadorTrocaContexto();
			resetaTrocaContexto();
			computaMetricasProcessoExecucaoTrocaContexto();
			computaMetricasProcessosProntosExeucao();
		// Senão
		// Se estiver executando algum processo
		} else if(processoExecutando != null) {
			adicionaSaidaProcessadorExecutando();
			computaMetricasProcessoExecucao();
			computaMetricasProcessosProntosExeucao();
			computaMetricasProcessosAguardandoIO();			
		// Senão (indica que o processador está parado)
		} else {
			adicionaSaidaProcessadorParado();
			computaMetricasProcessosProntosExeucao();
			computaMetricasProcessosAguardandoIO();
		}
	}
}
