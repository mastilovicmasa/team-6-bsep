export interface PasswordShare {
  id: number;
  userId: number;
  encryptedPassword: string;
  sharedAt: string;
}

export interface PasswordEntry {
  id: number;
  siteName: string;
  username: string;
  ownerId: number;
  createdAt: string;
  shares: PasswordShare[];
}

// Request DTO-ovi
export interface PasswordCreateRequest {
  siteName: string;
  username: string;
  encryptedPassword: string;
}

export interface PasswordShareRequest {
  targetUserId: number;
  encryptedPassword: string;
}