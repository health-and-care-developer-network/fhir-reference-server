package uk.nhs.fhir.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;

import com.google.common.collect.Maps;

public class FhirReflectionUtils {
	
	private static final Map<Class<?>, Map<String, Method>> cachedMethods = Maps.newConcurrentMap();
	
	private static Method getOrCacheMethod(Object o, String methodName) {
		Class<?> clazz = o.getClass();
		Map<String, Method> methodsMap = cachedMethods.putIfAbsent(clazz, Maps.newConcurrentMap());
		try {
			return methodsMap.putIfAbsent(methodName, o.getClass().getMethod(methodName));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("No method " + methodName + "() for resource class " + o.getClass().getName());
		}
	}
	
	public static Optional<String> callResourceMethodByReflection(String methodName, IBaseResource resource) {
		Method method = getOrCacheMethod(resource, methodName);
		Object result = invokeMethod(method, resource, methodName);
		String resultString = castToString(result, methodName, resource);
		return Optional.ofNullable(resultString);
	}

	public static Object invokeMethod(Method method, IBaseResource resource, String methodName) {
		try {
			return method.invoke(resource);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException("Failed to invoke " + methodName + "() for class " + resource.getClass().getName());
		}
	}
	
	private static String castToString(Object result, String methodName, IBaseResource resource) {
		try {
			return (String)result;
		} catch (ClassCastException cce) {
			String resultClassDesc = "[null]";
			if (result != null) {
				resultClassDesc = result.getClass().getName();
			}
			throw new IllegalStateException("Expected string from " + methodName + "() for class " + resource.getClass().getName() + " but got " + resultClassDesc);
		} 
	}

	public static Optional<String> getUrlByReflection(IBaseResource resource) {
		return callResourceMethodByReflection("getUrl", resource);
	}

	public static String expectUrlByReflection(IBaseResource resource) {
		Optional<String> url = getUrlByReflection(resource);
		if (url.isPresent()) {
			return url.get();
		} else {
			throw new IllegalStateException("getUrl() returned null for resource " + resource.getClass().getName());
		}
	}

	public static Optional<String> getFhirReleaseByReflection(IBaseResource resource) {
		return callResourceMethodByReflection("getFhirVersion", resource);
	}

	
}
