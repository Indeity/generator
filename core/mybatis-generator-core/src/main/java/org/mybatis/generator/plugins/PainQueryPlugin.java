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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.Field;
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
import org.mybatis.generator.config.GeneratedKey;

/**
 * pain query plugin for update and select, based on generic interface!
 *
 * <p> commented "public entity property", "limit,offset", "insertOrUpdateManually" functions tables
 * should generated as flat mode, and only one primary key!
 *
 * @see <a href="https://github.com/dfxyz/mybatis-generator-plugin">dfxyz/mybatis-generator-plugin</a>
 */
public class PainQueryPlugin extends PluginAdapter {

  // method names
  private String selectManuallyByExample = "selectManuallyByExample";
  private String selectManuallyByPrimaryKey = "selectManuallyById";
  private String updateManuallyByExample = "updateManuallyByExample";
  private String updateManuallyByPrimaryKey = "updateManuallyById";


  private CreateGenericInterfacePlugin genericPlugin;

  public boolean validate(List<String> warnings) {
    // check if CreateGenericInterfacePlugin is enabled
    genericPlugin = CreateGenericInterfacePlugin.getInstance(getContext());
    if (genericPlugin == null) {
      warnings.add("CreateGenericInterfacePlugin not enabled");
      return false;
    }

    return true;
  }

  // make all fields public
//  @Override
//  public boolean modelFieldGenerated(
//      Field field,
//      TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
//      IntrospectedTable introspectedTable,
//      ModelClassType modelClassType) {
//    field.setVisibility(JavaVisibility.PUBLIC);
//    return true;
//  }

  // remove evil getters
//  @Override
//  public boolean modelGetterMethodGenerated(
//      Method method,
//      TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
//      IntrospectedTable introspectedTable,
//      ModelClassType modelClassType) {
//    return false;
//  }

  // remove evil setters
//  @Override
//  public boolean modelSetterMethodGenerated(
//      Method method,
//      TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
//      IntrospectedTable introspectedTable,
//      ModelClassType modelClassType) {
//    return false;
//  }

  // add limit-and-offset-related fields and methods into example classes
  @Override
  public boolean modelExampleClassGenerated(
      TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    FullyQualifiedJavaType intInstance = FullyQualifiedJavaType.getIntInstance();

    Field limit = new Field("limit",
        FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper());
    limit.setVisibility(JavaVisibility.PROTECTED);
    topLevelClass.addField(limit);
    context.getCommentGenerator().addFieldComment(limit, introspectedTable);

    // both limit and offset
    Method justLimit = new Method("limit");
    justLimit.setVisibility(JavaVisibility.PUBLIC);
    justLimit.addParameter(new Parameter(intInstance, "offset"));
    justLimit.addParameter(new Parameter(intInstance, "limit"));
    justLimit.setReturnType(topLevelClass.getType());
    justLimit.addBodyLines(Arrays.asList(
        "this.offset = offset;",
        "this.limit = limit;",
        "return this;"
    ));
    topLevelClass.addMethod(justLimit);

    // simple limit
    Method mLimit = new Method("limit");
    mLimit.setVisibility(JavaVisibility.PUBLIC);
    mLimit.addParameter(new Parameter(intInstance, "limit"));
    mLimit.setReturnType(topLevelClass.getType());
    mLimit.addBodyLine("this.limit = limit;");
    mLimit.addBodyLine("return this;");
    topLevelClass.addMethod(mLimit);

    Method getLimit = new Method("getLimit");
    getLimit.setVisibility(JavaVisibility.PUBLIC);
    getLimit.setReturnType(FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper());
    getLimit.addBodyLine("return limit;");
    topLevelClass.addMethod(getLimit);
    context.getCommentGenerator().addGeneralMethodComment(getLimit, introspectedTable);

    Method setLimit = new Method("setLimit");
    setLimit.setVisibility(JavaVisibility.PUBLIC);
    setLimit.addParameter(
        new Parameter(FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper(), "limit"));
    setLimit.addBodyLine("this.limit = limit;");
    topLevelClass.addMethod(setLimit);
    context.getCommentGenerator().addGeneralMethodComment(setLimit, introspectedTable);

    Field offset = new Field("offset",
        FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper());
    offset.setVisibility(JavaVisibility.PROTECTED);
    topLevelClass.addField(offset);
    context.getCommentGenerator().addFieldComment(offset, introspectedTable);

    Method getOffset = new Method("getOffset");
    getOffset.setVisibility(JavaVisibility.PUBLIC);
    getOffset.setReturnType(FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper());
    getOffset.addBodyLine("return offset;");
    topLevelClass.addMethod(getOffset);
    context.getCommentGenerator().addGeneralMethodComment(getOffset, introspectedTable);

    Method setOffset = new Method("setOffset");
    setOffset.setVisibility(JavaVisibility.PUBLIC);
    setOffset.addParameter(
        new Parameter(FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper(), "offset"));
    setOffset.addBodyLine("this.offset = offset;");
    topLevelClass.addMethod(setOffset);
    context.getCommentGenerator().addGeneralMethodComment(setOffset, introspectedTable);

    return true;
  }

