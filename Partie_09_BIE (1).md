---
title: "Partie 9 — Cybersécurité, Zero Trust, infrastructure souveraine, DevSecOps et résilience"
project: "Budget Intelligence Engine (BIE)"
version: "1.0"
status: "Référence harmonisée"
date: "2026-09-24"
language: "fr"
---

# Partie 9 — Cybersécurité, Zero Trust, infrastructure souveraine, DevSecOps et résilience

## Sécuriser et opérer BIE comme une plateforme critique

## 1. Objet

Cette partie couvre :

- IAM ;
- Zero Trust ;
- autorisation ;
- séparation des fonctions ;
- cryptographie ;
- secrets ;
- audit ;
- sécurité applicative ;
- Kubernetes ;
- DevSecOps ;
- PRA/PCA ;
- souveraineté ;
- scalabilité.

## 2. Doctrine

```text
NEVER TRUST IMPLICITLY
 ↓
VERIFY IDENTITY
 ↓
VERIFY CONTEXT
 ↓
AUTHORIZE
 ↓
LEAST PRIVILEGE
 ↓
MONITOR
 ↓
AUDIT
```

## 3. Référentiels

BIE devra s'aligner notamment sur :

```text
PSSIE Bénin
Code du numérique
APDP requirements
NIST CSF
NIST Zero Trust
OWASP
```

## 4. Zero Trust

Aucune confiance implicite liée :

- au réseau ;
- au ministère ;
- au terminal ;
- au service.

## 5. Human & Workload Identity

Zero Trust s'applique aux :

```text
users
services
workers
agents
pipelines
```

## 6. Keycloak

Fonctions :

- SSO ;
- OIDC ;
- MFA ;
- fédération ;
- sessions ;
- identity lifecycle.

## 7. MFA

Obligatoire pour :

- administrateurs ;
- validateurs ;
- accès distant ;
- opérations critiques.

## 8. Passkeys / WebAuthn

À privilégier pour les rôles sensibles.

## 9. Account Lifecycle

```text
CREATE
ACTIVATE
ASSIGN
MOVE
SUSPEND
REVOKE
```

## 10. Authorization

Combiner :

```text
RBAC
+
ABAC
+
OPA
```

## 11. Separation of Duties

```text
PREPARE
≠
CONTROL
≠
APPROVE
```

## 12. Privileged Access

- comptes dédiés ;
- durée limitée ;
- justification ;
- audit.

## 13. Service Identity

Chaque service/agent a sa propre identité.

## 14. Data Classification

Classes indicatives :

```text
PUBLIC
INTERNAL
CONFIDENTIAL
RESTRICTED
CRITICAL
```

## 15. Data Handling

La classe détermine :

```text
storage
encryption
export
AI routing
retention
```

## 16. Encryption

- TLS ;
- chiffrement au repos ;
- KMS/HSM ;
- mTLS pour flux sensibles.

## 17. Crypto Agility

Le système doit permettre le remplacement des algorithmes sans refonte générale.

## 18. Secrets

Aucun secret :

```text
in Git
in source code
in Dockerfile
```

Utiliser Vault/KMS/secret manager.

## 19. Kubernetes

Plateforme d'orchestration cible avec politique de versions supportées.

## 20. Cluster Separation

Séparer autant que nécessaire :

```text
production
non-production
AI
data
```

## 21. Network Security

Cilium ou équivalent.

Principe :

```text
DENY BY DEFAULT
```

## 22. East-West Control

Chaque flux interservice doit être explicitement autorisé.

## 23. Egress Control

Les services n'ont pas tous accès à Internet.

## 24. Runtime Security

Falco ou équivalent pour détecter :

- shell inhabituel ;
- accès fichier sensible ;
- réseau anormal.

## 25. Container Hardening

```text
runAsNonRoot
readOnlyRootFilesystem
dropCapabilities
```

## 26. Admission Policies

Avant déploiement :

```text
signed image?
approved registry?
non-root?
policy compliant?
```

