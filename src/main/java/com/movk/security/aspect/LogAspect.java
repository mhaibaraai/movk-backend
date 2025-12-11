/*
 * @Author yixuanmiao
 * @Date 2025/12/11
 */

package com.movk.security.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movk.base.filter.TraceIdFilter;
import com.movk.common.enums.BusinessStatus;
import com.movk.entity.OperateLog;
import com.movk.security.annotation.Log;
import com.movk.security.model.LoginUser;
import com.movk.service.OperateLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 操作日志切面
 * 拦截标注了 @Log 注解的方法，自动记录操作日志
 */
@Slf4j
@Aspect
@Component
@Order(10) // 优先级低于权限切面，确保权限校验通过后再记录日志
@RequiredArgsConstructor
public class LogAspect {

    private final OperateLogService operateLogService;
    private final ObjectMapper objectMapper;

    /**
     * 请求开始时间（线程本地变量）
     */
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    /**
     * 响应数据最大长度（防止存储过大数据）
     */
    private static final int MAX_RESPONSE_LENGTH = 2000;

    /**
     * 请求参数最大长度
     */
    private static final int MAX_REQUEST_LENGTH = 2000;

    /**
     * 切入点：所有标注了 @Log 注解的方法
     */
    @Pointcut("@annotation(com.movk.security.annotation.Log)")
    public void logPointcut() {
    }

    /**
     * 前置通知：记录请求开始时间
     */
    @Before("logPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        START_TIME.set(System.currentTimeMillis());
    }

    /**
     * 后置返回通知：正常返回时记录日志
     */
    @AfterReturning(pointcut = "logPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, null, result);
    }

    /**
     * 异常通知：发生异常时记录日志
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "ex")
    public void doAfterThrowing(JoinPoint joinPoint, Exception ex) {
        handleLog(joinPoint, ex, null);
    }

    /**
     * 处理日志记录
     */
    private void handleLog(JoinPoint joinPoint, Exception ex, Object result) {
        try {
            // 获取 @Log 注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);

            if (logAnnotation == null) {
                return;
            }

            // 构建日志实体
            OperateLog operateLog = buildOperateLog(joinPoint, logAnnotation, ex, result);

            // 异步保存日志
            operateLogService.saveLogAsync(operateLog);

        } catch (Exception e) {
            log.error("记录操作日志异常: {}", e.getMessage(), e);
        } finally {
            START_TIME.remove();
        }
    }

    /**
     * 构建操作日志实体
     */
    private OperateLog buildOperateLog(JoinPoint joinPoint, Log logAnnotation, Exception ex, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        OperateLog operateLog = new OperateLog();

        // 基本信息
        operateLog.setTraceId(MDC.get(TraceIdFilter.TRACE_ID_MDC_KEY));
        operateLog.setModule(logAnnotation.module());
        operateLog.setOperation(logAnnotation.operation().name());
        operateLog.setMethod(method.getDeclaringClass().getName() + "." + method.getName());

        // 用户信息
        setUserInfo(operateLog);

        // 请求信息
        setRequestInfo(operateLog, joinPoint, logAnnotation);

        // 响应信息
        if (logAnnotation.isSaveResponseData() && result != null) {
            setResponseData(operateLog, result);
        }

        // 状态与耗时
        if (ex != null) {
            operateLog.setStatus(BusinessStatus.FAILURE);
            operateLog.setErrorMsg(truncate(ex.getMessage(), MAX_RESPONSE_LENGTH));
        } else {
            operateLog.setStatus(BusinessStatus.SUCCESS);
        }

        Long startTime = START_TIME.get();
        if (startTime != null) {
            operateLog.setOperationTime((int) (System.currentTimeMillis() - startTime));
        }

        return operateLog;
    }

