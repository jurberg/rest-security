package filter;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;

public class SignatureHelper {
	
	public static final String API_KEY = "RS00001";
	public static final String PUBLIC_KEY = "MIIBtzCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZp;RV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYQAAoGARBu0g4MdHVhU6NoSXMKDBFSX9KfkTwIOXM6GY3DhAWsQhejkAkxp8c0IpkKn+i+PQNM/2pntXLWxDGHQGhfJIwvP041SrRTCXtx8SJ59ima8Z6/my7N72pPvbeDcPjlshtp/oa6eHh9M4J18W5hI4HD6I6f4qnppP1rRYaZolhw=";
	public static final String PRIVATE_KEY = "MIIBSwIBADCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoEFgIUOCrAiHXm+FJBM7QHMhBxanPAn3k=";
	
	public static final String APIKEY_HEADER = "apikey";
	public static final String TIMESTAMP_HEADER = "timestamp";
	public static final String SIGNATURE_HEADER = "signature";
	public static final List<String> SIGNATURE_KEYWORDS = Arrays.asList(APIKEY_HEADER, TIMESTAMP_HEADER);

	private static final String ALGORITHM = "DSA";

	public static String getPublicKey(String apiKey) {
		if (apiKey.equals(SignatureHelper.API_KEY)) {
			return SignatureHelper.PUBLIC_KEY;
		}
		return null;	
	}

	public static String createSignature(HttpHeaders headers, String url, String privateKey) throws Exception {
		
		TreeMap<String, String> sortedHeaders = new TreeMap<String, String>();
		for (String key : headers.keySet()) {
			if (SIGNATURE_KEYWORDS.contains(key)) {
				sortedHeaders.put(key, headers.get(key).get(0));
			}
		}
		
		String sortedUrl = createSortedUrl(url, sortedHeaders);
		
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		byte[] privateKeyBytes = Base64.decodeBase64(privateKey);
		EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		
		Signature sig = Signature.getInstance(ALGORITHM);		
		sig.initSign(keyFactory.generatePrivate(privateKeySpec));
		sig.update(sortedUrl.getBytes());
		
		return Base64.encodeBase64URLSafeString(sig.sign());
	}

	private static PublicKey decodePublicKey(String publicKey) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
		byte[] publicKeyBytes = Base64.decodeBase64(publicKey);
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		return keyFactory.generatePublic(publicKeySpec);
	}

	public static boolean validateSignature(String url, String signatureString, String apiKey) throws InvalidKeyException, Exception {
		
		String publicKey = SignatureHelper.getPublicKey(apiKey);
		if (publicKey == null) return false;
		
		Signature signature = Signature.getInstance(ALGORITHM);
		signature.initVerify(decodePublicKey(publicKey));
		signature.update(url.getBytes());
		try {
			return signature.verify(Base64.decodeBase64(signatureString));
		} catch (SignatureException e) {
			return false;
		}
	}

	public static String createSortedUrl(HttpServletRequest request) {
		
		// use a TreeMap to sort the headers and parameters
		TreeMap<String, String> headersAndParams = new TreeMap<String, String>();	
		
		// load header values we care about
		Enumeration e = request.getHeaderNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (SIGNATURE_KEYWORDS.contains(key)) {
				headersAndParams.put(key, request.getHeader(key));
			}
		}
		
		// load parameters
		for (Object key : request.getParameterMap().keySet()) {
			String[] o = (String[]) request.getParameterMap().get(key);
			headersAndParams.put((String) key, o[0]);
		}
	
		return createSortedUrl(
				request.getContextPath() + request.getServletPath() + request.getPathInfo(),
				headersAndParams);
		
	}

	public static String createSortedUrl(String url, TreeMap<String, String> headersAndParams) {
		// build the url with headers and parms sorted
		String params = "";
		for (String key : headersAndParams.keySet()) {
			if (params.length() > 0) {
				params += "@";
			}
			params += key + "=" + headersAndParams.get(key).toString();
		}
		if (!url.endsWith("?")) url += "?";
		return url + params;	
	}

	public static void main(String[] args) throws Exception {
		
		 // Generate a 1024-bit Digital Signature Algorithm (DSA) key pair
	    KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
	    keyGen.initialize(1024);
	    KeyPair keypair = keyGen.genKeyPair();
	    PrivateKey privateKey = keypair.getPrivate();
	    PublicKey publicKey = keypair.getPublic();

	    // Get the bytes of the public and private keys (these go in the database with API Key)
	    byte[] privateKeyEncoded = privateKey.getEncoded();
	    byte[] publicKeyEncoded = publicKey.getEncoded();
	    
	    System.out.println("Private Key: " + Base64.encodeBase64URLSafeString(privateKeyEncoded));
	    System.out.println("Public Key: " + Base64.encodeBase64URLSafeString(publicKeyEncoded));
	    
	}
	
}
