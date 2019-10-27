
import java.io.*;
import java.util.*;
import java.util.regex.*;  

import java.nio.file.*;

/**
 * Escalonador
 * Classe responsável por carregar e fazer o escalonamento dos processos carregados
 */
public class Escalonador {

	private static List<Processo> lista_teste = new ArrayList<Processo>();
	private static List<Deque<Processo>> lista_de_processos_prontos = new ArrayList<Deque<Processo>>();
	private static HashMap<String, Bcp> tabela_de_processos = new HashMap<String, Bcp>();
	private static List<Processo> lista_de_bloqueados = new LinkedList<>();
	private static String[] nome_processos = new String[10];
	private static int maior_prioridade = 0;
	private static int quantum = -1;

	/**
	 * Carrega os de prioridade e de processo
	 */
	public static void loadFiles() {

		File processosFolder = new File(Paths.get("processos").toString());
		File[] listOfFiles = validaArquivos(processosFolder);
		Arrays.sort(listOfFiles);
		for (int i = 0; i < listOfFiles.length; i++) {
			String nomeArquivo = listOfFiles[i].getName();
			try (BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i]))) {
				if (i < 10) {
					List<String> textSegment = new ArrayList<String>();
					String line;
					while ((line = br.readLine()) != null) {
						textSegment.add(line);
					}
					if(checaProcesso(nomeArquivo)){
						String processName = textSegment.get(0); //Seta o nome do processo de acordo com o valor da primeira linha
						char[] arranjoNome = processName.toCharArray();
						nome_processos[i] = processName;
						
						int priority = getNumeroProcesso(Files.readAllLines(Paths.get("processos/prioridades.txt")), nomeArquivo);
						if (priority > maior_prioridade)
							maior_prioridade = priority;
						Processo process = new Processo(processName, priority, textSegment,
								Integer.parseInt(processName.substring(6, arranjoNome.length)));
						lista_teste.add(process);
						Bcp bloco = new Bcp(priority, processName);
						tabela_de_processos.put(processName, bloco);
					}
				
				}

				if (i == 11)
					quantum = Integer.parseInt(br.readLine());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Dado um nome de arquivo retorna se o mesmo é um arquivo de processo ou arquivo de prioridades ou é o arquivo de quantum
	 */
	private static boolean checaProcesso(String nomeArquivo) {
		if(nomeArquivo.equals("prioridades.txt") || nomeArquivo.equals("quantum.txt"))
		return false;
		return true;
	}

	/**
	 * Obtêm o numero do processo a partir da string com o nome do processo
	 * 
	 * Trocando todos os caracteres não números do nome do processo por vazio
	 */
	private static int getNumeroProcesso(List<String> strings, String nomeArquivoProcura) {

		int numero = -1;
		for (String nome : strings) {
			if(nome.equals(nomeArquivoProcura))
				{
					numero = Integer.parseInt(nome.replaceAll("\\D", ""));
					return numero;
				}
		}
		Erros.erroProcessoNaoEncontradoNaListaDePrioridades(strings, nomeArquivoProcura);
		return numero;
	}



	/**
	 * Função responsável por fazer uma pré validação dos arquivos dentro da pasta processos (Como o nome do arquivo, se tem prioridade, quantum, etc)
	 * @param diretorio pasta processos
	 * @return Lista dos arquivos lidos
	 */
	private static File[] validaArquivos(File diretorio) {
		// Trata pasta Vazia
		String nomeDiretorio = diretorio.getName();
		if (!diretorio.exists()) {
			Erros.erroDiretorio(nomeDiretorio);
		}

		File[] listOfFiles = diretorio.listFiles();

		// Trata Quantidade de arquivos <=1 (Ou falta processo ou falta as prioridades o
		// os dois)
		if (listOfFiles.length <= 1) {
			Erros.erroArquivos(nomeDiretorio);
		}

		/**
		 * Se ja encontrou o arquivo de prioridades na pasta
		 */
		boolean prioridades = false;
		/**
		 * Se ja encontrou o arquivo de quantum na pasta
		 */
		boolean quantum = false;

		for (File file : listOfFiles) {
			String nomeArquivo = file.getName();
			if(!prioridades && nomeArquivo.equals("prioridades.txt")) prioridades = true;
			if(!quantum && nomeArquivo.equals("quantum.txt")) quantum = true;

			if(!(Pattern.matches("prioridades.txt|quantum.txt|\\d+.txt", nomeArquivo))){
				Erros.erroArquivoNaoValidoEmProcessos(nomeDiretorio, nomeArquivo);
			}
		}
		if(!prioridades) Erros.erroArquivoPrioridadeFaltando(nomeDiretorio);
		if(!quantum) Erros.erroArquivoQuantumFaltando(nomeDiretorio);
		
		return listOfFiles;

	}

	/**
	 * Cria a lista de prioridades baseado do arquivo prioridades.txt
	 */
	public static void criandoListaPrioridades() {
		for (int i = maior_prioridade; i > 0; i--) {

			Deque<Processo> process = new LinkedList<>();

			for (int j = 0; j < 10; j++) {
				try {
					int prioridade_atual = Integer
							.parseInt(Files.readAllLines(Paths.get("processos/prioridades.txt")).get(j));

					if (prioridade_atual == i)
						process.addLast(lista_teste.get(j));

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (process.size() > 0)
				lista_de_processos_prontos.add(process);
		}

		Deque<Processo> prioridade0 = new LinkedList<>();
		lista_de_processos_prontos.add(prioridade0);
	}

	/**
	 * Printa na tela a ordem dos processos prontos
	 */
	public static void printaOrdemProntos() {

		for (int i = 0; i <= maior_prioridade; i++) {
			for (Iterator it = lista_de_processos_prontos.get(i).iterator(); it.hasNext();) {
				Processo p = (Processo) it.next();
				System.out.println("carregando " + p.getNome());
			}
			System.out.println();
			System.out.println("fim da fila " + i);
			System.out.println();
		}
	}

	// public static void reorganiza(int fila){
	// //Collections.sort(lista_de_processos_prontos.get(fila));
	// }

	/**
	 * Verifica a lista de bloqueados
	 */
	public static void checaBloquados() {
		Iterator it = lista_de_bloqueados.iterator();

		while (it.hasNext()) {
			Processo p = (Processo) it.next();

			p.diminuiTempo();

			if (p.getTempoBloq() == 0) {
				lista_de_bloqueados.remove(p);

				// aqui da cagada pq credito = 0 siginifica tabela 4
				lista_de_processos_prontos.get(maior_prioridade - p.getCreditos()).addLast(p);
				it = lista_de_bloqueados.iterator();
			}

		}

	}

	/**
	 * Atualiza o BCP
	 * @param bloco classe do BCP
	 * @param pc1 contador do programa
	 * @param estado Estado do processo
	 * @param x valor x do processo
	 * @param y valor y do processo
	 * @param credit Quantidade de creditos restantes do processo
	 */
	public static void atualizaBcp(Bcp bloco, int pc1, String estado, int x, int y, int credit) {
		bloco.setPc(pc1);
		bloco.setEstado(estado);
		bloco.setX(x);
		bloco.setY(y);
		bloco.setCreditos(credit);
	}

	/**
	 * Executa os processos de acordo com a fila
	 * @param fila número da fila a ser executada
	 */
	public static void executa(int fila) {

		while (lista_de_processos_prontos.get(fila).size() > 0) {

			Iterator it = lista_de_processos_prontos.get(fila).iterator();

			Processo p = (Processo) it.next();

			// System.out.println();
			System.out.println("executando " + p.getNome());

			int i = 0;
			for (i = 0; i < (p.getNQuantum() * quantum); i++) {

				String comando1 = p.getNextComando();
				// System.out.println(" -> " + comando1);
				char[] comando = comando1.toCharArray();
				int tamanho = comando.length;

				switch (comando[0]) {

				case 'X':
					p.setX(Integer.parseInt(comando1.substring(2, tamanho)));

					break;

				case 'Y':
					p.setY(Integer.parseInt(comando1.substring(2, tamanho)));
					break;

				case 'E':
					System.out.println("E/S iniciada em " + p.getNome());
					System.out.println("Interrompendo " + p.getNome() + " apos " + (i + 1) + " instrucoes");

					int aux = p.getCreditos();
					p.setCreditos(aux - 2);
					lista_de_processos_prontos.get(fila).removeFirst();

					lista_de_bloqueados.add(p);
					p.aumentaQuantum();
					p.setTempoBloq(2);

					p.setEstado("bloqueado");

					atualizaBcp(tabela_de_processos.get(p.getNome()), p.getPc(), p.getEstado(), p.getX(), p.getY(),
							p.getCreditos());

					checaBloquados();

					break;

				case 'S':

					lista_de_processos_prontos.get(fila).removeFirst();
					System.out.println(p.getNome() + " terminado " + "X=" + p.getX() + "Y=" + p.getY());

					checaBloquados();

					break;

				default:

					break;
				}

				if (comando[0] == 'E' || comando[0] == 'S')
					break;

			}

			if (i < p.getNQuantum() * (quantum - 1)) {
				continue;

			}

			if (i + 1 != p.getNQuantum() * quantum) {
				System.out.println("Interrompendo " + p.getNome() + " apos " + i + " instrucoes");

				int aux = p.getCreditos();
				p.setCreditos(aux - 2);
				lista_de_processos_prontos.get(fila).removeFirst();

				if (fila + 2 <= maior_prioridade)
					lista_de_processos_prontos.get(fila + 2).addFirst(p);
				else
					lista_de_processos_prontos.get(maior_prioridade).addFirst(p);

				p.aumentaQuantum();
				atualizaBcp(tabela_de_processos.get(p.getNome()), p.getPc(), p.getEstado(), p.getX(), p.getY(),
						p.getCreditos());
				checaBloquados();
			}

		}

	}

	// falta terminar
	public static void redistribui() {

		lista_teste.clear();
		Iterator it = lista_de_processos_prontos.get(maior_prioridade).iterator();
		while (it.hasNext()) {

			Processo p = (Processo) it.next();

			int fila = p.getPrioridade();

			p.setCreditos(p.getPrioridade());

			lista_teste.add(p);

			lista_de_processos_prontos.get(maior_prioridade).removeFirst();

			it = lista_de_processos_prontos.get(maior_prioridade).iterator();

		}

		Collections.sort(lista_teste, new Comparator<Processo>() {
			@Override
			public int compare(Processo o1, Processo o2) {
				if (o1.getVnome() > o2.getVnome())
					return 1;
				if (o1.getVnome() < o2.getVnome())
					return -1;
				return 0;
			}
		});

		Collections.sort(lista_teste);

	}

	// pode ter um for, ou um while, enquanto a fila de prioridade 0 não tem 10
	// itens
	public static void main(String[] args) {
		loadFiles();
		criandoListaPrioridades();
		printaOrdemProntos();

		System.out.println("_____________________");
		System.out.println();

		for (int i = 0; i < 4; i++) {
			// printaOrdemProntos();
			executa(i);
			// printaOrdemProntos();
		}

		System.out.println("_____________________");
		// executa(0);

		printaOrdemProntos();

		System.out.println("_____________________");
		redistribui();
		printaOrdemProntos();

		System.out.println(lista_teste.get(0).getNome());
		System.out.println(lista_teste.get(1).getNome());
		System.out.println(lista_teste.get(2).getNome());

		// printa lista de bloqueados
		// for(Iterator it = lista_de_bloqueados.iterator(); it.hasNext(); ){
		// Processo p = (Processo) it.next();
		// System.out.print( p.getNome() + " ");
		// System.out.println( p.getCreditos());
		// }

	}
}