  // add limit-and-offset-related sql part into selectByExample()
  @Override
  public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(
      XmlElement element, IntrospectedTable introspectedTable) {
    element.addElement(getLimitOffsetClauseElement(null));
    return true;
  }

  // add new methods into mapper interfaces
  @Override
  public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
    // check one not exist, then all not exist
    if (!genericPlugin.methodsAdded.contains(selectManuallyByExample)) {
      genericPlugin.methodsAdded.add(selectManuallyByExample);
      interfaze = genericPlugin.genericInterface;
//    addInsertOrUpdateManuallyMethod(interfaze, introspectedTable);
//    addInsertSelectiveOrUpdateManuallyMethod(interfaze, introspectedTable);
      addSelectManuallyByExampleMethod(interfaze, introspectedTable);
      addSelectManuallyByPrimaryKeyMethod(interfaze, introspectedTable);
      addUpdateManuallyByExampleMethod(interfaze, introspectedTable);
      addUpdateManuallyByPrimaryKeyMethod(interfaze, introspectedTable);
    }
    return true;
  }

  // add new elements into mapper XMLs
  @Override
  public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
    XmlElement root = document.getRootElement();
//    addInsertOrUpdateManuallyElement(root, introspectedTable);
//    addInsertSelectiveOrUpdateManuallyElement(root, introspectedTable);
    addSelectManuallyByExampleElement(root, introspectedTable);
    addSelectManuallyByPrimaryKeyElement(root, introspectedTable);
    addUpdateManuallyByExampleElement(root, introspectedTable);
    addUpdateManuallyByPrimaryKeyElement(root, introspectedTable);
    return true;
  }

  // add insertOrUpdateManually() method
  private void addInsertOrUpdateManuallyMethod(Interface interfaze,
      IntrospectedTable introspectedTable) {
    Method method = new Method("insertOrUpdateManually");
    context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(FullyQualifiedJavaType.getIntInstance());

    Parameter record = new Parameter(
        new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "record");
    record.addAnnotation("@Param(\"record\")");
    method.addParameter(record);
    Parameter updateClause = new Parameter(FullyQualifiedJavaType.getStringInstance(),
        "updateClause");
    updateClause.addAnnotation("@Param(\"updateClause\")");
    method.addParameter(updateClause);

    interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
    interfaze.addMethod(method);
  }

  // add XML element for insertOrUpdateManually()
  private void addInsertOrUpdateManuallyElement(XmlElement parent,
      IntrospectedTable introspectedTable) {
    XmlElement element = new XmlElement("insert");
    context.getCommentGenerator().addComment(element);

    element.addAttribute(new Attribute("id", "insertOrUpdateManually"));
    element.addAttribute(new Attribute("parameterType", "map"));

    GeneratedKey gk = introspectedTable.getGeneratedKey();
    String updateGK = "";
    if (gk != null) {
      IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
      if (introspectedColumn != null) {
        if (gk.isJdbcStandard()) {
          element.addAttribute(new Attribute("useGeneratedKeys", "true"));
          element.addAttribute(
              new Attribute("keyProperty", "record." + introspectedColumn.getJavaProperty()));
          element
              .addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName()));
        } else {
          element.addElement(getSelectKeyElement(introspectedColumn, gk));
        }
        updateGK = String.format(", %s = last_insert_id(%s)",
            introspectedColumn.getActualColumnName(), introspectedColumn.getActualColumnName());
      }
    }
    StringBuilder insertClause = new StringBuilder();
    StringBuilder valuesClause = new StringBuilder();
    insertClause.append("insert into ")
        .append(introspectedTable.getFullyQualifiedTableNameAtRuntime())
        .append(" (");
    valuesClause.append("values (");

    List<String> valuesClauses = new ArrayList<>();
    List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(
        introspectedTable.getAllColumns());
    for (int i = 0; i < columns.size(); i++) {
      IntrospectedColumn introspectedColumn = columns.get(i);

      insertClause.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
      String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
      valuesClause.append(parameterClause.substring(0, 2))
          .append("record.")
          .append(parameterClause.substring(2));

      if (i + 1 < columns.size()) {
        insertClause.append(", ");
        valuesClause.append(", ");
      }

      if (valuesClause.length() > 80) {
        element.addElement(new TextElement(insertClause.toString()));
        insertClause.setLength(0);
        OutputUtilities.xmlIndent(insertClause, 1);

        valuesClauses.add(valuesClause.toString());
        valuesClause.setLength(0);
        OutputUtilities.xmlIndent(valuesClause, 1);
      }
    }

    insertClause.append(')');
    element.addElement(new TextElement(insertClause.toString()));

    valuesClause.append(')');
    valuesClauses.add(valuesClause.toString());

    for (String clause : valuesClauses) {
      element.addElement(new TextElement(clause));
    }

    // if table has a generated key, append string like `id = last_insert_id(id)` to updateClause
    // to ensures the returned id always references the inserted entity or the updated entity
    element.addElement(new TextElement("on duplicate key update ${updateClause}" + updateGK));

    parent.addElement(element);
  }

  // add insertSelectiveOrUpdateManually() method
  private void addInsertSelectiveOrUpdateManuallyMethod(Interface interfaze,
      IntrospectedTable introspectedTable) {
    Method method = new Method("insertSelectiveOrUpdateManually");
    context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(FullyQualifiedJavaType.getIntInstance());

    Parameter record = new Parameter(
        new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()), "record");
    record.addAnnotation("@Param(\"record\")");
    method.addParameter(record);
    Parameter updateClause = new Parameter(FullyQualifiedJavaType.getStringInstance(),
        "updateClause");
    updateClause.addAnnotation("@Param(\"updateClause\")");
    method.addParameter(updateClause);

    interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
    interfaze.addMethod(method);
  }

  // add XML element for insertSelectiveOrUpdateManually()
  private void addInsertSelectiveOrUpdateManuallyElement(XmlElement parent,
      IntrospectedTable introspectedTable) {
    XmlElement element = new XmlElement("insert");
    context.getCommentGenerator().addComment(element);

    element.addAttribute(new Attribute("id", "insertSelectiveOrUpdateManually"));
    element.addAttribute(new Attribute("parameterType", "map"));

    GeneratedKey gk = introspectedTable.getGeneratedKey();
    String updateGK = "";
    if (gk != null) {
      IntrospectedColumn introspectedColumn = introspectedTable.getColumn(gk.getColumn());
      if (introspectedColumn != null) {
        if (gk.isJdbcStandard()) {
          element.addAttribute(new Attribute("useGeneratedKeys", "true"));
          element.addAttribute(
              new Attribute("keyProperty", "record." + introspectedColumn.getJavaProperty()));
          element
              .addAttribute(new Attribute("keyColumn", introspectedColumn.getActualColumnName()));
        } else {
          element.addElement(getSelectKeyElement(introspectedColumn, gk));
        }
        updateGK = String.format(", %s = last_insert_id(%s)",
            introspectedColumn.getActualColumnName(), introspectedColumn.getActualColumnName());
      }
    }

    StringBuilder sb = new StringBuilder();

    sb.append("insert into ");
    sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
    element.addElement(new TextElement(sb.toString()));

    XmlElement insertTrimElement = new XmlElement("trim");
    insertTrimElement.addAttribute(new Attribute("prefix", "("));
    insertTrimElement.addAttribute(new Attribute("suffix", ")"));
    insertTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
    element.addElement(insertTrimElement);

    XmlElement valuesTrimElement = new XmlElement("trim");
    valuesTrimElement.addAttribute(new Attribute("prefix", "values ("));
    valuesTrimElement.addAttribute(new Attribute("suffix", ")"));
    valuesTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
    element.addElement(valuesTrimElement);

    for (IntrospectedColumn introspectedColumn : ListUtilities.
        removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {

      if (introspectedColumn.isSequenceColumn()
          || introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
        sb.setLength(0);
        sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
        sb.append(',');
        insertTrimElement.addElement(new TextElement(sb.toString()));

        sb.setLength(0);
        String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
        sb.append(parameterClause.substring(0, 2));
        sb.append("record.");
        sb.append(parameterClause.substring(2));
        sb.append(',');
        valuesTrimElement.addElement(new TextElement(sb.toString()));

        continue;
      }

      XmlElement insertNotNullElement = new XmlElement("if");
      sb.setLength(0);
      sb.append("record.");
      sb.append(introspectedColumn.getJavaProperty());
      sb.append(" != null");
      insertNotNullElement.addAttribute(new Attribute(
          "test", sb.toString()));

      sb.setLength(0);
      sb.append(MyBatis3FormattingUtilities
          .getEscapedColumnName(introspectedColumn));
      sb.append(',');
      insertNotNullElement.addElement(new TextElement(sb.toString()));
      insertTrimElement.addElement(insertNotNullElement);

      XmlElement valuesNotNullElement = new XmlElement("if");
      sb.setLength(0);
      sb.append("record.");
      sb.append(introspectedColumn.getJavaProperty());
      sb.append(" != null");
      valuesNotNullElement.addAttribute(new Attribute(
          "test", sb.toString()));

      sb.setLength(0);
      String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
      sb.append(parameterClause.substring(0, 2));
      sb.append("record.");
      sb.append(parameterClause.substring(2));
      sb.append(',');
      valuesNotNullElement.addElement(new TextElement(sb.toString()));
      valuesTrimElement.addElement(valuesNotNullElement);
    }

    // if table has a generated key, append string like `id = last_insert_id(id)` to updateClause
    // to ensures the returned id always references the inserted entity or the updated entity
    element.addElement(new TextElement("on duplicate key update ${updateClause}" + updateGK));

    parent.addElement(element);
  }

  // add selectManuallyByExample() method
  private void addSelectManuallyByExampleMethod(Interface interfaze,
      IntrospectedTable introspectedTable) {
    Method method = new Method(selectManuallyByExample);
    method.setAbstract(true);
    context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

    method.setVisibility(JavaVisibility.PUBLIC);

//    FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
//    returnType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
    method.setReturnType(genericPlugin.genericModelList);

    Parameter selectClause = new Parameter(FullyQualifiedJavaType.getStringInstance(),
        "selectClause");
    selectClause.addAnnotation("@Param(\"selectClause\")");
    method.addParameter(selectClause);
//    Parameter example = new Parameter(
//        new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example");
    Parameter example = new Parameter(genericPlugin.genericExample, "example");
    example.addAnnotation("@Param(\"example\")");
    method.addParameter(example);

    interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
    interfaze.addMethod(method);
  }

  // add XML element for selectManuallyByExample()
  private void addSelectManuallyByExampleElement(XmlElement parent,
      IntrospectedTable introspectedTable) {
    XmlElement element = new XmlElement("select");
    context.getCommentGenerator().addComment(element);

    element.addAttribute(new Attribute("id", selectManuallyByExample));
    element.addAttribute(new Attribute("parameterType", "map"));
    element.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));

    element.addElement(new TextElement("select"));

    XmlElement distinct = new XmlElement("if");
    distinct.addAttribute(new Attribute("test", "example.distinct"));
    distinct.addElement(new TextElement("distinct"));
    element.addElement(distinct);

    element.addElement(new TextElement("${selectClause} from " +
        introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

    XmlElement example = new XmlElement("if");
    example.addAttribute(new Attribute("test", "_parameter != null"));
    XmlElement includeElement = new XmlElement("include");
    includeElement.addAttribute(
        new Attribute("refid", introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
    example.addElement(includeElement);
    element.addElement(example);

    XmlElement orderBy = new XmlElement("if");
    orderBy.addAttribute(new Attribute("test", "example.orderByClause != null"));
    orderBy.addElement(new TextElement("order by ${example.orderByClause}"));
    element.addElement(orderBy);

    element.addElement(getLimitOffsetClauseElement("example."));

    parent.addElement(element);
  }

  // add selectManuallyByPrimaryKey() method
  private void addSelectManuallyByPrimaryKeyMethod(Interface interfaze,
      IntrospectedTable introspectedTable) {
    Method method = new Method(selectManuallyByPrimaryKey);
    method.setAbstract(true);
    context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

    method.setVisibility(JavaVisibility.PUBLIC);
//    method.setReturnType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
    method.setReturnType(genericPlugin.genericModel);

    Parameter selectClause = new Parameter(FullyQualifiedJavaType.getStringInstance(),
        "selectClause");
    selectClause.addAnnotation("@Param(\"selectClause\")");
    method.addParameter(selectClause);

//    if (introspectedTable.getRules().generatePrimaryKeyClass()) {
//      Parameter key = new Parameter(
//          new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType()), "key");
//      key.addAnnotation("@Param(\"key\")");
//      method.addParameter(key);
//    } else {
//      List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
//      for (IntrospectedColumn introspectedColumn : introspectedColumns) {
//        FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
//        interfaze.addImportedType(type);
//
//        Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
//        parameter.addAnnotation("@Param(\"" + introspectedColumn.getJavaProperty() + "\")");
//        method.addParameter(parameter);
//      }
//    }
    method.addParameter(new Parameter(genericPlugin.genericId, "id", "@Param(\"id\")"));

    interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
    interfaze.addMethod(method);
  }

  // add XML element for selectManuallyByPrimaryKey()
  private void addSelectManuallyByPrimaryKeyElement(XmlElement parent,
      IntrospectedTable introspectedTable) {
    XmlElement element = new XmlElement("select");
    context.getCommentGenerator().addComment(element);

    element.addAttribute(new Attribute("id", selectManuallyByPrimaryKey));
    element.addAttribute(new Attribute("parameterType", "map"));
    element.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));

    element.addElement(new TextElement("select ${selectClause} from " +
        introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

    boolean addPrefix = introspectedTable.getRules().generatePrimaryKeyClass();
    boolean and = false;
    StringBuilder sb = new StringBuilder();
    for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
      sb.setLength(0);
      if (and) {
        sb.append("  and ");
      } else {
        sb.append("where ");
        and = true;
      }

      sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
      sb.append(" = "); //$NON-NLS-1$
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

  // add updateManuallyByExample() method
  private void addUpdateManuallyByExampleMethod(Interface interfaze,
      IntrospectedTable introspectedTable) {
    Method method = new Method(updateManuallyByExample);
    method.setAbstract(true);
    context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(FullyQualifiedJavaType.getIntInstance());

    Parameter updateClause = new Parameter(FullyQualifiedJavaType.getStringInstance(),
        "updateClause");
    updateClause.addAnnotation("@Param(\"updateClause\")");
    method.addParameter(updateClause);
//    Parameter example = new Parameter(
//        new FullyQualifiedJavaType(introspectedTable.getExampleType()), "example");
    Parameter example = new Parameter(genericPlugin.genericExample, "example");
    example.addAnnotation("@Param(\"example\")");
    method.addParameter(example);

    interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
    interfaze.addMethod(method);
  }

  // add XML element for updateManuallyByExample()
  private void addUpdateManuallyByExampleElement(XmlElement parent,
      IntrospectedTable introspectedTable) {
    XmlElement element = new XmlElement("update");
    context.getCommentGenerator().addComment(element);

    element.addAttribute(new Attribute("id", updateManuallyByExample));
    element.addAttribute(new Attribute("parameterType", "map"));

    element.addElement(new TextElement(
        "update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
    element.addElement(new TextElement("set ${updateClause}"));

    XmlElement example = new XmlElement("if");
    example.addAttribute(new Attribute("test", "_parameter != null"));
    XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
    includeElement.addAttribute(
        new Attribute("refid", introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
    example.addElement(includeElement);
    element.addElement(example);

    parent.addElement(element);
  }

  // add updateManuallyByPrimaryKey() method
  private void addUpdateManuallyByPrimaryKeyMethod(Interface interfaze,
      IntrospectedTable introspectedTable) {
    Method method = new Method(updateManuallyByPrimaryKey);
    method.setAbstract(true);
    context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(FullyQualifiedJavaType.getIntInstance());

    Parameter updateClause = new Parameter(FullyQualifiedJavaType.getStringInstance(),
        "updateClause");
    updateClause.addAnnotation("@Param(\"updateClause\")");
    method.addParameter(updateClause);

//    if (introspectedTable.getRules().generatePrimaryKeyClass()) {
//      Parameter key = new Parameter(
//          new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType()), "key");
//      key.addAnnotation("@Param(\"key\")");
//      method.addParameter(key);
//    } else {
//      List<IntrospectedColumn> introspectedColumns = introspectedTable.getPrimaryKeyColumns();
//      for (IntrospectedColumn introspectedColumn : introspectedColumns) {
//        FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
//        interfaze.addImportedType(type);
//
//        Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
//        parameter.addAnnotation("@Param(\"" + introspectedColumn.getJavaProperty() + "\")");
//        method.addParameter(parameter);
//      }
//    }
    method.addParameter(new Parameter(genericPlugin.genericId, "id", "@Param(\"id\")"));

    interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
    interfaze.addMethod(method);
  }

  // add XML element for updateManuallyByPrimaryKey()
  private void addUpdateManuallyByPrimaryKeyElement(XmlElement parent,
      IntrospectedTable introspectedTable) {
    XmlElement element = new XmlElement("update");
    context.getCommentGenerator().addComment(element);

    element.addAttribute(new Attribute("id", updateManuallyByPrimaryKey));
    element.addAttribute(new Attribute("parameterType", "map"));

    element.addElement(new TextElement(
        "update " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
    element.addElement(new TextElement("set ${updateClause}"));

    boolean addPrefix = introspectedTable.getRules().generatePrimaryKeyClass();
    boolean and = false;
    StringBuilder sb = new StringBuilder();
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

  // generate XML element for limit/offset clause
  private XmlElement getLimitOffsetClauseElement(String prefix) {
    if (prefix == null) {
      prefix = "";
    }

    XmlElement element = new XmlElement("if");
    element.addAttribute(new Attribute("test", prefix + "limit != null"));

    XmlElement choose = new XmlElement("choose");

    XmlElement when = new XmlElement("when");
    when.addAttribute(new Attribute("test", prefix + "offset != null"));
    when.addElement(new TextElement("limit ${" + prefix + "offset}, ${" + prefix + "limit}"));

    XmlElement otherwise = new XmlElement("otherwise");
    otherwise.addElement(new TextElement("limit ${" + prefix + "limit}"));

    choose.addElement(when);
    choose.addElement(otherwise);

    element.addElement(choose);
    return element;
  }

  // copied from AbstractXmlElementGenerator, used for selectOrUpdateManually() / selectSelectiveOrUpdateManually()
  // should return an XmlElement for the select key used to automatically generate keys.
  private XmlElement getSelectKeyElement(
      IntrospectedColumn introspectedColumn, GeneratedKey generatedKey) {
    XmlElement element = new XmlElement("selectKey");

    String identityColumnType = introspectedColumn.getFullyQualifiedJavaType()
        .getFullyQualifiedName();
    element.addAttribute(new Attribute("resultType", identityColumnType));
    // add "record." prefix to `keyProperty` for selectOrUpdateManually() / selectSelectiveOrUpdateManually()
    element.addAttribute(
        new Attribute("keyProperty", "record." + introspectedColumn.getJavaProperty()));
    element.addAttribute(new Attribute("order", generatedKey.getMyBatis3Order()));
    element.addElement(new TextElement(generatedKey.getRuntimeSqlStatement()));

    return element;
  }
}
