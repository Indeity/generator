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

import java.util.List;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * sometimes we just need select one by example, so just use this plugin
 *
 * @author YinJH
 */
public class SelectOneByExamplePlugin extends PluginAdapter {

  private String selectOneByExample = "selectOneByExample";

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
  public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
    // check one not exist, then all not exist
    if (!genericPlugin.methodsAdded.contains(selectOneByExample)) {
      genericPlugin.methodsAdded.add(selectOneByExample);
      interfaze = genericPlugin.genericInterface;
      addSelectOneByExampleMethod(interfaze, introspectedTable);
    }
    return true;
  }

  @Override
  public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
    XmlElement root = document.getRootElement();
    addSelectOneByExampleElement(root, introspectedTable);
    return true;
  }

  private void addSelectOneByExampleMethod(Interface interfaze,
      IntrospectedTable introspectedTable) {
    // add method
    Method method = new Method(selectOneByExample);
    method.setAbstract(true);
    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(genericPlugin.genericModel);
    // add example param
    Parameter example = new Parameter(genericPlugin.genericExample, "example");
//    example.addAnnotation("@Param(\"example\")");
    method.addParameter(example);
    // add imported type and method
    interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
    interfaze.addMethod(method);
  }

  private void addSelectOneByExampleElement(XmlElement parent,
      IntrospectedTable introspectedTable) {
    // create select element
    XmlElement element = new XmlElement("select");
    element.addAttribute(new Attribute("id", selectOneByExample));
    element.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
    element.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));

    // start sql statement
    element.addElement(new TextElement("select"));

    // distinct
    XmlElement distinct = new XmlElement("if");
    distinct.addAttribute(new Attribute("test", "distinct"));
    distinct.addElement(new TextElement("distinct"));
    element.addElement(distinct);

    // base column list
    XmlElement baseColumnList = new XmlElement("include");
    baseColumnList.addAttribute(new Attribute("refid", introspectedTable.getBaseColumnListId()));
    element.addElement(baseColumnList);

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

    // order by
    XmlElement orderBy = new XmlElement("if");
    orderBy.addAttribute(new Attribute("test", "orderByClause != null"));
    orderBy.addElement(new TextElement("order by ${orderByClause}"));
    element.addElement(orderBy);

    // limit
    element.addElement(new TextElement("limit 1"));

    // add to root
    parent.addElement(element);
  }

}
