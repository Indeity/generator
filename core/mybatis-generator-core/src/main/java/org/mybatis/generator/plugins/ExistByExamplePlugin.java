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

import java.util.List;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * add existByExample method for mapper
 */
public class ExistByExamplePlugin extends PluginAdapter {

  private String existByExample = "existByExample";

  private CreateGenericInterfacePlugin genericPlugin;

  @Override
  public boolean validate(List<String> warnings) {
    // check if CreateGenericInterfacePlugin is enabled
    genericPlugin = CreateGenericInterfacePlugin.getInstance(getContext());
    if (genericPlugin == null) {
      warnings.add("CreateGenericInterfacePlugin not enabled");
      return false;
    }

    return true;
  }

  @Override
  public boolean clientGenerated(
      Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    // check one not exist, then all not exist
    if (!genericPlugin.methodsAdded.contains(existByExample)) {
      genericPlugin.methodsAdded.add(existByExample);
      interfaze = genericPlugin.genericInterface;
      addExistByExampleMethod(interfaze, introspectedTable);
    }
    return true;
  }

  @Override
  public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
    XmlElement root = document.getRootElement();
    addExistByExampleElement(root, introspectedTable);
    return true;
  }

  private void addExistByExampleMethod(Interface interfaze,
      IntrospectedTable introspectedTable) {
    // add method
    Method method = new Method();
    method.setName(existByExample);
    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(FullyQualifiedJavaType.getBooleanPrimitiveInstance());
    // add example param
    Parameter example = new Parameter(genericPlugin.genericExample, "example");
    method.addParameter(example);
    // add imported type and method
    interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
    interfaze.addMethod(method);
  }

  private void addExistByExampleElement(XmlElement parent,
      IntrospectedTable introspectedTable) {
    // create select element
    XmlElement element = new XmlElement("select");
    element.addAttribute(new Attribute("id", existByExample));
    element.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
    element.addAttribute(new Attribute("resultType", "boolean"));

    // start sql statement
    element.addElement(new TextElement("select"));

    // count
    element.addElement(new TextElement("count(*) > 0"));

    // from
    element.addElement(new TextElement("from "
        + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

    // where example
    XmlElement example = new XmlElement("if");
    example.addAttribute(new Attribute("test", "_parameter != null"));
    XmlElement includeElement = new XmlElement("include");
    includeElement.addAttribute(
        new Attribute("refid", introspectedTable.getExampleWhereClauseId()));
    example.addElement(includeElement);
    element.addElement(example);

    // add to root
    parent.addElement(element);
  }


}
