import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CryptoService {


  // Pretvaranje PEM ↔ ArrayBuffer
  private pemToBase64(pem: string): string {
    return pem.replace(/-----(BEGIN|END)[\w\s]+-----/g, '').replace(/\s+/g, '');
  }

  // Pretvara base64 u binarni niz bajtova
  private base64ToArrayBuffer(base64: string): ArrayBuffer {
    const binary = atob(base64);
    const len = binary.length;
    const bytes = new Uint8Array(len);
    for (let i = 0; i < len; i++) bytes[i] = binary.charCodeAt(i);
    return bytes.buffer;
  }

  private arrayBufferToBase64(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (let i = 0; i < bytes.byteLength; i++) binary += String.fromCharCode(bytes[i]);
    return btoa(binary);
  }


  // Import javnog / privatnog ključa
  async importPublicKeyPem(pem: string): Promise<CryptoKey> {
    const base64 = this.pemToBase64(pem);
    const binaryDer = this.base64ToArrayBuffer(base64);
    return crypto.subtle.importKey(
      'spki',
      binaryDer,
      { name: 'RSA-OAEP', hash: 'SHA-256' },
      true,
      ['encrypt']
    );
  }

  async importPrivateKeyPem(pem: string): Promise<CryptoKey> {
    const base64 = this.pemToBase64(pem);
    const binaryDer = this.base64ToArrayBuffer(base64);
    return crypto.subtle.importKey(
      'pkcs8',
      binaryDer,
      { name: 'RSA-OAEP', hash: 'SHA-256' },
      true,
      ['decrypt']
    );
  }


  // Enkripcija i dekripcija 
  async encryptWithPublicKey(plaintext: string, publicKeyPem: string): Promise<string> {
    const key = await this.importPublicKeyPem(publicKeyPem);
    const encoded = new TextEncoder().encode(plaintext);
    const ciphertext = await crypto.subtle.encrypt({ name: 'RSA-OAEP' }, key, encoded);
    return this.arrayBufferToBase64(ciphertext);
  }

  async decryptWithPrivateKey(ciphertextBase64: string, privateKeyPem: string): Promise<string> {
    const key = await this.importPrivateKeyPem(privateKeyPem);
    const ciphertext = this.base64ToArrayBuffer(ciphertextBase64);
    const decrypted = await crypto.subtle.decrypt({ name: 'RSA-OAEP' }, key, ciphertext);
    return new TextDecoder().decode(decrypted);
  }


  // Pomoćna funkcija za testiranje
  async testEncryption(plaintext: string, publicPem: string, privatePem: string) {
    const encrypted = await this.encryptWithPublicKey(plaintext, publicPem);
    const decrypted = await this.decryptWithPrivateKey(encrypted, privatePem);
    console.log('Plaintext:', plaintext);
    console.log('Encrypted (base64):', encrypted);
    console.log('Decrypted:', decrypted);
  }
}
