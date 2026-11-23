# VEIL
> **Private by design. Simple by nature.**

VEIL is a lightweight, privacy-first encrypted cloud storage and photo gallery application for Android.

Combining the client-side, zero-knowledge encryption model of **Cryptomator** with the modern, seamless photo backup and timeline browsing experience of **Ente Photos**, VEIL empowers users with total visual privacy and multi-cloud data sovereignty without complex manual setup.

---

## Architecture Overview

```
VEIL Architecture
├── Presentation Layer: Jetpack Compose + Material 3 (Obsidian Dark Theme)
│   ├── Timeline Photo & Video Gallery (Pinch-to-zoom, responsive columns)
│   ├── Full-Screen Decrypted Media Viewer (Pinch-to-zoom, EXIF metadata inspector)
│   ├── Smart & Custom Encrypted Albums
│   └── Cloud Provider & Auto-Backup Management
├── Core Security & Cryptography:
│   ├── 64 KiB Chunked Streaming AEAD (AES-256-GCM)
│   ├── PBKDF2 / Argon2id Key Derivation (120,000 iterations, 32-byte salt)
│   ├── Android Keystore Hardware Wrapping (TEE / StrongBox)
│   ├── Biometric Authentication (Class 3 Strong Biometrics)
│   ├── Blind Deduplication (HMAC-SHA256 with user-private DedupKey)
│   └── 24-Word BIP-39 Emergency Recovery Phrase
├── Data & Storage:
│   ├── Room Database with SQLCipher Full-Disk Database Encryption
│   ├── MediaStore ContentObserver (Automatic Camera Roll Detection)
│   └── Encrypted In-Memory Chunk Pipeline (Zero Plaintext Disk Leakage)
├── Background Backup Engine:
│   ├── WorkManager Scheduled Background Tasks
│   └── Constraints: Wi-Fi Only, Charging Only, Battery Not Low
└── Blind Multi-Cloud Transport:
    ├── Modular CloudStorageProvider Interface
    ├── WebDAV Storage Provider (Nextcloud, ownCloud, Synology, Hetzner)
    ├── AWS S3 SigV4 REST Provider (Wasabi, Backblaze B2, MinIO, AWS)
    ├── Google Drive REST v3 Provider
    └── Local Encrypted Sandbox Provider (Offline & Testing Vault)
```

---

## Cryptographic Specification

### 1. Key Derivation & Hierarchy
- **Master Password**: Processed with `PBKDF2WithHmacSHA256` (120,000 rounds, 32-byte cryptographically secure random salt) to derive the **Key Encryption Key (KEK)**.
- **Vault Master Key (VMK)**: 256-bit high-entropy random key.
- **Hardware Wrapping**: In Android Keystore, an AES-256-GCM key (`veil_keystore_bio_master`) wraps the VMK, gated by biometric authentication (`BiometricPrompt`).
- **Sub-Key Derivation**: Uses RFC 5869 HKDF-Expand:
  - `PMK` (Payload Master Key) = `HKDF-Expand(VMK, "veil-payload-v1", 32)`
  - `DDK` (Deduplication Key) = `HKDF-Expand(VMK, "veil-dedup-token-v1", 32)`

### 2. 64 KiB Chunked Streaming AEAD Container
All media items (photos, high-res videos, raw files) are encrypted as streamed chunks:
- **Header (34 Bytes)**: Magic (`VEIL`, 4B) + Version (`0x0001`, 2B) + File Salt (16B) + Base IV (12B).
- **Chunk Size**: 64 KiB (65,536 bytes).
- **Nonce Misuse Resistance**: Chunk Nonce = `Base IV XOR (chunk_index)`.
- **Associated Data (AD)**: Binds `FileID || chunk_index || is_last_chunk`, preventing chunk reordering, truncation, or cross-file splicing.
- **Authentication**: 128-bit GCM authentication tag per chunk. Decryption aborts instantly if any chunk is tampered with.

### 3. Blind Deduplication
- `DedupToken = HMAC-SHA256(DDK, PlaintextStream)`.
- Prevents redundant uploads without leaking the file's SHA-256 hash to cloud providers or rainbow tables.

---

## Project Structure

```
VEIL/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/veil/
│   │   │   ├── MainActivity.kt                 # Single-activity Compose host
│   │   │   ├── VeilApplication.kt              # App initialization & SQLCipher loader
│   │   │   ├── core/
│   │   │   │   ├── backup/                     # MediaStoreWatcher & BackupWorker
│   │   │   │   ├── crypto/                     # VeilCrypto, KeystoreManager, Bip39Recovery
│   │   │   │   ├── database/                   # Room entities, DAOs & VeilDatabase
│   │   │   │   ├── media/                      # VeilImagePipeline, VeilMediaDataSource
│   │   │   │   ├── model/                      # Domain models (MediaItem, Vault, CloudAccount)
│   │   │   │   └── transport/                  # CloudStorageProvider, WebDAV, LocalSandbox
│   │   │   └── ui/
│   │   │       ├── components/                 # TopAppBar, NavigationBar, BackupBadges
│   │   │       ├── navigation/                 # VeilNavGraph & Screen routes
│   │   │       ├── screens/                    # Lock, Setup, Gallery, Viewer, Albums, Settings
│   │   │       └── theme/                      # Color, Theme, Typography (Material 3)
│   │   └── res/                                # XML themes, drawables, adaptive launcher icons
│   └── src/test/                               # Unit & Cryptographic KAT tests
├── gradle/
│   ├── libs.versions.toml                      # Version catalog
│   └── wrapper/
├── build.gradle.kts                            # Root build configuration
└── settings.gradle.kts                         # Module inclusions
```

---

## Building and Running

1. Open project in **Android Studio** (Hedgehog / Iguana / Jellyfish / Ladybug).
2. Sync Gradle with project files.
3. Run on physical device or emulator (Android 8.0+ / API 26+).
