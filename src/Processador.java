
public class Processador {
	/**
	 * Tempo que leva para executar opera��o de IO
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
	 * Indicador do n�mero de processos
	 */
	private int numeroProcessos;

	/**
	 * Fila de processos que ainda n�o est�o dispon�veis para escalonamento.
	 */
	Fila processosNaoProntos;
	
	/**
	 * Fila de processos escalon�veis, prontos para serem executados pelo processador.
	 */
	Fila processosProntosExecucao;
	
	/**
	 * Fila de processos que est�o aguardando opera��o de IO
	 */
	Fila processosAguardandoIO;
	
	/**
	 * Fila de processos finalizados pelo processador
	 */
	Fila processosFinalizados;
	
	/**
	 * Processo que est� atualmente no processador
	 */
	Processo processoExecutando;

	/**
	 * Sa�da do processador
	 */
	StringBuilder saidaProcessador;
	
	/**
	 * Tempo do rel�gio
	 */
	int tempo;
	
	/**
	 * Vari�vel indica se est� ocorrendo troca de contexto
	 */
	boolean ehTrocaContexto;
	
	/**
	 * Construtor que instancia o processador
	 * @param processosNaoProntos Fila de processos n�o escalon�veis ainda.
	 * @param numeroProcessos Numero de processos que ser�o escalonados.
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
		// Como n�o houve processo no tempo 0, adiciona marca��o � sa�da.
		adicionaSaidaProcessadorParado();
		
		// Loop principal do programa.
		// Executa enquanto a quantidade de processos finalizados for menor que o n�mero de processos
		while(processosFinalizados.size() < numeroProcessos ) {
			
			// M�todo que retira da lista de processos n�o prontos, e move para a fila de execu��o
			verificaProcessosDisponiveisExecucao();
			
			// M�todo que verifica se processo vai para IO;
			verificaProcessoVaiParaIO();
			
			// M�todo que verifica se tem processo para retornar de IO
			verificaProcessoRetornaIO();

			// Se n�o tem ninguem no processo executando
			if(processoExecutando == null) {
				// E a processos prontos para execu��o n�o est� vazia
				if(!processosProntosExecucao.isEmpty()) {
					// Coloca processo no processador para execu��o
					colocaProcessoEmExecucao();
				}
				// Caso tenha processo em execu��o
			} else {
				// Se fila de processos prontos para execu��o n�o estiver vazia, e o primeiro processo dispon�vel para execu��o tiver prioridade MELHOR que o processo atual em execu��o
				if(!processosProntosExecucao.isEmpty() && processosProntosExecucao.getFirst().getPrioridade() < processoExecutando.getPrioridade()) {
					// Devolve processo em execu��o para fila de processos dispon�veis
					devolveProcessoFilaDisponiveis();
					// Coloca processo no processador para execu��o
					colocaProcessoEmExecucao();
				// Sen�o 
				// verifica se o contador de execu��o do processo em execu��o chegou a zero (indica que acabou seu tempo no processador) ou se o tempo de execu��o do processo acabou
				} else if(processoExecutando.getContadorExecucao() == 0 || processoExecutando.getTempoExecucaoRestante() == 0) {
					// Se o tempo de execu��o do processo for maior do que zero, indica que ele ainda n�o acabou seu processamento
					if(processoExecutando.getTempoExecucaoRestante() > 0) {
						// Devolve processo em execu��o para fila de processos dispon�veis
						devolveProcessoFilaDisponiveis();
						// Retira o processo de execu��o, deixando o processador livre.
						retiraProcessoExecucao();
					// Sen�o (indica que acabou seu processamento)
					} else {
						// Envia processo para a fila de processos finalizados
						enviaProcessoFilaFinalizados();
						// Retira o processo de execu��o, deixando o processador livre.
						retiraProcessoExecucao();
					}
					
					// Se a fila de processos prontos para execu��o n�o est� vazia, e o n�mero de processos finalizdos for menor que o n�mero total de processos a serem executados
					if(!processosProntosExecucao.isEmpty() && processosFinalizados.size() < numeroProcessos) {
						// Coloca processo em execu��o
						colocaProcessoEmExecucao();
					// Sen�o
					// Se o n�mero de processos finalizdos for menor que o n�mero total de processos a serem executados
					} else if(processosFinalizados.size() < numeroProcessos) {
						// Realiza a troca de contexto
						realizaTrocaContexto();
					// Sen�o, n�o tem mais processos para executar
					} else {
						// Encerra o loop
						break;
					}
				}
			}
			
			// Realiza o processamento de acordo com a situa��o do processador (ele pode estar realizando troca de contexto, processando algum processo ou parado)
			realizaProcessamento();
			
			tempo++;
		}
		
		imprimeMetricas(processosFinalizados, tempo);
		
		imprimeSaida();
		
	}
	
	/**
	 * M�todo que computa as m�tricas do processo em execu��o:
	 * Decrementa o tempo restante de execu��o
	 * Decrementa o contador de execu��o (fatia de tempo do processador)
	 * Incrementa o turnaround do processo
	 * Indica que o processo j� foi iniciado
	 */
	private void computaMetricasProcessoExecucao() {
		processoExecutando.setTempoExecucaoRestante(-1);
		processoExecutando.setContadorExecucao(-1);
		processoExecutando.setTurnaround(1);
		processoExecutando.setProcessojaIniciado(true);
	}
	
