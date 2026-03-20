package com.chat.security.guard;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.*;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('member')")
public @interface MemberGuard { }
