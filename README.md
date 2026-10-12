# PKI Platform & Secure Password Manager

Academic team project developed for the *Security in Electronic Business Systems* course at the Faculty of Technical Sciences, University of Novi Sad.

The application combines a Public Key Infrastructure (PKI) platform for managing X.509 certificates with a secure password manager for end users.

## Overview

The platform supports certificate lifecycle management and role-based access for administrators, Certificate Authority (CA) users, and end users.

The system includes:

- User registration, email-based account activation, login, and password recovery
- Role-based authorization for administrators, CA users, and end users
- Creation and management of Root, Intermediate, and End-Entity certificates
- Certificate Signing Request (CSR) upload and processing
- Certificate download and revocation workflows
- Secure storage and sharing of password entries
- JWT-based authentication and protected backend endpoints

## Secure Password Manager

The password manager is available to end users with an issued certificate and key pair.

Users can create password entries containing a website name, username, and password. Sensitive passwords are encrypted before storage. Decryption is performed locally in the client application using the user's private key, which is not stored on the server.

Password entries can also be shared with other users. Before sharing, a password is encrypted for the recipient using the recipient's public key, so every authorized user can decrypt only their own encrypted copy.

## Technology Stack

### Backend

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT authentication
- JavaMail
- Bouncy Castle cryptography library
- Maven

### Frontend

- Angular 20
- TypeScript
- HTML and CSS