	/**
	 * M�todo que computa as m�tricas se tiver processo em execu��o:
	 * Incrementa o turnaround do processo
	 * incrementa o tempo m�dio de espera do processo
	 * Caso o processo n�o tenha sido iniciado ainda, incrementa o tempo m�dio de resposta
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
	 * M�todo que computa as m�tricas dos processos prontos para execu��o:
	 * Incrementa turnaround dos processos na fila de prontos para execu��o
	 * Incrementa tempo m�dio de resposta dos processos na fila de prontos para execu��o
	 * Incrementa tempo m�dio de espera dos processos na fila de prontos para execu��o
	 */
	private void computaMetricasProcessosProntosExeucao() {
		processosProntosExecucao = incrementaTurnaround(processosProntosExecucao); 
		processosProntosExecucao = incrementaTempoRespostaMedio(processosProntosExecucao); 
		processosProntosExecucao = incrementaTempoEsperaMedio(processosProntosExecucao); 
	}
	
	/**
	 * M�todo que computa as m�tricas dos processos que est�o aguardando IO:
	 * Decrementa o tempo corrente de IO dos processos da fila
	 * Incrementa o turnaround dos processos da fila
	 * Incrementa o tempo m�dio de espera dos processos da fila
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
	 * M�todo que incrementa o turnaround dos procesos de uma fila de uma fila
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
	 * M�todo que incrementa o tempo m�dio de resposta dos procesos de uma fila de uma fila
	 * @param fila Fila que vai ter o tempo m�dio de resposta incrementado
	 * @return Fila com tempo m�dio de resposta incrementado
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
	 * M�todo que incrementa o tempo m�dio de espera dos procesos de uma fila de uma fila
	 * @param fila Fila que vai ter o tempo m�dio de espera incrementado
	 * @return Fila com tempo m�dio de espera incrementado
	 */
	private Fila incrementaTempoEsperaMedio(Fila fila) {
		for(int i = 0 ; i < fila.size() ; i++) {
			fila.getIndex(i).setTempoMedioEspera(1);
		}
		return fila;
	}
	
	/**
	 * M�todo que imprime sa�da no console.
	 */
	private void imprimeSaida() {
		System.out.println(saidaProcessador.toString());
	}
	
	/**
	 * M�todo que imprime as m�tricas de uma fila, e o tempo total de execu��o do processador.
	 * @param fila Fila que vai ter as m�tricas impressas
	 * @param tempo Tempo total de execu��o do processador
	 */
	private void imprimeMetricas(Fila fila, int tempo) {
		System.out.println("Tempo de execu��o: " + tempo);
		
		float tempoRespostaTotal = 0;
		float tempoEsperaTotal = 0;
		
		for(int i = 0 ; i < fila.size() ; i++) {
			tempoRespostaTotal += fila.getIndex(i).getTempoMedioResposta();
			tempoEsperaTotal += fila.getIndex(i).getTempoMedioEspera();
		}
		
		float tempoRespostaMedio = tempoRespostaTotal / fila.size();
		float tempoEsperaMedio = tempoEsperaTotal / fila.size();
		
		System.out.println("Tempo de resposta m�dio: " + tempoRespostaMedio);
		System.out.println("Tempo de espera m�dio: " + tempoEsperaMedio);
		System.out.println("\n");
		for(int i = 0 ; i < fila.size() ; i++) {
			System.out.println(fila.getIndex(i).getMetricas());
		}
		System.out.println("\n");
	}
	
