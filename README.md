# 🚀 OfflineUPI-Mesh

### Offline Digital Payment System using Mesh Networking

**OfflineUPI-Mesh** is a backend-focused simulation of an **offline UPI payment system** designed to demonstrate how digital payments could be initiated and transported between devices **without requiring continuous internet connectivity**.

The system uses a simulated **phone-to-phone mesh network**, cryptographic protection, packet deduplication, transaction settlement, optimistic locking, and a Spring Boot backend.

> ⚠️ **Educational Project:** This is a simulation/prototype and is **not connected to the real UPI/NPCI infrastructure**.

---

## 🌐 Live Demo

### ▶️ Try the deployed application

**[Open OfflineUPI-Mesh](https://offlinemeshpay.onrender.com)**

The application is deployed using **Render** and can be accessed from a browser or mobile device.

---

## 🎯 Problem Statement

Traditional digital payment systems generally depend on network connectivity to communicate with a central payment infrastructure.

But imagine situations such as:

* 📵 No internet connectivity
* 🏔️ Remote areas with poor network coverage
* 🚨 Temporary network outages
* 🚆 Underground or isolated environments
* 🌐 Intermittent connectivity

The goal of this project is to explore:

> **How can a payment instruction move between devices when the internet is temporarily unavailable?**

OfflineUPI-Mesh addresses this problem by simulating a **store-and-forward mesh network**.

---

# 🏗️ System Architecture

```text
                    ┌───────────────────────┐
                    │       User Device     │
                    │                       │
                    │  Create Payment       │
                    │  Instruction          │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │   Hybrid Encryption   │
                    │                       │
                    │ AES-GCM Encryption    │
                    │ RSA-OAEP Key Wrapping │
                    └───────────┬───────────┘
                                │
                                ▼
                    ┌───────────────────────┐
                    │      Mesh Packet      │
                    │                       │
                    │ Packet ID             │
                    │ TTL                   │
                    │ Ciphertext            │
                    │ Created At            │
                    └───────────┬───────────┘
                                │
                       Device-to-Device
                         Forwarding
                                │
              ┌─────────────────┼─────────────────┐
              ▼                 ▼                 ▼
        ┌──────────┐      ┌──────────┐      ┌──────────┐
        │ Device B │ ───► │ Device C │ ───► │ Device D │
        └──────────┘      └──────────┘      └────┬─────┘
                                                  │
                                                  ▼
                                    ┌──────────────────────┐
                                    │    Spring Boot       │
                                    │      Backend         │
                                    └──────────┬───────────┘
                                               │
                           ┌───────────────────┼──────────────────┐
                           ▼                   ▼                  ▼
                    ┌────────────┐      ┌────────────┐    ┌────────────┐
                    │ Decryption │      │ Deduplication│    │ Settlement │
                    └────────────┘      └────────────┘    └────────────┘
                                               │
                                               ▼
                                      ┌─────────────────┐
                                      │     Database    │
                                      │                 │
                                      │ Accounts        │
                                      │ Transactions    │
                                      └─────────────────┘
```

---

# 🔄 How the Payment Flow Works

```text
1. Sender creates payment
          ↓
2. Payment instruction is created
          ↓
3. Payment data is encrypted
          ↓
4. Encrypted data is placed inside MeshPacket
          ↓
5. Packet receives a TTL
          ↓
6. Nearby devices can forward the packet
          ↓
7. Packet eventually reaches backend
          ↓
8. Backend decrypts the payment
          ↓
9. Transaction hash is checked
          ↓
10. Duplicate transaction is rejected
          ↓
11. Account balance is validated
          ↓
12. Transaction is settled/rejected
          ↓
13. Database state is updated
```

---

# 🔐 Security Design

Security is one of the important parts of this project.

## Hybrid Encryption

The project uses a combination of:

### AES-GCM

Used for encrypting the actual payment payload.

```text
Payment Data
     ↓
AES-GCM
     ↓
Encrypted Payload
```

AES-GCM provides:

* Confidentiality
* Integrity
* Authentication

---

### RSA-OAEP

RSA is used for securely protecting/exchanging the AES key.

```text
AES Secret Key
      ↓
RSA-OAEP
      ↓
Encrypted AES Key
```

The server maintains an RSA key pair:

```text
                 RSA Key Pair
                      │
            ┌─────────┴─────────┐
            ▼                   ▼
       Public Key           Private Key
            │                   │
            │                   │
     Can be shared        Kept securely
            │                   │
            ▼                   ▼
      Encryption            Decryption
```

The public key can be distributed, while the private key remains on the server.

---

# 🛡️ Transaction Deduplication

Offline systems introduce an important problem:

> What happens if the same payment packet reaches the server multiple times?

The project uses a **SHA-256 transaction hash** to identify duplicate payment packets.

```text
Payment Packet
      │
      ▼
    SHA-256
      │
      ▼
 Packet Hash
      │
      ▼
Database Lookup
      │
 ┌────┴────┐
 │         │
Exists    New
 │         │
 ▼         ▼
Reject    Process
```

The transaction hash is stored with a unique constraint to prevent duplicate settlement.

---

# 💰 Account & Transaction Management

The system maintains accounts using a unique VPA.

Example:

```text
alice@offline
bob@offline
```

An account contains information such as:

```text
Account
 ├── id
 ├── vpa
 ├── balance
 └── version
```

Transactions maintain:

```text
Transaction
 ├── id
 ├── packetHash
 ├── status
 ├── createdAt
 └── ...
```

Transaction status can be:

```text
SETTLED
REJECTED
```

---

# 🔒 Optimistic Locking

The `Account` entity uses JPA optimistic locking with:

```java
@Version
```

This helps prevent inconsistent balance updates when multiple transactions attempt to modify an account concurrently.

Conceptually:

```text
Transaction A ──► Account Balance
                       │
                       ▼
                   Version 5
                       │
Transaction B ──► Account Balance
                       │
                       ▼
                  Version Conflict
```

This is important for a payment system because **concurrent balance modifications must be handled safely**.

---

# 📦 Mesh Packet

A mesh packet contains information required to transport the payment through the simulated network.

Conceptually:

```text
MeshPacket
 ├── packetId
 ├── TTL
 ├── createdAt
 └── ciphertext
```

### TTL — Time To Live

TTL prevents a packet from being forwarded indefinitely.

```text
Packet
 ↓ TTL = 5
Device A
 ↓ TTL = 4
Device B
 ↓ TTL = 3
Device C
 ↓ TTL = 2
Device D
 ↓ TTL = 1
Device E
 ↓ TTL = 0
STOP
```

---

# 🧩 Main Components

## Account

Represents a user's payment account.

Responsible for:

* VPA
* Balance
* Versioning
* Concurrent balance protection

---

## PaymentInstruction

Represents the actual payment instruction.

Contains concepts such as:

* Sender VPA
* Receiver VPA
* Amount
* PIN hash
* Nonce
* Signed timestamp

---

## MeshPacket

Represents the encrypted packet transported through the simulated mesh network.

---

## HybridCryptoService

Responsible for:

* AES-GCM encryption
* RSA-OAEP encryption
* Decryption
* Cryptographic processing

---

## ServerKeyHolder

Generates and maintains the server RSA key pair.

```text
Application Startup
       │
       ▼
Generate RSA-2048 Key Pair
       │
 ┌─────┴─────┐
 ▼           ▼
Public      Private
Key         Key
```

---

## MeshSimulator

Simulates the movement of packets between devices.

Example:

```text
Phone A
   ↓
Phone B
   ↓
Phone C
   ↓
Phone D
   ↓
Backend
```

This allows the project to demonstrate the core concept without requiring real Bluetooth hardware.

---

# 🛠️ Technology Stack

| Technology        | Purpose                   |
| ----------------- | ------------------------- |
| Java              | Backend programming       |
| Spring Boot       | Application framework     |
| Spring Data JPA   | Database persistence      |
| Hibernate         | ORM                       |
| Spring MVC        | REST/Web layer            |
| Thymeleaf         | Dashboard UI              |
| H2 / SQL Database | Development persistence   |
| AES-GCM           | Payload encryption        |
| RSA-OAEP          | Secure key encryption     |
| SHA-256           | Transaction deduplication |
| Maven             | Build management          |
| Docker            | Containerization          |
| Git               | Version control           |
| GitHub            | Source code hosting       |
| Render            | Cloud deployment          |

---

# 📁 Project Structure

```text
src
└── main
    ├── java
    │   └── com.example.OfflineUPI_Mesh
    │       │
    │       ├── controller
    │       │
    │       ├── service
    │       │
    │       ├── entity
    │       │
    │       ├── repository
    │       │
    │       ├── crypto
    │       │
    │       ├── mesh
    │       │
    │       └── config
    │
    └── resources
        ├── templates
        │   └── dashboard.html
        │
        └── application.properties
```

---

# 🖥️ Dashboard

The project includes a web-based dashboard for interacting with the system.

The dashboard provides a visual interface for demonstrating the offline payment flow.

```text
┌──────────────────────────────────────────────┐
│             OfflineUPI-Mesh                  │
├──────────────────────────────────────────────┤
│                                              │
│  Sender        Receiver        Amount        │
│  ─────────     ─────────       ───────       │
│  alice@...     bob@...         ₹500          │
│                                              │
│              [ Send Payment ]                │
│                                              │
├──────────────────────────────────────────────┤
│              Transaction History             │
│                                              │
│  Status       Amount       Timestamp         │
│  SETTLED      ₹500         ...               │
│                                              │
└──────────────────────────────────────────────┘
```

---

# 🐳 Docker

The application can also be containerized using Docker.

Example build:

```bash
./mvnw clean package
```

Then build the Docker image:

```bash
docker build -t offline-upi-mesh .
```

Run:

```bash
docker run -p 8080:8080 offline-upi-mesh
```

---

# 🚀 Running Locally

## 1. Clone the repository

```bash
git clone https://github.com/AashishKumarOfficial/OfflineUPI-Mesh.git
```

```bash
cd OfflineUPI-Mesh
```

## 2. Build the project

Windows:

```bash
mvnw.cmd clean package
```

Linux/macOS:

```bash
./mvnw clean package
```

## 3. Run the application

```bash
mvnw.cmd spring-boot:run
```

Or:

```bash
java -jar target/OfflineUPI-Mesh-0.0.1-SNAPSHOT.jar
```

## 4. Open the application

```text
http://localhost:8080
```

---

# ☁️ Deployment

The application is deployed on **Render**.

### Production URL

**https://offlinemeshpay.onrender.com**

Deployment flow:

```text
GitHub
   │
   ▼
Render
   │
   ▼
Docker Build
   │
   ▼
Spring Boot Application
   │
   ▼
Public Internet
```

---

# 🧪 Example Payment Scenario

Suppose:

```text
Sender:
alice@offline

Receiver:
bob@offline

Amount:
₹500
```

The flow becomes:

```text
Alice
 │
 │ Create ₹500 payment
 ▼
PaymentInstruction
 │
 │ AES-GCM
 ▼
Encrypted Payload
 │
 │ RSA-OAEP protected key
 ▼
MeshPacket
 │
 ▼
Phone B
 │
 ▼
Phone C
 │
 ▼
Backend
 │
 ├── Decrypt
 │
 ├── Verify
 │
 ├── Check duplicate
 │
 ├── Check balance
 │
 └── Settle
       │
       ▼
     ₹500
 Alice ─────────► Bob
```

---

# 🔍 Key Engineering Concepts Demonstrated

This project focuses on practical backend engineering concepts:

* ✅ Spring Boot architecture
* ✅ REST APIs
* ✅ Spring Data JPA
* ✅ Hibernate
* ✅ Database transactions
* ✅ Optimistic locking
* ✅ Cryptography
* ✅ Hybrid encryption
* ✅ AES-GCM
* ✅ RSA-OAEP
* ✅ SHA-256 hashing
* ✅ Idempotency / deduplication
* ✅ TTL-based packet propagation
* ✅ Mesh networking simulation
* ✅ Concurrent transaction handling
* ✅ Docker
* ✅ Cloud deployment
* ✅ Thymeleaf
* ✅ Maven
* ✅ Git/GitHub

---

# 🚧 Future Improvements

The current implementation is a simulation. Possible future improvements include:

* [ ] Real Bluetooth Low Energy communication
* [ ] Actual phone-to-phone packet transfer
* [ ] Digital signatures for payment instructions
* [ ] Secure device identity
* [ ] Replay-attack protection
* [ ] Redis-based distributed deduplication
* [ ] Kafka-based transaction/event processing
* [ ] PostgreSQL production database
* [ ] Prometheus monitoring
* [ ] Grafana dashboards
* [ ] Kubernetes deployment
* [ ] Rate limiting
* [ ] Better offline conflict resolution
* [ ] Multi-device synchronization
* [ ] Production-grade key management

---

# 📊 Project Goal

The primary goal of OfflineUPI-Mesh is to explore the engineering challenges involved in building a **secure, fault-tolerant payment system under intermittent connectivity**.

The project focuses on the backend problems that appear when:

```text
Network ≠ Always Available
```

and therefore explores:

```text
Offline
  ↓
Store
  ↓
Forward
  ↓
Verify
  ↓
Deduplicate
  ↓
Settle
```

---

# 👨‍💻 Author

### Aashish Kumar

**B.Tech Computer Science Engineering**

Interested in:

* Java Backend Development
* Spring Boot
* Distributed Systems
* Microservices
* System Design
* Kafka
* Docker
* Cloud Deployment
* Secure Backend Systems

### Profiles

* 💼 **LinkedIn:** [Aashish Kumar](https://linkedin.com/in/aashish-kumar-784ab226b)
* 🐙 **GitHub:** [AashishKumarOfficial](https://github.com/AashishKumarOfficial)
* 🧩 **LeetCode:** [Aashish_Kumar23](https://leetcode.com/u/Aashish_Kumar23/)

---

# ⭐ Support

If you find this project interesting, consider giving the repository a ⭐.

It helps support further development of the project.

---

## 📜 Disclaimer

This project is created for **educational and research purposes**.

It is a simulation of an offline digital payment architecture and is **not an official UPI implementation**. It does not connect to NPCI, banks, or real UPI payment infrastructure.
