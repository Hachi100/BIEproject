package bj.bie

import bj.bie.shared.TENANT_HEADER
import bj.bie.shared.USER_HEADER
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Parcours critique de recette (Partie 10 §39) sur le Ministère de la Santé :
 * PTAB → activité → tâches → costing contrôlé par le répertoire → enveloppe → workflow → audit.
 */
class PtabScenarioTest : IntegrationTest() {

    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var json: JsonMapper
    @Autowired lateinit var jdbc: JdbcClient

    private fun ResultActionsDsl.body(): JsonNode = json.readTree(andReturn().response.contentAsString)

    private fun postAs(user: String, tenant: String, url: String, body: Any) = mvc.post(url) {
        header(USER_HEADER, user)
        header(TENANT_HEADER, tenant)
        contentType = MediaType.APPLICATION_JSON
        content = json.writeValueAsString(body)
    }

    private fun getAs(user: String, tenant: String, url: String) = mvc.get(url) {
        header(USER_HEADER, user)
        header(TENANT_HEADER, tenant)
    }

    @Test
    fun `parcours complet d'un PTAB 2026 du ministère de la Santé`() {
        val ptab = postAs("preparateur", "MS", "/api/v1/ptabs", mapOf("fiscalYear" to 2026))
            .andExpect { status { isCreated() }; jsonPath("$.status") { value("DRAFT") } }.body()
        val ptabId = ptab["id"].asString()

        val activity = postAs(
            "preparateur", "MS", "/api/v1/ptabs/$ptabId/activities",
            mapOf(
                "label" to "Revue à mi-parcours du PTA 2026",
                "programCode" to "045",
                "responsibleUnit" to "DPAF",
                "expectedResult" to "Le taux d'exécution physique à fin juin est connu et analysé",
                "startDate" to "2026-06-01",
                "endDate" to "2026-07-31",
                "executionMode" to "DIRECT",
            ),
        ).andExpect { status { isCreated() }; jsonPath("$.code") { value("045-001") } }.body()

        val task = postAs(
            "preparateur", "MS", "/api/v1/activities/${activity["id"].asString()}/tasks",
            mapOf("label" to "Atelier de revue", "responsible" to "SPSE", "startDate" to "2026-07-01", "endDate" to "2026-07-03"),
        ).andExpect { status { isCreated() } }.body()
        val taskId = task["id"].asString()

        // Prix au-dessus de la borne supérieure du répertoire (BS = 1 250 FCFA) : ligne bloquante
        val blocking = postAs(
            "preparateur", "MS", "/api/v1/tasks/$taskId/costing-lines",
            mapOf(
                "description" to "Scotch adhésif transparent GM", "resourceNature" to "SUPPLIES",
                "quantity" to 40, "unit" to "U", "unitPrice" to 1500,
                "priceSource" to "PRICE_REPERTOIRE", "priceReferenceCode" to "6013 3321 251 1111",
            ),
        ).andExpect {
            status { isCreated() }
            jsonPath("$.controlStatus") { value("BLOCKING") }
            jsonPath("$.economicTitle") { value("GOODS_AND_SERVICES") }
            jsonPath("$.economicTitleOrigin") { value("INFERRED_FROM_PRICE_REFERENCE") }
        }.body()

        postAs("preparateur", "MS", "/api/v1/ptabs/$ptabId/transitions", mapOf("action" to "SUBMIT")).andExpect {
            status { isUnprocessableContent() }
            jsonPath("$.violations[0].code") { value("LIGNE_DE_COUT_BLOQUANTE") }
        }

        mvc.delete("/api/v1/costing-lines/${blocking["id"].asString()}?justification=Prix corrigé") {
            header(USER_HEADER, "preparateur"); header(TENANT_HEADER, "MS")
        }.andExpect { status { isNoContent() } }

        postAs(
            "preparateur", "MS", "/api/v1/tasks/$taskId/costing-lines",
            mapOf(
                "description" to "Scotch adhésif transparent GM", "resourceNature" to "SUPPLIES",
                "quantity" to 40, "unit" to "U", "unitPrice" to 1200,
                "priceSource" to "PRICE_REPERTOIRE", "priceReferenceCode" to "6013 3321 251 1111",
            ),
        ).andExpect { status { isCreated() }; jsonPath("$.controlStatus") { value("COMPLIANT") }; jsonPath("$.total") { value(48000) } }

        // Article hors répertoire : accepté avec avertissement d'homologation
        postAs(
            "preparateur", "MS", "/api/v1/tasks/$taskId/costing-lines",
            mapOf(
                "description" to "Location de salle équipée", "resourceNature" to "SERVICES",
                "quantity" to 1, "frequency" to 3, "unit" to "jour", "unitPrice" to 150000,
                "priceSource" to "MARKET_OBSERVATION", "economicTitle" to "GOODS_AND_SERVICES",
            ),
        ).andExpect { status { isCreated() }; jsonPath("$.homologationRequired") { value(true) } }

        getAs("preparateur", "MS", "/api/v1/ptabs/$ptabId/envelope-check").andExpect {
            status { isOk() }
            jsonPath("$[0].programCode") { value("045") }
            jsonPath("$[0].need") { value(498000) }
            jsonPath("$[0].overrun") { value(false) }
        }
        getAs("preparateur", "MS", "/api/v1/ptabs/$ptabId/submission-check").andExpect {
            jsonPath("$.submittable") { value(true) }
            jsonPath("$.warnings[0].code") { value("HOMOLOGATION_REQUISE") }
        }

        postAs("preparateur", "MS", "/api/v1/ptabs/$ptabId/transitions", mapOf("action" to "SUBMIT"))
            .andExpect { status { isOk() }; jsonPath("$.status") { value("SUBMITTED") } }
        // PTAB soumis : plus de modification possible
        postAs("preparateur", "MS", "/api/v1/tasks/$taskId/costing-lines", mapOf(
            "description" to "Eau minérale", "resourceNature" to "CATERING", "quantity" to 60, "unit" to "bouteille",
            "unitPrice" to 300, "priceSource" to "MARKET_OBSERVATION", "economicTitle" to "GOODS_AND_SERVICES",
        )).andExpect { status { isConflict() } }

        // Séparation des fonctions : le préparateur ne contrôle pas son propre PTAB
        postAs("preparateur", "MS", "/api/v1/ptabs/$ptabId/transitions", mapOf("action" to "START_REVIEW"))
            .andExpect { status { isForbidden() } }
        postAs("controleur", "MS", "/api/v1/ptabs/$ptabId/transitions", mapOf("action" to "START_REVIEW"))
            .andExpect { status { isOk() } }
        postAs("controleur", "MS", "/api/v1/ptabs/$ptabId/transitions", mapOf("action" to "VALIDATE"))
            .andExpect { status { isOk() }; jsonPath("$.status") { value("VALIDATED") } }
        postAs("controleur", "MS", "/api/v1/ptabs/$ptabId/transitions", mapOf("action" to "APPROVE"))
            .andExpect { status { isForbidden() } }
        postAs("ordonnateur", "MS", "/api/v1/ptabs/$ptabId/transitions", mapOf("action" to "APPROVE"))
            .andExpect { status { isOk() }; jsonPath("$.status") { value("APPROVED") } }

        getAs("auditeur", "MS", "/api/v1/ptabs/$ptabId").andExpect {
            jsonPath("$.total") { value(498000) }
            jsonPath("$.linesRequiringHomologation") { value(1) }
        }

        // Isolation des institutions : le PTAB du MS est invisible pour le MEF
        getAs("agent", "MEF", "/api/v1/ptabs/$ptabId").andExpect { status { isNotFound() } }

        // Toute l'histoire est tracée et la chaîne d'audit est intacte
        val events = getAs("auditeur", "MS", "/api/v1/audit/events?objectId=$ptabId").body()
        assertEquals(
            listOf("PTAB_CREATED", "PTAB_SUBMIT", "PTAB_START_REVIEW", "PTAB_VALIDATE", "PTAB_APPROVE"),
            (0 until events.size()).map { events[it]["action"].asString() }.reversed(),
        )
        getAs("auditeur", "MS", "/api/v1/audit/verification").andExpect {
            jsonPath("$.valid") { value(true) }
        }
    }

