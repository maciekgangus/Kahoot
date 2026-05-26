package pl.agh.zti.quiz.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AuditAspect — logs all service layer method invocations.
 * Per component diagram: AOP intercepts service layer calls.
 * Pointcut: all methods in pl.agh.zti.quiz.service package.
 */
@Aspect
@Component
@Slf4j
public class AuditAspect {

    @Pointcut("execution(* pl.agh.zti.quiz.service.*.*(..))")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object audit(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        String args = Arrays.toString(pjp.getArgs());
        long start = System.currentTimeMillis();

        log.info("[AUDIT] >> {} args={}", method, args);
        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[AUDIT] << {} OK ({}ms)", method, elapsed);
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[AUDIT] << {} EXCEPTION {} ({}ms)", method, ex.getMessage(), elapsed);
            throw ex;
        }
    }
}
