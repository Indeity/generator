/**
 *    Copyright 2006-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * lombok plugin, usage:
 *
 * <pre>
 * &lt;plugin type=&quot;org.mybatis.generator.plugins.LombokPlugin&quot;&gt;
 * &lt;!-- enable annotations --&gt;
 * &lt;property name=&quot;builder&quot; value=&quot;true&quot;/&gt;
 * &lt;property name=&quot;allArgsConstructor&quot; value=&quot;false&quot;/&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * @see <a href="https://github.com/softwareloop/mybatis-generator-lombok-plugin">softwareloop/mybatis-generator-lombok-plugin</a>
 */
public class LombokPlugin extends PluginAdapter {

  private final Collection<Annotations> annotations;

  /**
   * LombokPlugin constructor
   */
  public LombokPlugin() {
    annotations = new LinkedHashSet<Annotations>(Annotations.values().length);
  }

  /**
   * @param warnings list of warnings
   * @return always true
   */
  public boolean validate(List<String> warnings) {
    return true;
  }

  /**
   * Intercepts base record class generation
   *
   * @param topLevelClass the generated base record class
   * @param introspectedTable The class containing information about the table as introspected from
   * the database
   * @return always true
   */
  @Override
  public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    addDataAnnotation(topLevelClass);
    return true;
  }

  /**
   * Intercepts primary key class generation
   *
   * @param topLevelClass the generated primary key class
   * @param introspectedTable The class containing information about the table as introspected from
   * the database
   * @return always true
   */
  @Override
  public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    addDataAnnotation(topLevelClass);
    return true;
  }

  /**
   * Intercepts "record with blob" class generation
   *
   * @param topLevelClass the generated record with BLOBs class
   * @param introspectedTable The class containing information about the table as introspected from
   * the database
   * @return always true
   */
  @Override
  public boolean modelRecordWithBLOBsClassGenerated(
      TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    addDataAnnotation(topLevelClass);
    return true;
  }

  /**
   * Prevents all getters from being generated. See SimpleModelGenerator
   *
   * @param method the getter, or accessor, method generated for the specified column
   * @param topLevelClass the partially implemented model class
   * @param introspectedColumn The class containing information about the column related to this
   * field as introspected from the database
   * @param introspectedTable The class containing information about the table as introspected from
   * the database
   * @param modelClassType the type of class that the field is generated for
   */
  @Override
  public boolean modelGetterMethodGenerated(Method method,
      TopLevelClass topLevelClass,
      IntrospectedColumn introspectedColumn,
      IntrospectedTable introspectedTable,
      ModelClassType modelClassType) {
    return false;
  }

  /**
   * Prevents all setters from being generated See SimpleModelGenerator
   *
   * @param method the setter, or mutator, method generated for the specified column
   * @param topLevelClass the partially implemented model class
   * @param introspectedColumn The class containing information about the column related to this
   * field as introspected from the database
   * @param introspectedTable The class containing information about the table as introspected from
   * the database
   * @param modelClassType the type of class that the field is generated for
   * @return always false
   */
  @Override
  public boolean modelSetterMethodGenerated(Method method,
      TopLevelClass topLevelClass,
      IntrospectedColumn introspectedColumn,
      IntrospectedTable introspectedTable,
      ModelClassType modelClassType) {
    return false;
  }

  /**
   * Adds the lombok annotations' imports and annotations to the class
   *
   * @param topLevelClass the partially implemented model class
   */
  private void addDataAnnotation(TopLevelClass topLevelClass) {
    for (Annotations annotation : annotations) {
      topLevelClass.addImportedType(annotation.javaType);
      topLevelClass.addAnnotation(annotation.name);
    }
  }

  @Override
  public void setProperties(Properties properties) {
    super.setProperties(properties);

    //@Data is default annotation
    annotations.add(Annotations.DATA);

    for (Entry<Object, Object> entry : properties.entrySet()) {
      boolean isEnable = Boolean.parseBoolean(entry.getValue().toString());

      if (isEnable) {
        String paramName = entry.getKey().toString().trim();
        Annotations annotation = Annotations.getValueOf(paramName);
        if (annotation != null) {
          annotations.add(annotation);
          annotations.addAll(Annotations.getDependencies(annotation));
        }
      }
    }
  }

  @Override
  public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
    interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Mapper"));
    interfaze.addAnnotation("@Mapper");
    return true;
  }

  private enum Annotations {
    DATA("data", "@Data", "lombok.Data"),
    BUILDER("builder", "@Builder", "lombok.Builder"),
    ALL_ARGS_CONSTRUCTOR("allArgsConstructor", "@AllArgsConstructor", "lombok.AllArgsConstructor"),
    NO_ARGS_CONSTRUCTOR("noArgsConstructor", "@NoArgsConstructor", "lombok.NoArgsConstructor"),
    TO_STRING("toString", "@ToString", "lombok.ToString");


    private final String paramName;
    private final String name;
    private final FullyQualifiedJavaType javaType;


    Annotations(String paramName, String name, String className) {
      this.paramName = paramName;
      this.name = name;
      this.javaType = new FullyQualifiedJavaType(className);
    }

    private static Annotations getValueOf(String paramName) {
      for (Annotations annotation : Annotations.values()) {
        if (String.CASE_INSENSITIVE_ORDER.compare(paramName, annotation.paramName) == 0) {
          return annotation;
        }
      }

      return null;
    }

    private static Collection<Annotations> getDependencies(Annotations annotation) {
      if (annotation == ALL_ARGS_CONSTRUCTOR) {
        return Collections.singleton(NO_ARGS_CONSTRUCTOR);
      } else {
        return Collections.emptyList();
      }
    }
  }

}
