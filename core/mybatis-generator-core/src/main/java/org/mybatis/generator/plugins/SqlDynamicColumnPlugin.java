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

import static org.mybatis.generator.plugins.CreateGenericInterfacePlugin.formatLines;

import java.util.ArrayList;
import java.util.List;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * sql dynamic column for joined table alias, like ${user}.phone as ${user}_phone
 * <pre>
 *   &lt;plugin type="org.mybatis.generator.plugins.SqlDynamicColumnPlugin" /&gt;
 * </pre>
 */
public class SqlDynamicColumnPlugin extends PluginAdapter {

  private String fullDynamic = "Dynamic_Column_List";
  private String oneToOne = "Table_Dynamic_Column_List";
  private String tableProperty = "tb"; // the table join alias

  private String aliasDynamic = "Table_Alias_Dynamic_Column_List";
  private String aliasPrefix = "pfx"; // alias column prefix


  @Override
  public boolean validate(List<String> warnings) {
    return true;
  }

  @Override
  public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
    // create full Dynamic
    XmlElement fullDynamicEle = new XmlElement("sql");
    fullDynamicEle.addAttribute(new Attribute("id", fullDynamic));
    // build alias
    List<String> columns = new ArrayList<>();
    for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
      columns.add(String.format("${%1$s}.%2$s as ${%1$s}_%2$s", tableProperty,
          column.getActualColumnName()));
    }
    formatLines(fullDynamicEle, columns, ',', false, 0);
    document.getRootElement().addElement(fullDynamicEle);

    // create oneToOne Dynamic
    XmlElement oneToOneEle = new XmlElement("sql");
    oneToOneEle.addAttribute(new Attribute("id", oneToOne));
    // build alias
    columns.clear();
    for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
      columns.add(String.format("${%1$s}.%2$s as %2$s", tableProperty,
          column.getActualColumnName()));
    }
    formatLines(oneToOneEle, columns, ',', false, 0);
    document.getRootElement().addElement(oneToOneEle);

    // alias Dynamic
    XmlElement aliasEle = new XmlElement("sql");
    aliasEle.addAttribute(new Attribute("id", aliasDynamic));
    columns.clear();
    for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
      columns.add(String.format("${%1$s}.%3$s as ${%2$s}_%3$s",
          tableProperty, aliasPrefix, column.getActualColumnName()));
    }
    formatLines(aliasEle, columns, ',', false, 0);
    document.getRootElement().addElement(aliasEle);

    return true;
  }


}