	/**
	 * M�todo que verifica quais processos est�o dispon�veis para execu��o
	 * remove da fila de n�o dispon�veis
	 * e adiciona os dispon�veis na fila de processos prontos para execu��o. 
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
	 * M�todo que verifica se o processo deve ir para IO
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
	 * M�todo que verifica se algum processo deve retornar do IO
	 * Se tiver que retornar, remove da fila de processos em IO
	 * e adiciona na fila de processos prontos para execu��o
	 * Caso o processador esteja dispon�vel
	 * adiciona o procesos para execu��o no processador
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
	 * M�todo que coloca o processo em execu��o no processador
	 * Caso o contador de execu��o tenha sido zerado (ou seja, o processo rodou toda sua fatia de tempo)
	 * restaura o contador de execu��o
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
	 * M�todo que realiza a troca de contexto
	 */
	private void realizaTrocaContexto() {
		ehTrocaContexto = true;
	}
	
	/**
	 * M�todo que reseta a troca de contexto
	 */
	private void resetaTrocaContexto() {
		ehTrocaContexto = false;
	}
	
	/**
	 * M�todo que retira processo de execu��o do processador
	 */
	private void retiraProcessoExecucao() {
		processoExecutando = null;
	}
	
	/**
	 * M�todo que devolve um processo em execu��o para a fila de processos prontos para execu��o
	 * mas n�o remove o processo do processador
	 */
	private void devolveProcessoFilaDisponiveis() {
		processosProntosExecucao.insertionSort(processoExecutando, "prioridade");
	}
	
	/**
	 * M�todo que adiciona um processo em execu��o poara a fila de processos finalizados
	 */
	private void enviaProcessoFilaFinalizados() {
		processosFinalizados.add(processoExecutando);
	}
	
	/**
	 * M�todo que envia processo para a fila de processos aguardando IO
	 * e seta o tempo de IO do processo
	 */
	private void enviaProcessoFilaAguardandoIO() {
		processoExecutando.setTempoIOCorrente(tempoIO);
		processosAguardandoIO.add(processoExecutando);
	}
	
	/**
	 * M�todo que imprime a indica��o de processador parado na sa�da
	 */
	private void adicionaSaidaProcessadorParado() {
		saidaProcessador.append(String.valueOf(processadorParado) + " ");
	}
	
	/**
	 * M�todo que imprime a indica��o de troca de contexto na sa�da
	 */
	private void adicionaSaidaProcessadorTrocaContexto() {
		saidaProcessador.append(String.valueOf(trocaContexto) + " ");
	}
	
	/**
	 * M�todo que imprime a indica��o do processo que est� executando na sa�da
	 */
	private void adicionaSaidaProcessadorExecutando() {
		saidaProcessador.append(processoExecutando.nome.toString() + " ");
	}
	
	/**
	 * M�todo que realiza o processamento do processador.
	 */
	private void realizaProcessamento() {
		// Se estiver realizando troca de contexto
		if(ehTrocaContexto) {
			adicionaSaidaProcessadorTrocaContexto();
			resetaTrocaContexto();
			computaMetricasProcessoExecucaoTrocaContexto();
			computaMetricasProcessosProntosExeucao();
		// Sen�o
		// Se estiver executando algum processo
		} else if(processoExecutando != null) {
			adicionaSaidaProcessadorExecutando();
			computaMetricasProcessoExecucao();
			computaMetricasProcessosProntosExeucao();
			computaMetricasProcessosAguardandoIO();			
		// Sen�o (indica que o processador est� parado)
		} else {
			adicionaSaidaProcessadorParado();
			computaMetricasProcessosProntosExeucao();
			computaMetricasProcessosAguardandoIO();
		}
	}
}
