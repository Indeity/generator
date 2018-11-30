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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * rename criteria methods name to relational operator format
 *
 * @see <a href="https://github.com/mybatis/generator/issues/204">mbg/issues-204</a>
 * @author YinJH
 */
public class CriteriaMethodRenameToRelationalOperatorPlugin extends PluginAdapter {

  /**
   * all possible method end with:
   * IsNull, IsNotNull, EqualTo, NotEqualTo, GreaterThan, GreaterThanOrEqualTo,
   * LessThan, LessThanOrEqualTo, Like, NotLike, In, NotIn, Between, NotBetween
   */
  private Map<String, String> renameMap = new LinkedHashMap<String, String>() {{
    // full named with same suffix should put first
    put("GreaterThanOrEqualTo", "Ge");
    put("LessThanOrEqualTo", "Le");
    put("NotEqualTo", "Ne");
    put("EqualTo", "Eq");
    put("GreaterThan", "Gt");
    put("LessThan", "Lt");
  }};

  @Override
  public boolean validate(List<String> warnings) {
    return true;
  }

  @Override
  public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
      IntrospectedTable introspectedTable) {
    // check if criteria exist
    InnerClass criteria = getInnerClass(topLevelClass, "Criteria");
    if (criteria == null) {
      return true;
    }
    // get GeneratedCriteria
    InnerClass generatedCriteria = getInnerClass(topLevelClass, "GeneratedCriteria");
    if (generatedCriteria == null) {
      return true;
    }

    for (Method method : generatedCriteria.getMethods()) {
      String name = method.getName();
      if (name.startsWith("and")) {
        for (Entry<String, String> entry : renameMap.entrySet()) {
          if (name.endsWith(entry.getKey())) {
            // andIdEqualTo -> idEq
            String prop = name.substring(3, name.length() - entry.getKey().length());
            prop = prop.substring(0, 1).toLowerCase() + prop.substring(1);
            name = prop.concat(entry.getValue());
            method.setName(name);
            continue;
          }
        }
      }
    }

    return true;
  }

  private InnerClass getInnerClass(TopLevelClass topLevelClass, String name) {
    InnerClass inner = null;
    for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
      if (name.equals(innerClass.getType().getShortName())) {
        inner = innerClass;
        break;
      }
    }
    return inner;
  }

}
