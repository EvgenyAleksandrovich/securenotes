package sectool.securenotes;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class cryptMod {

    // HASH
    private static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = h.concat("0");
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String SHA1(String text)  {
        byte[] sha1hash = new byte[0];
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] textBytes = text.getBytes("iso-8859-1");
            md.update(textBytes, 0, textBytes.length);
            sha1hash = md.digest();
        } catch(Exception e){e.printStackTrace();}
        return hex(sha1hash);
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return sb.toString();
    }

    static String multiHash(String data){
        return md5(SHA1(data));
    }

    private static final byte[] IV = {
            0, 2, 4, 8, 16, 32, 64, 127,
            127, 64, 32, 16, 8, 4, 2, 0
    };

    static String encrypt(String data, String pwd) throws Exception {
        byte[] pass = new byte[32];

        for (byte i = 0; i < 32; i++){
            if (i < pwd.length()) pass[i] = pwd.getBytes()[i]; else pass[i] = 65;
        }

        SecretKeySpec skeySpec = new SecretKeySpec(pass,  "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(IV));
        return Base64.encodeToString(cipher.doFinal(data.getBytes()), Base64.DEFAULT);

    }

    static String decrypt(String data, String pwd) throws Exception {
        byte[] pass = new byte[32];

        for (byte i = 0; i < 32; i++){
            if (i < pwd.length()) pass[i] = pwd.getBytes()[i]; else pass[i] = 65;
        }

        final byte[] encryptedBytes = Base64.decode(data, Base64.DEFAULT);

        //Инициализация и задание параметров расшифровки
        SecretKeySpec secretKeySpec = new SecretKeySpec(pass, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(IV));

        //Расшифровка
        final byte[] resultBytes = cipher.doFinal(encryptedBytes);
        return new String(resultBytes);
    }

}


