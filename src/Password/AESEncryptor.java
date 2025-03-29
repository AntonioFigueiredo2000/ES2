package src.Password;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESEncryptor implements IEncryptor {
    private final Cipher cipher;
    private final SecretKeySpec key;

    public AESEncryptor() {
        try {
            String keyString = "ChaveSecreta1234";
            key = new SecretKeySpec(keyString.getBytes("UTF-8"), "AES");
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar AESEncryptor: " + e.getMessage());
        }
    }

    @Override
    public String encrypt(String data) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao encriptar: " + e.getMessage());
        }
    }

    @Override
    public String decrypt(String data) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedBytes = Base64.getDecoder().decode(data);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desencriptar: " + e.getMessage());
        }
    }
}