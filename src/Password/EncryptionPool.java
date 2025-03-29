package src.Password;

import java.util.ArrayList;
import java.util.List;

public class EncryptionPool {
    private static EncryptionPool instance; // Singleton para um único pool
    private List<AESEncryptor> available;
    private List<AESEncryptor> inUse;
    private final int maxSize = 3;         // Limite inicial de objetos no pool

    // Construtor privado (Singleton)
    private EncryptionPool() {
        available = new ArrayList<>();
        inUse = new ArrayList<>();
        // Preenche o pool com instâncias as iniciais
        for (int i = 0; i < maxSize; i++) {
            available.add(new AESEncryptor());
        }
    }

    public static synchronized EncryptionPool getInstance() {
        if (instance == null) {
            instance = new EncryptionPool();
        }
        return instance;
    }

    public synchronized IEncryptor acquireEncryptor() {
        if (available.isEmpty()) {
            return new AESEncryptor();
        }
        IEncryptor encryptor = available.remove(0);
        inUse.add((AESEncryptor) encryptor);
        return encryptor;
    }

    public synchronized void releaseEncryptor(IEncryptor encryptor) {
        if (encryptor instanceof AESEncryptor && inUse.contains(encryptor)) {
            inUse.remove(encryptor);
            if (available.size() < maxSize) {
                available.add((AESEncryptor) encryptor);
            }
            // Descartar o objeto caso pool esteja cheio
        }
    }
}