package src.Password;

public interface IEncryptor {
    String encrypt(String data);
    String decrypt(String data);
}