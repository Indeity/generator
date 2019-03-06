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

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

import java.util.Arrays;
import java.util.List;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.JavaBeansUtil;

/**
 * add db column enum to example, avoid hard code. used with lombok
 */
public class ExampleColumnPlugin extends PluginAdapter {

  public static final String COLUMN_SUFFIX = "columnSuffix"; // for static import

  public static final String METHOD_EXCLUDES = "excludes"; // exclude method name
  public static final String ENUM_NAME = "Col";  // inner enum name
  private boolean columnSuffix;

  @Override
  public boolean validate(List<String> warnings) {
    String cs = properties.getProperty(COLUMN_SUFFIX);
    columnSuffix = cs == null || isTrue(cs);
    return true;
  }

  @Override
  public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    topLevelClass.addInnerEnum(generateColumnEnum(topLevelClass, introspectedTable));
    return true;
  }

  private InnerEnum generateColumnEnum(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    String enumName = ENUM_NAME;
    FullyQualifiedJavaType stringType = FullyQualifiedJavaType.getStringInstance();
    if (columnSuffix) {
      String entityName = introspectedTable.getBaseRecordType();
      enumName += entityName.substring(entityName.lastIndexOf(".") + 1);
    }

    // inner column enum
    InnerEnum innerEnum = new InnerEnum(new FullyQualifiedJavaType(enumName));
    innerEnum.setVisibility(JavaVisibility.PUBLIC);
    // enum is inner class, no need to be static
//    innerEnum.setStatic(true);
    // lombok getter
    innerEnum.addAnnotation("@Getter");
    topLevelClass.addImportedType("lombok.Getter");

    // generate property and constructor
    // private final String column;
    Field columnField = new Field("column", stringType);
    columnField.setVisibility(JavaVisibility.PRIVATE);
    columnField.setFinal(true);
    innerEnum.addField(columnField);

    // private final String javaProperty;
    Field javaPropertyField = new Field("javaProperty", stringType);
    javaPropertyField.setVisibility(JavaVisibility.PRIVATE);
    javaPropertyField.setFinal(true);
    innerEnum.addField(javaPropertyField);

    // private final String jdbcType;
    Field jdbcTypeField = new Field("jdbcType", stringType);
    jdbcTypeField.setVisibility(JavaVisibility.PRIVATE);
    jdbcTypeField.setFinal(true);
    innerEnum.addField(jdbcTypeField);

    // public String value();
    Method mValue = new Method("v");
    mValue.setVisibility(JavaVisibility.PUBLIC);
    mValue.setReturnType(stringType);
    mValue.addBodyLine("return this.column;");
    innerEnum.addMethod(mValue);

    // add constructor
    Method constructor = new Method(enumName);
    constructor.setConstructor(true);
    constructor.addBodyLine("this.column = column;");
    constructor.addBodyLine("this.javaProperty = javaProperty;");
    constructor.addBodyLine("this.jdbcType = jdbcType;");
    constructor.addParameter(new Parameter(stringType, "column"));
    constructor.addParameter(new Parameter(stringType, "javaProperty"));
    constructor.addParameter(new Parameter(stringType, "jdbcType"));
    innerEnum.addMethod(constructor);

    // add Enums
    for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
      Field field = JavaBeansUtil.getJavaBeansField(column, context, introspectedTable);

      StringBuffer sb = new StringBuffer();
      sb.append(field.getName());
      sb.append("(\"");
      sb.append(column.getActualColumnName());
      sb.append("\", \"");
      sb.append(column.getJavaProperty());
      sb.append("\", \"");
      sb.append(column.getJdbcTypeName());
      sb.append("\")");

      innerEnum.addEnumConstant(sb.toString());
    }

    // desc method
    Method desc = new Method("desc");
    desc.setVisibility(JavaVisibility.PUBLIC);
    desc.setReturnType(stringType);
    desc.addBodyLine("return this.column + \" DESC\";");
    innerEnum.addMethod(desc);

    // asc method
    Method asc = new Method("asc");
    asc.setVisibility(JavaVisibility.PUBLIC);
    asc.setReturnType(stringType);
    asc.addBodyLine("return this.column + \" ASC\";");
    innerEnum.addMethod(asc);

    // alias method, only for select
    Method alias = new Method("as");
    alias.setVisibility(JavaVisibility.PUBLIC);
    alias.setReturnType(stringType);
    alias.addBodyLine("// only for select statements");
    alias.addBodyLine("return this.column + \" as \" + this.javaProperty;");
    innerEnum.addMethod(alias);

    Method eq = new Method("eq");
    eq.setVisibility(JavaVisibility.PUBLIC);
    eq.setReturnType(stringType);
    eq.addParameter(new Parameter(FullyQualifiedJavaType.getObjectInstance(), "value"));
    eq.addBodyLines(Arrays.asList(
        "String v = value == null ? \"NULL\" : value.toString();",
        "if (value != null) {",
        "if (!(value instanceof Number)) {",
        "v = v.replaceAll(\"\\\\\\\\\", \"\\\\\\\\\\\\\\\\\")",
        ".replaceAll(\"\\b\",\"\\\\\\\\b\")",
        ".replaceAll(\"\\n\",\"\\\\\\\\n\")",
        ".replaceAll(\"\\r\", \"\\\\\\\\r\")",
        ".replaceAll(\"\\t\", \"\\\\\\\\t\")",
        ".replaceAll(\"\\\\x1A\", \"\\\\\\\\Z\")",
        ".replaceAll(\"\\\\x00\", \"\\\\\\\\0\")",
        ".replaceAll(\"'\", \"\\\\\\\\'\")",
        ".replaceAll(\"\\\"\", \"\\\\\\\\\\\"\");",
        "v = \"\\\"\" + v + \"\\\"\";",
        "}",
        "}",
        "return this.column + \"=\" + v;"
    ));
    innerEnum.addMethod(eq);

    // excludes method
    topLevelClass.addImportedType("java.util.Arrays");
    topLevelClass.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());

    Method mExcludes = new Method(METHOD_EXCLUDES);
    mExcludes.setVisibility(JavaVisibility.PUBLIC);