    @Test
    fun `un dépassement d'enveloppe bloque la soumission`() {
        val ptabId = postAs("preparateur", "MS", "/api/v1/ptabs", mapOf("fiscalYear" to 2026)).body()["id"].asString()
        val activityId = postAs(
            "preparateur", "MS", "/api/v1/ptabs/$ptabId/activities",
            mapOf("label" to "Construction", "programCode" to "047", "responsibleUnit" to "DIEM",
                "startDate" to "2026-02-01", "endDate" to "2026-11-30", "executionMode" to "INDIRECT"),
        ).body()["id"].asString()
        val taskId = postAs(
            "preparateur", "MS", "/api/v1/activities/$activityId/tasks",
            mapOf("label" to "Travaux", "responsible" to "DIEM", "startDate" to "2026-03-01", "endDate" to "2026-10-31"),
        ).body()["id"].asString()
        postAs(
            "preparateur", "MS", "/api/v1/tasks/$taskId/costing-lines",
            mapOf("description" to "Centre hospitalier", "resourceNature" to "WORKS", "quantity" to 1, "unit" to "ouvrage",
                "unitPrice" to "900000000000", "priceSource" to "TECHNICAL_ESTIMATE", "economicTitle" to "CAPITAL",
                "fundingSource" to "LOAN"),
        ).andExpect { status { isCreated() } }

        postAs("preparateur", "MS", "/api/v1/ptabs/$ptabId/transitions", mapOf("action" to "SUBMIT")).andExpect {
            status { isUnprocessableContent() }
            jsonPath("$.violations[?(@.code == 'DEPASSEMENT_ENVELOPPE')]") { exists() }
        }
    }

    @Test
    fun `le journal d'audit refuse toute modification`() {
        postAs("preparateur", "MEF", "/api/v1/ptabs", mapOf("fiscalYear" to 2027)).andExpect { status { isCreated() } }
        val error = assertFailsWith<Exception> {
            jdbc.sql("UPDATE audit.audit_event SET actor = 'pirate'").update()
        }
        val rootMessage = generateSequence<Throwable>(error) { it.cause }.last().message.orEmpty()
        assertTrue(rootMessage.contains("Le journal d'audit est inaltérable : UPDATE refusé"), rootMessage)
        assertFailsWith<Exception> { jdbc.sql("DELETE FROM audit.audit_event").update() }
    }

    @Test
    fun `les requêtes sans identité sont refusées`() {
        mvc.post("/api/v1/ptabs") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"fiscalYear": 2026}"""
        }.andExpect { status { isBadRequest() } }
        getAs("agent", "INCONNU", "/api/v1/ptabs").andExpect { status { isBadRequest() } }
    }
}
