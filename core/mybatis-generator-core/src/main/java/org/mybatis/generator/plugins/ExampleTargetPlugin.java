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

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.util.List;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;

/**
 * Example classes package setting
 * <code>
 *   &lt;plugin type="org.mybatis.generator.plugins.ExampleTargetPlugin" /&gt;
 * </code>
 *
 * @see <a href="https://github.com/itfsw/mybatis-generator-plugin">itfsw/mybatis-generator-plugin</a>
 */
public class ExampleTargetPlugin extends PluginAdapter {

  public static final String TARGET_PACKAGE = "targetPackage";
  private static String targetPackage;

  @Override
  public boolean validate(List<String> warnings) {
    // null targetPackage is allowed here
    targetPackage = properties.getProperty(TARGET_PACKAGE);
//    if (!stringHasValue(targetPackage)) {
//      warnings.add("ExampleTargetPlugin targetPackage not set");
//      return false;
//    }
    return true;
  }

  @Override
  public void initialized(IntrospectedTable introspectedTable) {
    String exampleType = introspectedTable.getExampleType();

    // edit package name
    Context context = getContext();
    JavaModelGeneratorConfiguration configuration = context.getJavaModelGeneratorConfiguration();
    String targetPackage = configuration.getTargetPackage();
    if (!stringHasValue(this.targetPackage)) {
      this.targetPackage = targetPackage + ".example";
    }
    String newExampleType = exampleType.replace(targetPackage, this.targetPackage);
    introspectedTable.setExampleType(newExampleType);
  }

}
