package src.Password;

   import src.ConfiguracaoCentral;
   import java.util.Stack;

   public class PasswordCaretaker {
       private final PasswordManager originator;
       private final Stack<PasswordManager.PasswordMemento> history;

       public PasswordCaretaker(PasswordManager originator) {
           this.originator = originator;
           this.history = new Stack<>();
       }

       public void saveState() {
           history.push(originator.saveState());
       }

       public void undo() {
           if (!history.isEmpty()) {
               originator.restoreState(history.pop());
           } else {
               System.out.println("Nenhum estado para restaurar.");
           }
       }

       public void generateAndStore(String username, String categoryPath, ConfiguracaoCentral config) {
           originator.generateAndStore(username, categoryPath, config);
           saveState(); // Salva automaticamente após alterar o estado
       }

       public String getPasswordByUser(String username) {
           return originator.getPasswordByUser(username);
       }

       public PasswordComponent getRoot() {
           return originator.getRoot();
       }

       public String getLastCategory() {
           return originator.getLastCategory();
       }

       public String getLastPassword() {
           return originator.getLastPassword();
       }
   }