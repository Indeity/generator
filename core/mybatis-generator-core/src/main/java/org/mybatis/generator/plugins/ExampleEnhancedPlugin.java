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
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * Example only support simple criteria, such as: a or b&amp;c or d&amp;e&amp;f
 * DO NOT support: (a or b)&amp;c
 *
 * java 8 is needed for some functions in this plugin
 *
 * <p> enhance example, e.g. chain call, column support
 *
 * @see <a href="https://stackoverflow.com/questions/9717130/">complex where</a>
 */
public class ExampleEnhancedPlugin extends PluginAdapter {

  private static FullyQualifiedJavaType stringType = FullyQualifiedJavaType.getStringInstance();

  @Override
  public boolean validate(List<String> warnings) {
    return true;
  }

  @Override
  public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {

    List<InnerClass> innerClasses = topLevelClass.getInnerClasses();
    for (InnerClass innerClass : innerClasses) {
      if ("Criteria".equals(innerClass.getType().getShortName())) {
        // NEVER call User.Criteria outside here! just chain forever!
        innerClass.setStatic(false);
        // add static criteria create
        criteria(topLevelClass, introspectedTable, innerClass);
        // add build
        build(topLevelClass, introspectedTable, innerClass);
        // add or inside criteria, criteria only support (a AND b) OR (c AND d)
        or(topLevelClass, introspectedTable, innerClass);
        // andIf, java 8, check condition without break code chain! only work for more ands
        andIf(topLevelClass, introspectedTable, innerClass);
      }
    }

    // distinct
    distinct(topLevelClass, introspectedTable);

    // orderBy method
    orderBy(topLevelClass, introspectedTable);

    return true;
  }

  private void distinct(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    Method method = new Method("distinct");
    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(topLevelClass.getType());
    method.addBodyLine("this.distinct = true;");
    method.addBodyLine("return this;");
    topLevelClass.addMethod(method);
  }

  private void orderBy(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    // query order by, e.g. "id asc, created desc"
    Method method = new Method("orderBy");
    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(topLevelClass.getType());
    method.addParameter(new Parameter(stringType, "orderByClause"));
    method.addBodyLine("this.orderByClause = orderByClause;");
    method.addBodyLine("return this;");
    topLevelClass.addMethod(method);

    // order by with columns, orderBy(String... orderBys){}
    Method varMethod = new Method("orderBy");
    varMethod.setVisibility(JavaVisibility.PUBLIC);
    varMethod.setReturnType(topLevelClass.getType());
    varMethod.addParameter(new Parameter(stringType, "orderBys", true));
    varMethod.addBodyLine("this.orderByClause = String.join(\",\", orderBys);");
    varMethod.addBodyLine("return this;");
    topLevelClass.addMethod(varMethod);
  }

  private void criteria(TopLevelClass topLevelClass, IntrospectedTable introspectedTable,
      InnerClass innerClass) {
    Method method = new Method("and"); // criteria/where/and
    method.setVisibility(JavaVisibility.PUBLIC);
    method.setStatic(true);
    method.setReturnType(innerClass.getType());
    method.addBodyLine(
        String.format("%1$s hook = new %1$s();", topLevelClass.getType().getShortName()));
    method.addBodyLine("return hook.createCriteria();");
    topLevelClass.addMethod(method);
  }

  private void build(TopLevelClass topLevelClass, IntrospectedTable introspectedTable,
      InnerClass innerClass) {
    Method method = new Method("build");
    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(topLevelClass.getType());
    method.addBodyLine(String.format("return %s.this;", topLevelClass.getType().getShortName()));
    innerClass.addMethod(method);
  }

  private void or(TopLevelClass topLevelClass, IntrospectedTable introspectedTable,
      InnerClass innerClass) {
    Method method = new Method("or");
    method.setVisibility(JavaVisibility.PUBLIC);
    method.setReturnType(innerClass.getType());
    method
        .addBodyLine(String.format("return %s.this.or();", topLevelClass.getType().getShortName()));
    innerClass.addMethod(method);
  }

  private void andIf(TopLevelClass topLevelClass, IntrospectedTable introspectedTable,
      InnerClass innerClass) {
    // create consumer type
    FullyQualifiedJavaType consumerType = new FullyQualifiedJavaType("Consumer");
    consumerType.addTypeArgument(innerClass.getType());

    Method method = new Method("andIf");
    method.setVisibility(JavaVisibility.PUBLIC);
    method.addParameter(
        new Parameter(FullyQualifiedJavaType.getBooleanPrimitiveInstance(), "andFlag"));
    method.addParameter(new Parameter(consumerType, "consumer"));
    method.setReturnType(innerClass.getType());
    method.addBodyLine("if (andFlag) consumer.accept(this);");
    method.addBodyLine("return this;");
    topLevelClass.addImportedType("java.util.function.Consumer");
    innerClass.addMethod(method);
  }

}
