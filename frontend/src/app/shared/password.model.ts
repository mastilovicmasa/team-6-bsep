export interface PasswordShare {
  id: number;
  userId: number;
  userEmail: string;
  encryptedPassword: string;
  sharedAt: string;
}

export interface PasswordEntry {
  id: number;
  siteName: string;
  username: string;
  ownerId: number;
  createdAt: string;
  encryptedPassword: string;
  shares: PasswordShare[];
}

export interface SharedPassword {
  shareId: number;
  siteName: string;
  username: string;
  ownerEmail: string;
  sharedAt: string;
  encryptedPassword: string;
}

// Request DTO-ovi
export interface PasswordCreateRequest {
  siteName: string;
  username: string;
  encryptedPassword: string;
}

export interface PasswordShareRequest {
  targetEmail: string;
  encryptedPassword: string;
}