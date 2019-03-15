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
import org.mybatis.generator.api.IntrospectedColumn;
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
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

/**
 * updateByIdSelective method would ignore all null values,
 * use this SelectNull method to update null or override entity updates
 */
public class UpdateSelectNullPlugin extends PluginAdapter {

  private String updateByIdSelectManually = "updateByIdSelectNull";

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
  public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    // check one not exist, then all not exist
    if (!genericPlugin.methodsAdded.contains(updateByIdSelectManually)) {
      genericPlugin.methodsAdded.add(updateByIdSelectManually);
      interfaze = genericPlugin.genericInterface;
      addUpdateByIdSelectManuallyMethod(interfaze, introspectedTable);
    }
    return true;
  }

  @Override
  public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
    XmlElement root = document.getRootElement();
    addUpdateByIdSelectManuallyElement(root, introspectedTable);
    return true;
  }

  private void addUpdateByIdSelectManuallyMethod(Interface interfaze,
      IntrospectedTable introspectedTable) {
    Method method = new Method();
    context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

    method.setName(updateByIdSelectManually);
    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(FullyQualifiedJavaType.getIntInstance());

    Parameter record = new Parameter(genericPlugin.genericModel, "record");
    record.addAnnotation("@Param(\"record\")");
    method.addParameter(record);

    Parameter updateClause = new Parameter(FullyQualifiedJavaType.getStringInstance(),
        "updateNullClause");
    updateClause.addAnnotation("@Param(\"updateNullClause\")");
    method.addParameter(updateClause);

    interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
    interfaze.addMethod(method);
  }

  private void addUpdateByIdSelectManuallyElement(XmlElement parent,
      IntrospectedTable introspectedTable) {
    XmlElement element = new XmlElement("update");
    context.getCommentGenerator().addComment(element);

    element.addAttribute(new Attribute("id", updateByIdSelectManually));
    element.addAttribute(new Attribute("parameterType",
        introspectedTable.getBaseRecordType()));

    element.addElement(new TextElement(
        "update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

    XmlElement dynamicElement = new XmlElement("set"); //$NON-NLS-1$
    element.addElement(dynamicElement);
    // dynamic sets
    StringBuilder sb = new StringBuilder();
    for (IntrospectedColumn introspectedColumn : ListUtilities.removeGeneratedAlwaysColumns(introspectedTable
        .getNonPrimaryKeyColumns())) {
      sb.setLength(0);
      sb.append(introspectedColumn.getJavaProperty());
      sb.append(" != null"); //$NON-NLS-1$
      XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
      isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
      dynamicElement.addElement(isNotNullElement);

      sb.setLength(0);
      sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
      sb.append(" = "); //$NON-NLS-1$
      sb.append(MyBatis3FormattingUtilities
          .getParameterClause(introspectedColumn));
      sb.append(',');

      isNotNullElement.addElement(new TextElement(sb.toString()));
    }

    // extra manually sets
    dynamicElement.addElement(new TextElement("${updateNullClause}"));

    boolean addPrefix = introspectedTable.getRules().generatePrimaryKeyClass();
    boolean and = false;
    for (IntrospectedColumn introspectedColumn : introspectedTable
        .getPrimaryKeyColumns()) {
      sb.setLength(0);
      if (and) {
        sb.append("  and ");
      } else {
        sb.append("where ");
        and = true;
      }

      sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
      sb.append(" = ");
      String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
      if (addPrefix) {
        sb.append(parameterClause.substring(0, 2));
        sb.append("key.");
        sb.append(parameterClause.substring(2));
      } else {
        sb.append(parameterClause);
      }
      element.addElement(new TextElement(sb.toString()));
    }

    parent.addElement(element);
  }

}
