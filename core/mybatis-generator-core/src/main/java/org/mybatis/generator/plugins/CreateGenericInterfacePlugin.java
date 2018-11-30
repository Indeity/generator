/**
 *    Copyright 2006-2018 the original author or authors.
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

import static org.mybatis.generator.internal.util.StringUtility.isTrue;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.PluginAggregator;

/**
 * Mybatis generator plugin to create a generic interface for all mappers. usage:
 * <pre>
 * &lt;plugin type=&quot;org.mybatis.generator.plugins.CreateGenericInterfacePlugin&quot;&gt;
 * &lt;property name=&quot;interface&quot; value=&quot;some.package.InterfaceName&quot; /&gt;
 * &lt;property name="example" value="true" /&gt;
 * &lt;/plugin&gt;
 * </pre>
 *
 * <p> notes: examples must be full enabled or disabled as &lt;table&gt; settings.
 * And claim this plugin at top of config file
 *
 * @see <a href="https://github.com/dcendents/mybatis-generator-plugins">dcendents/mybatis-generator-plugins</a>
 */
public class CreateGenericInterfacePlugin extends PluginAdapter {

  public static final String INTERFACE = "interface";

  // global enable examples
  public static final String EXAMPLE = "example";

  Interface genericInterface; // the generic interface would be edited by other plugins
  FullyQualifiedJavaType genericModel = new FullyQualifiedJavaType("T");
  FullyQualifiedJavaType genericExample = new FullyQualifiedJavaType("E");
  FullyQualifiedJavaType genericId = new FullyQualifiedJavaType("PK");
  FullyQualifiedJavaType genericModelList;
  Set<String> methodsAdded;

  private boolean enableExample;
  private String interfaceName;
  private FullyQualifiedJavaType longPrimitive;
  private Map<IntrospectedTable, FullyQualifiedJavaType> models;
  private Map<IntrospectedTable, FullyQualifiedJavaType> examples;
  private Map<IntrospectedTable, FullyQualifiedJavaType> ids;

