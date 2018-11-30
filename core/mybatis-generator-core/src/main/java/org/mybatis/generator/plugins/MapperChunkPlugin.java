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

import java.util.Arrays;
import java.util.List;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.DefaultXmlFormatter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.XmlConstants;

/**
 * used this plugin only when initialize mapper files
 *
 * chunk mapper xml file to generated and hand-writing parts
 * <p>
 * this plugin would generate empty xml files in empty folder under xml mapper path
 */
public class MapperChunkPlugin extends PluginAdapter {

//  private String generatedPackage;
//  private String mapperPackage;

  @Override
  public boolean validate(List<String> warnings) {
    return true;
  }

  @Override
  public void initialized(IntrospectedTable introspectedTable) {
//    if (generatedPackage == null) {
//      generatedPackage = introspectedTable.getMyBatis3XmlMapperPackage();
//      mapperPackage = generatedPackage + ".generated";
//    }

  }

  @Override
  public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(
      IntrospectedTable introspectedTable) {
    // create document
    Document document = new Document(XmlConstants.MYBATIS3_MAPPER_PUBLIC_ID,
        XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);
    XmlElement root = new XmlElement("mapper");
    root.addAttribute(new Attribute("namespace", introspectedTable.getMyBatis3JavaMapperType()));
    root.addElement(new TextElement(""));
    document.setRootElement(root);

    String fileName = introspectedTable.getMyBatis3XmlMapperFileName();
    String targetPackage = introspectedTable.getMyBatis3XmlMapperPackage() + ".empty";
    String targetProject = context.getSqlMapGeneratorConfiguration().getTargetProject();

    GeneratedXmlFile xml = new GeneratedXmlFile(document, fileName,
        targetPackage, targetProject, true, new DefaultXmlFormatter());

    return Arrays.asList(xml);
  }

}
