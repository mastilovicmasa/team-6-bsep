export interface CertificateRequest {
  issuerId: number;             // ID CA koji potpisuje novi sertifikat
  commonName: string;           // CN
  organization: string;         // O
  organizationalUnit: string;   // OU
  country: string;              // C
  email: string;                // E
  validityInDays: number;
}