  /**
   * get instance of current plugin
   *
   * @param context
   * @return
   */
  static CreateGenericInterfacePlugin getInstance(Context context) {
    try {
      PluginAggregator pluginAggregator = (PluginAggregator) context.getPlugins();
      Field field = pluginAggregator.getClass().getDeclaredField("plugins");
      field.setAccessible(true);
      List<Plugin> plugins = (List<Plugin>) field.get(pluginAggregator);
      for (Plugin plugin : plugins) {
        if (CreateGenericInterfacePlugin.class.equals(plugin.getClass())) {
          return (CreateGenericInterfacePlugin) plugin;
        }
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
    }
    return null;
  }

  /**
   * format xml lines
   */
  static void formatLines(XmlElement element, List<String> list, char delimiter, boolean bracket,
      int level) {
    List<TextElement> texts = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    if (bracket) {
      sb.append('(');
    }
    for (String s : list) {
      if (sb.length() > 80) {
        texts.add(new TextElement(sb.toString()));
        sb.setLength(0);
        OutputUtilities.xmlIndent(sb, level);
      }
      sb.append(s).append(delimiter);
    }
    sb.setLength(sb.length() - 1);
    if (bracket) {
      sb.append(')');
    }
    texts.add(new TextElement(sb.toString()));

    for (TextElement text : texts) {
      element.addElement(text);
    }
  }

  @Override
  public boolean validate(List<String> warnings) {
    interfaceName = properties.getProperty(INTERFACE);
    enableExample = isTrue(properties.getProperty(EXAMPLE));

    String warning = "Property %s not set for plugin %s";
    if (!stringHasValue(interfaceName)) {
      warnings.add(String.format(warning, INTERFACE, this.getClass().getSimpleName()));
      return false;
    }

    init();

    return true;
  }

  private void init() {
    genericModelList = FullyQualifiedJavaType.getNewListInstance();
    genericModelList.addTypeArgument(genericModel);

    longPrimitive = new FullyQualifiedJavaType("long");

    FullyQualifiedJavaType className = new FullyQualifiedJavaType(interfaceName);
    className.addTypeArgument(genericModel);
    if (enableExample) {
      className.addTypeArgument(genericExample);
    }
    className.addTypeArgument(genericId);

    genericInterface = new Interface(className);
    genericInterface.setVisibility(JavaVisibility.PUBLIC);

    methodsAdded = new HashSet<>();

    models = new HashMap<>();
    examples = new HashMap<>();
    ids = new HashMap<>();
  }

  @Override
  public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles() {
    List<GeneratedJavaFile> models = new ArrayList<>();

    // PS. select by example would return list
    if (enableExample) {
      genericInterface.addImportedType(FullyQualifiedJavaType.getNewListInstance());
    }

    // PS. sort methods
    Collections.sort(genericInterface.getMethods(), new Comparator<Method>() {
      @Override
      public int compare(Method o1, Method o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });

    GeneratedJavaFile genericInterfaceFile =
        new GeneratedJavaFile(genericInterface,
            context.getJavaClientGeneratorConfiguration().getTargetProject(),
            new DefaultJavaFormatter());

    models.add(genericInterfaceFile);

    return models;
  }

  @Override
  public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    FullyQualifiedJavaType type = new FullyQualifiedJavaType(interfaceName);
    type.addTypeArgument(models.get(introspectedTable));
    if (enableExample) {
      type.addTypeArgument(examples.get(introspectedTable));
    }
    type.addTypeArgument(ids.get(introspectedTable));

    interfaze.addSuperInterface(type);

    // PS. add explicitly import for entity class
    interfaze.addImportedType(models.get(introspectedTable));
    if (enableExample) {
      interfaze.addImportedType(examples.get(introspectedTable));
    }

    return true;
  }

  void addGenericMethod(Method method, FullyQualifiedJavaType returnType,
      FullyQualifiedJavaType... types) {
    method.addAnnotation("@Override");

    if (!methodsAdded.contains(method.getName())) {
      Method genericMethod = new Method(method.getName());
//      genericMethod.addJavaDocLine("/**");
//      genericMethod.addJavaDocLine(" * This method was generated by MyBatis Generator.");
//      genericMethod.addJavaDocLine(" *");
//      genericMethod.addJavaDocLine(" * @mbg.generated");
//      genericMethod.addJavaDocLine(" */");

      genericMethod.setReturnType(returnType);

      for (int i = 0; i < method.getParameters().size(); i++) {
        Parameter parameter = method.getParameters().get(i);
        FullyQualifiedJavaType paramType = types.length > i ? types[i] : parameter.getType();

        Parameter genericParameter = new Parameter(paramType, parameter.getName());
        genericMethod.addParameter(genericParameter);
      }

      genericInterface.addMethod(genericMethod);

      methodsAdded.add(method.getName());
    }
  }

  @Override
  public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientCountByExample(method, introspectedTable);
    }
    return false;
  }

  @Override
  public boolean clientCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientCountByExample(method, introspectedTable);
    }
    return false;
  }

  private void addClientCountByExample(Method method, IntrospectedTable introspectedTable) {
    examples.put(introspectedTable, method.getParameters().get(0).getType());
    addGenericMethod(method, longPrimitive, genericExample);
  }

  @Override
  public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientDeleteByExample(method);
    }
    return false;
  }

  @Override
  public boolean clientDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientDeleteByExample(method);
    }
    return false;
  }

  private void addClientDeleteByExample(Method method) {
    addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericExample);
  }

  @Override
  public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    addClientDeleteByPrimaryKey(method, introspectedTable);
    return false;
  }

  @Override
  public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    addClientDeleteByPrimaryKey(method, introspectedTable);
    return false;
  }

  private void addClientDeleteByPrimaryKey(Method method, IntrospectedTable introspectedTable) {
    ids.put(introspectedTable, method.getParameters().get(0).getType());
    addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericId);
  }

  @Override
  public boolean clientInsertMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    addClientInsert(method, introspectedTable);
    return false;
  }

  @Override
  public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    addClientInsert(method, introspectedTable);
    return false;
  }

  private void addClientInsert(Method method, IntrospectedTable introspectedTable) {
    models.put(introspectedTable, method.getParameters().get(0).getType());
    addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel);
  }

  @Override
  public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientSelectByExampleWithBLOBs(method);
    }
    return false;
  }

  @Override
  public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method,
      TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientSelectByExampleWithBLOBs(method);
    }
    return false;
  }

  private void addClientSelectByExampleWithBLOBs(Method method) {
    addGenericMethod(method, genericModelList, genericExample);
  }

  @Override
  public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method,
      Interface interfaze, IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientSelectByExampleWithoutBLOBs(method);
    }
    return false;
  }

  @Override
  public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method,
      TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientSelectByExampleWithoutBLOBs(method);
    }
    return false;
  }

  private void addClientSelectByExampleWithoutBLOBs(Method method) {
    addGenericMethod(method, genericModelList, genericExample);
  }

  @Override
  public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    addClientSelectByPrimaryKey(method);
    return false;
  }

  @Override
  public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    addClientSelectByPrimaryKey(method);
    return false;
  }

  private void addClientSelectByPrimaryKey(Method method) {
    addGenericMethod(method, genericModel, genericId);
  }

  @Override
  public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientUpdateByExampleSelective(method);
    }
    return false;
  }

  @Override
  public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method,
      TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientUpdateByExampleSelective(method);
    }
    return false;
  }

  private void addClientUpdateByExampleSelective(Method method) {
    addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel, genericExample);
  }

  @Override
  public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientUpdateByExampleWithBLOBs(method);
    }
    return false;
  }

  @Override
  public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method,
      TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientUpdateByExampleWithBLOBs(method);
    }
    return false;
  }

  private void addClientUpdateByExampleWithBLOBs(Method method) {
    addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel, genericExample);
  }

  @Override
  public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method,
      Interface interfaze, IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientUpdateByExampleWithoutBLOBs(method);
    }
    return false;
  }

  @Override
  public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method,
      TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    if (enableExample) {
      addClientUpdateByExampleWithoutBLOBs(method);
    }
    return false;
  }

  private void addClientUpdateByExampleWithoutBLOBs(Method method) {
    addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel, genericExample);
  }

  @Override
  public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method,
      Interface interfaze, IntrospectedTable introspectedTable) {
    addClientUpdateByPrimaryKeySelective(method);
    return false;
  }

  @Override
  public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method,
      TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    addClientUpdateByPrimaryKeySelective(method);
    return false;
  }

  private void addClientUpdateByPrimaryKeySelective(Method method) {
    addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel);
  }

  @Override
  public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method,
      Interface interfaze, IntrospectedTable introspectedTable) {
    addClientUpdateByPrimaryKeyWithBLOBs(method);
    return false;
  }

  @Override
  public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method,
      TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    addClientUpdateByPrimaryKeyWithBLOBs(method);
    return false;
  }

  private void addClientUpdateByPrimaryKeyWithBLOBs(Method method) {
    addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel);
  }

  @Override
  public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method,
      Interface interfaze, IntrospectedTable introspectedTable) {
    addClientUpdateByPrimaryKeyWithoutBLOBs(method);
    return false;
  }

  @Override
  public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method,
      TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    addClientUpdateByPrimaryKeyWithoutBLOBs(method);
    return false;
  }

  private void addClientUpdateByPrimaryKeyWithoutBLOBs(Method method) {
    addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel);
  }

  @Override
  public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    addClientInsertSelective(method);
    return false;
  }

  @Override
  public boolean clientInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    addClientInsertSelective(method);
    return false;
  }

  private void addClientInsertSelective(Method method) {
    addGenericMethod(method, FullyQualifiedJavaType.getIntInstance(), genericModel);
  }

  @Override
  public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze,
      IntrospectedTable introspectedTable) {
    addClientSelectAll(method);
    return false;
  }

  @Override
  public boolean clientSelectAllMethodGenerated(Method method, TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    addClientSelectAll(method);
    return false;
  }

  private void addClientSelectAll(Method method) {
    addGenericMethod(method, genericModel);
  }

}
