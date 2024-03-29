/**
 * Erros
 * Classe que serve para reunir os erros costumizados do programa
 */
public class Erros {

    /**
     * Erro que ocorre quando o programa não encontra o diretorio em que os processos deveriam estar
     * @param diretorio Diretorio dos processos
     */
    public static void erroDiretorio(String diretorio) {
        System.out.println("O diretorio de processos ("+ diretorio + ") Nao pode ser encontrado, abortando...");
        System.exit(-3);
    }

    /**
     * Erro que ocorre quando o diretório dos processos não possui os arquivos necessários para a execução 
     * @param diretorio pasta processos
     */
    public static void erroArquivos(String diretorio, String... arquivos){
        System.out.println("O diretorio " + diretorio + " nao possui os arquivos necessarios para a execucao do programa");
        for (String string : arquivos) {
            System.out.println(string);
        }
        System.exit(-15);
    }
    /**
     * Erro que ocorre quando o diretório dos processos não possui os arquivos necessários para a execução 
     * @param diretorio  pasta processos
     */
    public static void erroArquivos(String diretorio){
        System.out.println("O diretorio " + diretorio + " nao possui os arquivos necessarios para a execucao do programa");
        System.exit(-14);
    }

    /**
     * Quando o arquivo de prioridades não é encontrado 
     */
    public static void erroArquivoPrioridadeFaltando(String diretorio) {
        System.out.println("O arquivo de prioridades (prioridades.txt) nao existe no diretorio " + diretorio);
        System.exit(-16);
    }
    /**
     * Quando o arquivo de quantum não é encontrado 
     */
    public static void erroArquivoQuantumFaltando(String diretorio) {
        System.out.println("O arquivo de prioridades (quantum.txt) nao existe no diretorio " + diretorio);
        System.exit(-16);
    }

    /**
     * Erro que ocorre quando existem arquivos estranhos na pasta de processo 
     * @param diretorio pasta processos
     * @param arquivo arquivo estranho encontrado
     */
    public static void erroArquivoNaoValidoEmProcessos(String diretorio, String arquivo) {
        System.out.println("Arquivo estranho na pasta " + diretorio + ": \"" + arquivo + "\" (so eh permitido processo(numero).txt ou prioridades.txt)");
        System.exit(-18);
    }
    
}