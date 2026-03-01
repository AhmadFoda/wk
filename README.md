# Credentials App (Proxy + Backend + Frontend + Integration Tests)

This repository contains a small system for managing organisation credentials behind an authenticated proxy.

## Components

- **proxy/**: Spring Boot proxy that protects `/api/**` and forwards requests to the backend
- **backend/**: Spring Boot REST API (credentials + organisations + user bootstrap)
- **frontend/**: Vite + React UI 
- **integration-test/**: Playwright acceptance tests (assignment criteria)

## Prerequisites

### To run the application stack

- Docker + Docker Compose
- docker compose up --build
- front end: http://localhost:3000

### To run the integration tests (Playwright) on your machine

- Node.js 18+ (recommended)
- npm (comes with Node)
- cd integration-test
- npm ci
- E2E_TOKEN="**" npx playwright test

## Quick start (run the stack)

From repo root:

```bash
docker compose up --build