## 27. Private Registry

Images approuvées par digest.

## 28. Supply Chain

Pipeline :

```text
SOURCE
 ↓
BUILD
 ↓
TEST
 ↓
SBOM
 ↓
SCAN
 ↓
SIGN
 ↓
VERIFY
 ↓
DEPLOY
```

## 29. DevSecOps

Scans :

- SAST ;
- DAST ;
- SCA ;
- secrets ;
- IaC ;
- images.

## 30. GitOps

Argo CD ou équivalent.

La production correspond à un état versionné.

## 31. Audit

Quatre couches :

```text
Business
Security
System
AI
```

## 32. Immutable Audit

Les événements critiques sont protégés contre altération/suppression.

## 33. SIEM

Centraliser les logs de :

- Keycloak ;
- OPA ;
- applications ;
- Kubernetes ;
- WAF ;
- Vault ;
- bases ;
- agents IA.

## 34. Incident Response

```text
DETECT
TRIAGE
CONTAIN
ERADICATE
RECOVER
LEARN
```

## 35. Vulnerability Management

```text
discover
assess
prioritize
remediate
verify
```

## 36. Asset Inventory

Chaque service possède :

```text
owner
version
environment
criticality
```

## 37. API Security

Obligations :

- authn ;
- authz ;
- validation ;
- rate limiting ;
- object-level authorization.

## 38. File Upload

Pipeline :

```text
upload
 ↓
size/type
 ↓
malware scan
 ↓
quarantine
 ↓
processing
```

## 39. Database Security

- non-public ;
- TLS ;
- minimum privileges ;
- backups ;
- audit.

## 40. Tenant Isolation

Appliquée :

```text
application
policy
database
search
analytics
storage
```

## 41. Backup

- chiffré ;
- versionné ;
- copie isolée ;
- restauration testée.

## 42. PITR

PostgreSQL doit permettre Point-in-Time Recovery.

## 43. PCA/PRA

BIE doit formaliser :

```text
Business Impact Analysis
RTO
RPO
Backup
Failover
Recovery
```

## 44. HA

Architecture nationale :

- multi-replica ;
- réplication données ;
- multi-zone si disponible.

## 45. Disaster Recovery

Prévoir scénario :

```text
Primary unavailable
→ DR
```

et le tester.

## 46. SLO

Exemples :

```text
availability
latency
error rate
```

## 47. Observability

```text
OpenTelemetry
Prometheus
Grafana
Loki
Tempo
```

## 48. Scalability

- HPA ;
- node pools ;
- GPU pools ;
- resource limits ;
- topology spread.

## 49. Deployment Models

```text
CLOUD
ON-PREM
HYBRID
```

## 50. Sovereignty

Dimensions :

```text
location
jurisdiction
operator access
keys
portability
exit
```

## 51. Hybrid Target

À maturité :

```text
National Core
+
Elastic Cloud
+
DR
```

## 52. Cloud Exit

Prévoir l'export :

```text
PostgreSQL
Parquet
Iceberg
S3
OCI
Kubernetes configs
```

## 53. Privacy

BIE applique :

- minimisation ;
- finalité ;
- rétention ;
- sécurité ;
- droits.

## 54. Security Tests

Avant production :

```text
architecture review
SAST/SCA
penetration test
restore test
load test
```

## 55. Requirements

| ID | Exigence |
|---|---|
| SEC-001 | Zero Trust |
| SEC-002 | IAM/MFA |
| SEC-003 | RBAC/ABAC/OPA |
| SEC-004 | SoD |
| SEC-005 | chiffrement |
| SEC-006 | secrets |
| SEC-007 | audit immutable |
| SEC-008 | DevSecOps |
| SEC-009 | backup/PRA |
| SEC-010 | souveraineté/portabilité |

## 56. Conclusion

La sécurité de BIE doit être démontrable par :

```text
controls
tests
audit logs
restore tests
penetration tests
incident exercises
```

et non par une simple déclaration de conformité.
