package bj.bie.planning

import bj.bie.shared.InvalidRequestException
import bj.bie.shared.InvalidStateException
import bj.bie.shared.SeparationOfDutiesException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PtabWorkflowTest {

    private val submitted = PtabActors(submittedBy = "preparateur")

    @Test
    fun `cycle complet brouillon - soumis - en revue - validé - approuvé`() {
        var status = PtabWorkflow.next(PtabStatus.DRAFT, PtabAction.SUBMIT, "preparateur", PtabActors(), null)
        assertEquals(PtabStatus.SUBMITTED, status)
        status = PtabWorkflow.next(status, PtabAction.START_REVIEW, "controleur", submitted, null)
        assertEquals(PtabStatus.UNDER_REVIEW, status)
        status = PtabWorkflow.next(status, PtabAction.VALIDATE, "controleur", submitted, null)
        assertEquals(PtabStatus.VALIDATED, status)
        status = PtabWorkflow.next(status, PtabAction.APPROVE, "ordonnateur", submitted.copy(validatedBy = "controleur"), null)
        assertEquals(PtabStatus.APPROVED, status)
    }

    @Test
    fun `le préparateur ne peut pas contrôler son propre PTAB`() {
        assertFailsWith<SeparationOfDutiesException> {
            PtabWorkflow.next(PtabStatus.SUBMITTED, PtabAction.START_REVIEW, "preparateur", submitted, null)
        }
        assertFailsWith<SeparationOfDutiesException> {
            PtabWorkflow.next(PtabStatus.UNDER_REVIEW, PtabAction.VALIDATE, "preparateur", submitted, null)
        }
    }

    @Test
    fun `l'approbateur doit être distinct du préparateur et du contrôleur`() {
        val actors = submitted.copy(validatedBy = "controleur")
        assertFailsWith<SeparationOfDutiesException> { PtabWorkflow.next(PtabStatus.VALIDATED, PtabAction.APPROVE, "controleur", actors, null) }
        assertFailsWith<SeparationOfDutiesException> { PtabWorkflow.next(PtabStatus.VALIDATED, PtabAction.APPROVE, "preparateur", actors, null) }
    }

    @Test
    fun `un renvoi doit être motivé et rend le PTAB de nouveau modifiable`() {
        assertFailsWith<InvalidRequestException> { PtabWorkflow.next(PtabStatus.UNDER_REVIEW, PtabAction.RETURN, "controleur", submitted, " ") }
        val status = PtabWorkflow.next(PtabStatus.UNDER_REVIEW, PtabAction.RETURN, "controleur", submitted, "Chiffrer la tâche 2")
        assertEquals(PtabStatus.RETURNED, status)
        assertEquals(true, status.editable)
        assertEquals(PtabStatus.SUBMITTED, PtabWorkflow.next(status, PtabAction.SUBMIT, "preparateur", submitted, null))
    }

    @Test
    fun `les transitions hors séquence sont refusées`() {
        assertFailsWith<InvalidStateException> { PtabWorkflow.next(PtabStatus.DRAFT, PtabAction.VALIDATE, "controleur", PtabActors(), null) }
        assertFailsWith<InvalidStateException> { PtabWorkflow.next(PtabStatus.APPROVED, PtabAction.SUBMIT, "preparateur", submitted, null) }
    }
}