//    mExcludes.setReturnType(new FullyQualifiedJavaType(enumName + "[]"));
    mExcludes.setReturnType(stringType);
    mExcludes.addParameter(new Parameter(innerEnum.getType(), "excludes", true));
    mExcludes.setStatic(true);
    mExcludes.addBodyLines(Arrays.asList(
        String.format(
            "ArrayList<%1$s> columns = new ArrayList<>(Arrays.asList(%1$s.values()));", enumName),
        "if (excludes != null && excludes.length > 0) {",
        "columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));",
        "}"
    ));
    mExcludes.addBodyLines(Arrays.asList(
        "StringBuilder sb = new StringBuilder();",
        String.format("for (%s column : columns) {", enumName),
        "sb.append(column.as()).append(',');",
        "}",
        "sb.setLength(sb.length() - 1);"
    ));
    mExcludes.addBodyLine("return sb.toString();");
//    mExcludes.addBodyLine("return columns.toArray(new " + enumName + "[]{});");
    innerEnum.addMethod(mExcludes);

    // ==================== update builder =====================
    InnerClass updateBuilder = new InnerClass(new FullyQualifiedJavaType("UpdateBuilder"));
    updateBuilder.setStatic(true);
    updateBuilder.setVisibility(JavaVisibility.PUBLIC);
    innerEnum.addInnerClass(updateBuilder);

    // static update method
    Method mUpdate = new Method("update");
    mUpdate.setStatic(true);
    mUpdate.setReturnType(updateBuilder.getType());
    mUpdate.setVisibility(JavaVisibility.PUBLIC);
    mUpdate.addBodyLine("return new UpdateBuilder();");
    innerEnum.addMethod(mUpdate);

    // UpdateBuilder fields
    Field fUpdate = new Field("updates", new FullyQualifiedJavaType("java.util.List<String>"));
    fUpdate.setInitializationString("new ArrayList()");
    fUpdate.setVisibility(JavaVisibility.PRIVATE);
    updateBuilder.addField(fUpdate);
    // build method
    Method mfBuild = new Method("build");
    mfBuild.setVisibility(JavaVisibility.PUBLIC);
    mfBuild.setReturnType(stringType);
    mfBuild.addBodyLine("return String.join(\",\", updates);");
    updateBuilder.addMethod(mfBuild);
    // iterator columns
    for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
      Field field = JavaBeansUtil.getJavaBeansField(column, context, introspectedTable);
      // builder method
      Method m = new Method(field.getName());
      m.setVisibility(JavaVisibility.PUBLIC);
      m.setReturnType(updateBuilder.getType());
      m.addParameter(new Parameter(field.getType(), "value"));
      m.addBodyLine("updates.add(" + field.getName() + ".eq(value));");
      m.addBodyLine("return this;");
      updateBuilder.addMethod(m);
    }

    return innerEnum;
  }

}
