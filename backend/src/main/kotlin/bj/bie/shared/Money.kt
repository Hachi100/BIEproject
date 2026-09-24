package bj.bie.shared

import java.math.BigDecimal
import java.math.RoundingMode

/** Le franc CFA n'a pas de subdivision : les montants consolidés sont arrondis au franc. */
fun BigDecimal.toFcfa(): BigDecimal = setScale(0, RoundingMode.HALF_UP)

fun Iterable<BigDecimal>.sumFcfa(): BigDecimal = fold(BigDecimal.ZERO, BigDecimal::add).toFcfa()

/** Taux de TVA appliqué par le répertoire des prix : prix HT = prix TTC / 1,18. */
val VAT_RATE: BigDecimal = BigDecimal("0.18")

fun BigDecimal.excludingVat(): BigDecimal = divide(BigDecimal.ONE + VAT_RATE, 2, RoundingMode.HALF_UP)