    /**
     * 设置用户信息
     */
    private void setUserInfo(OperateLog operateLog) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                operateLog.setUserId(loginUser.getId());
                operateLog.setUsername(loginUser.getUsername());
            }
        } catch (Exception e) {
            log.debug("获取当前用户信息失败: {}", e.getMessage());
        }
    }

    /**
     * 设置请求信息
     */
    private void setRequestInfo(OperateLog operateLog, JoinPoint joinPoint, Log logAnnotation) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        operateLog.setRequestMethod(request.getMethod());
        operateLog.setRequestUrl(request.getRequestURI());
        operateLog.setUserIp(getClientIp(request));
        operateLog.setUserAgent(request.getHeader("User-Agent"));

        // 请求参数
        if (logAnnotation.isSaveRequestData()) {
            setRequestParams(operateLog, joinPoint, logAnnotation, request);
        }
    }

    /**
     * 设置请求参数
     */
    private void setRequestParams(OperateLog operateLog, JoinPoint joinPoint, Log logAnnotation, HttpServletRequest request) {
        try {
            // URL 参数
            String queryString = request.getQueryString();
            if (StringUtils.hasText(queryString)) {
                operateLog.setRequestParams(truncate(queryString, MAX_REQUEST_LENGTH));
            }

            // 方法参数（作为 requestBody）
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                Set<String> excludeNames = Arrays.stream(logAnnotation.excludeParamNames())
                    .collect(Collectors.toSet());

                String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
                Map<String, Object> paramMap = new HashMap<>();

                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    String paramName = parameterNames != null && i < parameterNames.length ? parameterNames[i] : "arg" + i;

                    // 跳过敏感参数
                    if (excludeNames.contains(paramName)) {
                        paramMap.put(paramName, "[FILTERED]");
                        continue;
                    }

                    // 跳过无法序列化的类型
                    if (isIgnoredType(arg)) {
                        continue;
                    }

                    // 过滤对象中的敏感字段
                    paramMap.put(paramName, filterSensitiveFields(arg, excludeNames));
                }

                if (!paramMap.isEmpty()) {
                    String requestBody = objectMapper.writeValueAsString(paramMap);
                    operateLog.setRequestBody(truncate(requestBody, MAX_REQUEST_LENGTH));
                }
            }
        } catch (Exception e) {
            log.debug("序列化请求参数失败: {}", e.getMessage());
            operateLog.setRequestBody("[SERIALIZE_ERROR]");
        }
    }

    /**
     * 设置响应数据
     */
    private void setResponseData(OperateLog operateLog, Object result) {
        try {
            String responseJson = objectMapper.writeValueAsString(result);
            operateLog.setResponseData(truncate(responseJson, MAX_RESPONSE_LENGTH));
        } catch (Exception e) {
            log.debug("序列化响应数据失败: {}", e.getMessage());
            operateLog.setResponseData("[SERIALIZE_ERROR]");
        }
    }

    /**
     * 过滤敏感字段
     */
    private Object filterSensitiveFields(Object obj, Set<String> excludeNames) {
        if (obj == null) {
            return null;
        }

        // 对于基本类型和字符串，直接返回
        if (obj instanceof String || obj.getClass().isPrimitive() || obj instanceof Number || obj instanceof Boolean) {
            return obj;
        }

        try {
            // 转换为 Map 后过滤
            String json = objectMapper.writeValueAsString(obj);
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            for (String excludeName : excludeNames) {
                if (map.containsKey(excludeName)) {
                    map.put(excludeName, "[FILTERED]");
                }
            }

            return map;
        } catch (Exception e) {
            return obj;
        }
    }

    /**
     * 判断是否为忽略类型（无法/无需序列化的类型）
     */
    private boolean isIgnoredType(Object obj) {
        if (obj == null) {
            return true;
        }
        return obj instanceof HttpServletRequest
            || obj instanceof HttpServletResponse
            || obj instanceof MultipartFile
            || obj instanceof MultipartFile[];
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能包含多个 IP，取第一个
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 截断字符串
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...[TRUNCATED]";
    }
}
