import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;

public class KeyStoreInterface {

    public static final String KEYSTORE_PASSWORD = "keyStore";

    public static final int RSA_KEY_BYTES = 2048;
    public static final int CERTIFICATE_VALIDITY = 365; //days

    public static final String SERVER_URL = "http://localhost:8080";

    public KeyStoreInterface() {
        createBaseKeyStore();
    }

    // Main methods

    public HashMap<String, String> getPublicKey(String username, int sessionId, int albumId) {
        HashMap<String, String> response = new HashMap<>();
        try {
            if(!isUserInAlbum(username, sessionId, albumId)) {
                response.put("error", "User is not in album or user albums could not be obtained");
                return response;
            }
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(System.getProperty("user.dir") + "\\src\\main\\resources\\KeyStore.jks"), KEYSTORE_PASSWORD.toCharArray());
            if(!keyStore.isKeyEntry(String.valueOf(albumId))) {
                addKeyPairToKeyStore(albumId);
            }
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(writeCertificateToFileCommandGenerator(albumId));
            process.waitFor();
            if(process.exitValue() != 0) {
                System.out.println("Error while getting public key of album" + albumId);
            }
            InputStream inputStream = new FileInputStream(System.getProperty("user.dir") + "\\src\\main\\resources\\Temp.cert");
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(inputStream);

            response.put("success", "Public key obtained successfully");
            response.put("publicKey", new Gson().toJson(certificate.getPublicKey().getEncoded()));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Exception while getting public key of album" + albumId);
            return response;
        }
    }

    public HashMap<String, String> getPrivateKey(String username, int sessionId, int albumId) {
        HashMap<String, String> response = new HashMap<>();
        try {
            if(!isUserInAlbum(username, sessionId, albumId)) {
                response.put("error", "User is not in album or user albums could not be obtained");
                return response;
            }
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(System.getProperty("user.dir") + "\\src\\main\\resources\\KeyStore.jks"), KEYSTORE_PASSWORD.toCharArray());
            if(!keyStore.isKeyEntry(String.valueOf(albumId))) {
                addKeyPairToKeyStore(albumId);
            }
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(String.valueOf(albumId), String.valueOf(albumId).toCharArray());

            response.put("success", "Private key obtained successfully");
            response.put("privateKey", new Gson().toJson(privateKey.getEncoded()));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Exception while getting private key of album" + albumId);
            return response;
        }
    }

    // Auxiliary methods

    private void createBaseKeyStore() {
        try {
             File baseKeyStore = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\KeyStore.jks");
             if(!baseKeyStore.exists()) {
                 KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                 keyStore.load(null, KEYSTORE_PASSWORD.toCharArray());
                 FileOutputStream stream = new FileOutputStream(System.getProperty("user.dir") + "\\src\\main\\resources\\KeyStore.jks");
                 keyStore.store(stream, KEYSTORE_PASSWORD.toCharArray());
             }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not create key store");
        }
    }

    private void addKeyPairToKeyStore(int albumId) {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(addKeyPairCommandGenerator(albumId));
            process.waitFor();
            if(process.exitValue() != 0) {
                throw new Exception("An error occurred while creating keypair for album " + albumId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not add key pair to keystore");
        }
    }

    private boolean isUserInAlbum(String username, int sessionId, int albumId) {
        try {
            String URL = SERVER_URL + "/useralbums/" + sessionId + "/" + username;
            Request request = new Request.Builder().url(URL).get().build();
            Response response = new OkHttpClient().newCall(request).execute();

            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            return mapResponse.get(String.valueOf(albumId)) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String addKeyPairCommandGenerator(int albumId) {
        return "keytool -genkeypair " +
                "-alias " + albumId + " " +
                "-keyalg RSA " +
                "-keysize " + RSA_KEY_BYTES + " " +
                "-dname \"CN=Group20, OU=CMU, O=IST, L=Taguspark, ST=Lisboa, C=PT\" " +
                "-keypass " + albumId + " " +
                "-validity " + CERTIFICATE_VALIDITY + " " +
                "-storetype JKS " +
                "-keystore " + System.getProperty("user.dir") + "\\src\\main\\resources\\KeyStore.jks" + " " +
                "-storepass " + KEYSTORE_PASSWORD;
    }

    private String writeCertificateToFileCommandGenerator(int albumId) {
        return "keytool -export -keystore " + System.getProperty("user.dir") + "\\src\\main\\resources\\KeyStore.jks" + " " +
                "-alias " + albumId + " " + "-file " + System.getProperty("user.dir") + "\\src\\main\\resources\\Temp.cert" + " " +
                "-storepass " + KEYSTORE_PASSWORD;
    }

    protected void deleteKeystore() {
        try {
            File keyStore = new File(System.getProperty("user.dir") + "\\src\\main\\resources\\KeyStore.jks");
            if(keyStore.exists()) {
                if (keyStore.delete()) {
                    System.out.println("Keystore successfully deleted");
                } else {
                    System.out.println("Error while deleting keystore");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
