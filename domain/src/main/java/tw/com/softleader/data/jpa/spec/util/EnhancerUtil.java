package tw.com.softleader.data.jpa.spec.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class EnhancerUtil {

  public <T> T wrapWithIfaceImplementation(
      @NonNull Class<T> iface,
      @NonNull Specification<Object> targetSpec) {
    Enhancer enhancer = new Enhancer();
    enhancer.setInterfaces(new Class[] { iface });
    enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
      if ("toString".equals(method.getName())) {
        return iface.getSimpleName() + "[" + proxy.invoke(targetSpec, args) + "]";
      }
      return proxy.invoke(targetSpec, args);
    });
    return (T) enhancer.create();
  }
}
