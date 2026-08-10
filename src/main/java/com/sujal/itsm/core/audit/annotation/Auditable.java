package com.sujal.itsm.core.audit.annotation;

import com.sujal.itsm.core.audit.enums.AuditAction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    AuditAction action();
    String module();
    String entityType() default ""; // e.g., "Ticket", "Asset"
    String remarks() default "";
}