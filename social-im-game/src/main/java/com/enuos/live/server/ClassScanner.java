package com.enuos.live.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * TODO 类扫描工具.
 *
 * @author wangcaiwen|1443****11@qq.com
 * @version v1.0.0
 * @since 2020/5/11 14:59
 */

@SuppressWarnings("WeakerAccess")
public class ClassScanner implements ResourceLoaderAware {

  private final List<TypeFilter> includeFilters = Lists.newLinkedList();
  private final List<TypeFilter> excludeFilters = Lists.newLinkedList();

  private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
  private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(this.resourcePatternResolver);

  /**
   * TODO 扫描准备.
   *
   * @param basePackages [扫描包]
   * @param annotations [发现注解]
   * @return [对应类]
   * @author wangcaiwen|1443****11@qq.com
   * @create 2020/9/30 18:03
   * @update 2020/9/30 18:03
   */
  @SafeVarargs
  public static Set<Class<?>> scan(String[] basePackages, Class<? extends Annotation>... annotations) {
    ClassScanner cs = new ClassScanner();

    if (ArrayUtils.isNotEmpty(annotations)) {
      for (Class anno : annotations) {
        cs.addIncludeFilter(new AnnotationTypeFilter(anno));
      }
    }

    Set<Class<?>> classes = new HashSet<>();
    for (String s : basePackages) {
      classes.addAll(cs.doScan(s));
    }

    return classes;
  }

  @SafeVarargs
  public static Set<Class<?>> scan(String basePackages, Class<? extends Annotation>... annotations) {
    return ClassScanner.scan(StringUtils.tokenizeToStringArray(basePackages, ",; \t\n"), annotations);
  }

  public final ResourceLoader getResourceLoader() {
    return this.resourcePatternResolver;
  }

  @Override
  public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
    this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    this.metadataReaderFactory = new CachingMetadataReaderFactory(resourceLoader);
  }

  public void addIncludeFilter(TypeFilter includeFilter) {
    this.includeFilters.add(includeFilter);
  }

  public void addExcludeFilter(TypeFilter excludeFilter) {
    this.excludeFilters.add(0, excludeFilter);
  }

  public void resetFilters(boolean useDefaultFilters) {
    this.includeFilters.clear();
    this.excludeFilters.clear();
  }

  /**
   * TODO 开始扫描.
   *
   * @param basePackage 扫描包
   * @return 相应类
   * @author WangCaiWen
   * @since 2020/6/5 - 2020/6/5
   */
  public Set<Class<?>> doScan(String basePackage) {
    Set<Class<?>> classes = new HashSet<>();
    try {
      String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
          + org.springframework.util.ClassUtils.convertClassNameToResourcePath(
            SystemPropertyUtils.resolvePlaceholders(basePackage))
          + "/**/*.class";
      Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);

      for (Resource resource : resources) {
        if (resource.isReadable()) {
          MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resource);
          boolean flagOne = includeFilters.size() == 0 && excludeFilters.size() == 0;
          boolean flagTwo = matches(metadataReader);
          if (flagOne || flagTwo) {
            try {
              classes.add(Class.forName(metadataReader
                  .getClassMetadata().getClassName()));
            } catch (ClassNotFoundException e) {
              e.printStackTrace();
            }
          }
        }
      }
    } catch (IOException ex) {
      throw new BeanDefinitionStoreException(
          "I/O failure during classpath scanning", ex);
    }
    return classes;
  }

  protected boolean matches(MetadataReader metadataReader) throws IOException {
    for (TypeFilter tf : this.excludeFilters) {
      if (tf.match(metadataReader, this.metadataReaderFactory)) {
        return false;
      }
    }
    for (TypeFilter tf : this.includeFilters) {
      if (tf.match(metadataReader, this.metadataReaderFactory)) {
        return true;
      }
    }
    return false;
  }
}
