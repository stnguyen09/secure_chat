import java.util.Random;
import java.math.*;

public class RSA {
	
	public RSA(){}
	
	// Compute a new private key, modulus, and public key
	public int[] computeKeys(){
		
		int[] keys = new int[3]; // [c, e, d]
		
		// Calculate 2 random prime numbers up to 100
		Random generator = new Random();
		Boolean prime1 = false;
		Boolean prime2 = false;
		int r1 = 0;
		int r2 = 0;
		while(!prime1){
			r1 = Math.abs(generator.nextInt()) % 100;
			prime1 = isPrime(r1);
		}
		
		while(!prime2){
			r2 = Math.abs(generator.nextInt()) % 100;
			prime2 = isPrime(r2);
		}
		// End prime number calculation
		
		keys[0] = r1 * r2; // c
		int m = (r1-1)*(r2-1);
		
		keys[1] = coprime(m); // e
		keys[2] = mod_inverse(keys[1], m); // d
		
		return keys;
	}
	
	// Checks if a number is prime
	public Boolean isPrime(int r){
		if(r%2==0){
			return false;
		}
		
		for(int i=3; i*i<r; i+=2){
			if(r%i==0){
				return false;
			}
		}		
		return true;
	}
	
	// Calculate a random coprime under 100
	public int coprime(int x){
		Random generator = new Random();
		int cop;
		
		while(true){
			int r = Math.abs(generator.nextInt()) % 100; // Keep things under 100
			try{
				int gcd = GCD(r, x);
				if(gcd == 1){
					cop = r;
					break;
				}
			} catch(StackOverflowError e){
				System.err.println("StackOverflow: Numbers were x=" + x + ", r=" + r);
			}
		}
		return cop;
	}
	
	// Encrypts the character using its ASCII representation
	public int encrypt(char word, int key, int c){
		int ascii = (int)word;
		int mod = modulo(ascii, key, c);
		return mod;
	}
	
	// Returns decrypted character
	public char decrypt(int cipher, int key, int c){
		BigInteger a1 = BigInteger.valueOf(cipher);
		BigInteger a2 = a1.pow(key);
		int mod = a2.mod(BigInteger.valueOf(c)).intValue();
		
		char character = (char)mod;
		return character;
	}
	
	// Calculates the GCD
	private int GCD(int a, int b){;
		BigInteger gcd = BigInteger.valueOf(a).gcd(BigInteger.valueOf(b));
		return gcd.intValue();
	}
	
	// (base^-1)%m
	public int mod_inverse(int base, int m){
		BigInteger a1 = BigInteger.valueOf(base);
		BigInteger a2 = BigInteger.valueOf(m);
		int modInverse = a1.modInverse(a2).intValue();
		
		return modInverse;
	}

	// (a^b)%c -- Need BigInteger to handle arbitrarily large numbers
	private int modulo(int a, int b, int c){
		BigInteger a1 = BigInteger.valueOf(a);
		BigInteger a2 = a1.pow(b);
		int mod = a2.mod(BigInteger.valueOf(c)).intValue();
		
		return mod;
	}
	
	// Euler's totient
	public int totient(int n){
		int count = 0;
		for(int i=1; i<n; i++){
			if(GCD(n, i) == 1){
				count++;
			}
		}
		return count;
	}
}
