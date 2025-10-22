export interface CaUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  organization: string;
  hasCaCertificate: boolean; // backend će vratiti true/false
  certificateSerial?: string;
  revoked?: boolean;
}