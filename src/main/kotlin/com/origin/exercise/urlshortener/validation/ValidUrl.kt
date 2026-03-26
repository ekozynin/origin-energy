package com.origin.exercise.urlshortener.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.net.URI
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UrlValidator::class])
annotation class ValidUrl(
    val message: String = "URL must be a valid http or https URL",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class UrlValidator : ConstraintValidator<ValidUrl, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) return false
        return try {
            val uri = URI(value)
            uri.scheme in listOf("http", "https") && !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }
}
