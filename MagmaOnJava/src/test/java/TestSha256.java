import static org.encrypting.HashUtils.sha256;

public class TestSha256 {
    // Для тестирования
    public static void main(String[] args) {
        // Тест: "abc" -> SHA-256
        byte[] input = "abc".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] hash = sha256(input);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        System.out.println(sb.toString());
    }
}
