# Cashflow (Praktikum 5 PBO 2026)

Spring Boot 4.1.2 · Vaadin 25.3.4 · JDBC PostgreSQL · Clean Architecture

## Langkah awal
1. `python set_username.py <username-anda>` (mengganti placeholder `ifs25032`; sekali saja).
1. Buat database: `CREATE DATABASE pbo2026_cashflow;`
2. Salin `.env.example` menjadi `.env` lalu sesuaikan `DB_*` / `ROOT_*`.
3. Maven Wrapper sudah ikut (`mvnw`, `mvnw.cmd`, `.mvn/`); Maven diunduh otomatis saat pertama dijalankan.
4. Jalankan: `.\mvnw.cmd spring-boot:run` → http://localhost:8080 (admin: ROOT_USERNAME / ROOT_PASSWORD).

## Build & uji
- `.\mvnw.cmd clean verify` : unit test (InMemory + H2) dan cek Jacoco (`-Djacoco.minimum=0.90` untuk melonggarkan).
- `.\mvnw.cmd clean package -Pproduction` lalu `java -jar target\<artifact>-0.0.1-SNAPSHOT.jar`.

## Rute
| Rute | Akses |
|---|---|
| `/login`, `/register`, `/uploads/**`, `/icons/**` | Publik |
| `/`, `/profile` | USER & ADMIN (wajib login) |
| `/users`, `/user-transactions` | ADMIN saja |
