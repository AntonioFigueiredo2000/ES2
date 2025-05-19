package src;

   import src.Password.FileStorage;
   import src.Password.PasswordCaretaker;
   import src.Password.PasswordManager;
   import src.autenticacao.AlertaSeguranca;
   import src.autenticacao.Autenticacao;
   import src.autenticacao.AutenticacaoMultifator;
   import src.autenticacao.AutenticacaoSimples;

   import java.util.Scanner;

   public class Main {
       public static void main(String[] args) {
           // Cria o PasswordManager e o Caretaker
           PasswordManager manager = new PasswordManager(new FileStorage());
           PasswordCaretaker caretaker = new PasswordCaretaker(manager);

           // Gera e adiciona passwords em categorias
           caretaker.generateAndStore("António", "pessoal", ConfiguracaoCentral.getInstancia());
           caretaker.generateAndStore("João", "pessoal/produtividade/Trello", ConfiguracaoCentral.getInstancia());
           caretaker.generateAndStore("Maria", "pessoal/Telefone", ConfiguracaoCentral.getInstancia());

           // Mostra a estrutura
           System.out.println("Estrutura de categorias e passwords (encriptadas):");
           caretaker.getRoot().display(0);

           // Recupera uma senha para mudar o estado
           String decryptedPassword = caretaker.getPasswordByUser("pessoal/Telefone/Maria");
           System.out.println("\nApós recuperação:");
           System.out.println("Senha recuperada: " + decryptedPassword);
           System.out.println("Categoria atual: " + caretaker.getLastCategory());
           System.out.println("Senha atual: " + caretaker.getLastPassword());

           // Restaura o estado anterior (undo)
           caretaker.undo();
           System.out.println("\nApós restauração (último estado):");
           System.out.println("Categoria restaurada: " + caretaker.getLastCategory());
           System.out.println("Senha restaurada: " + caretaker.getLastPassword());
           System.out.println("Estrutura restaurada:");
           caretaker.getRoot().display(0);

           // Restaura mais um estado anterior (undo novamente)
           caretaker.undo();
           System.out.println("\nApós restauração (penúltimo estado):");
           System.out.println("Categoria restaurada: " + caretaker.getLastCategory());
           System.out.println("Senha restaurada: " + caretaker.getLastPassword());
           System.out.println("Estrutura restaurada:");
           caretaker.getRoot().display(0);

           System.out.println("\n------------------------Autenticacao multifator-----------------------------:");

           Autenticacao autenticacaoSimples = new AutenticacaoSimples();
           Autenticacao autenticacaoComMultifator = new AutenticacaoMultifator(autenticacaoSimples);
           Autenticacao autenticacaoComAlerta = new AlertaSeguranca(autenticacaoComMultifator);

           // Testa a autenticação
           Scanner scanner = new Scanner(System.in);
           System.out.print("Digite o nome de utilizador: ");
           String utilizador = scanner.nextLine();
           System.out.print("Digite a password: ");
           String password = scanner.nextLine();

           boolean autenticado = autenticacaoComAlerta.autenticar(utilizador, password);

           if (autenticado) {
               System.out.println("Usuário autenticado com sucesso!");
           } else {
               System.out.println("Falha na autenticação.");
           }
       }
   }