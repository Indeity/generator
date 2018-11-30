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

import static org.mybatis.generator.internal.util.StringUtility.isTrue;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

/**
 * usage 1: without regex
 * <pre>
 *   searchString = updateByPrimaryKeySelective,BaseResultMap
 *   replaceString = updateByIdSelect,MyResultMap
 * </pre>
 * usage 2: with regex and replace
 * <pre>
 *   searchString = PrimaryKey
 *   replaceString = Id
 *   regex = true
 * </pre>
 * if you need both replace and regex, just apply this plugin multi times!
 *
 * @author YinJH
 */
public class RenameMapperIdPlugin extends PluginAdapter {

  public static final String SEARCH_STRING = "searchString";
  public static final String REPLACE_STRING = "replaceString";
  public static final String REGEX = "regex";

  private String[] searchStrings;
  private String[] replaceStrings;

  private String searchString;
  private String replaceString;
  private Pattern pattern;
  private boolean regex;

  @Override
  public boolean validate(List<String> warnings) {

    searchString = properties.getProperty(SEARCH_STRING);
    replaceString = properties.getProperty(REPLACE_STRING);
    regex = isTrue(properties.getProperty(REGEX));

    boolean valid = stringHasValue(searchString) && stringHasValue(replaceString);
    if (valid) {
      if (regex) {
        pattern = Pattern.compile(searchString);
      } else {
        searchStrings = searchString.split(",");
        replaceStrings = replaceString.split(",");
      }
    } else {
      warnings.add("Invalid Rename Mapper Id Params");
    }

    return valid;
  }

  private String replace(String old) {
    if (regex) {
      Matcher matcher = pattern.matcher(old);
      return matcher.replaceAll(replaceString);
    } else {
      for (int i = 0; i < searchStrings.length; i++) {
        if (searchStrings[i].equals(old)) {
          return replaceStrings[i];
        }
      }
      return old;
    }
  }

  @Override
  public void initialized(IntrospectedTable introspectedTable) {
    String old;
    // =========================== updates ===========================
    // updateByPrimaryKey
    old = introspectedTable.getUpdateByPrimaryKeyStatementId();
    introspectedTable.setUpdateByPrimaryKeyStatementId(replace(old));

    // updateByPrimaryKeySelective
    old = introspectedTable.getUpdateByPrimaryKeySelectiveStatementId();
    introspectedTable.setUpdateByPrimaryKeySelectiveStatementId(replace(old));

    // updateByPrimaryKeyWithBLOBs
    old = introspectedTable.getUpdateByPrimaryKeyWithBLOBsStatementId();
    introspectedTable.setUpdateByPrimaryKeyWithBLOBsStatementId(replace(old));

    // updateByExample
    old = introspectedTable.getUpdateByExampleStatementId();
    introspectedTable.setUpdateByExampleStatementId(replace(old));

    // updateByExampleSelective
    old = introspectedTable.getUpdateByExampleSelectiveStatementId();
    introspectedTable.setUpdateByExampleSelectiveStatementId(replace(old));

    // updateByExampleWithBLOBs
    old = introspectedTable.getUpdateByExampleWithBLOBsStatementId();
    introspectedTable.setUpdateByExampleWithBLOBsStatementId(replace(old));

    // =========================== deletes ===========================
    // deleteByPrimaryKey
    old = introspectedTable.getDeleteByPrimaryKeyStatementId();
    introspectedTable.setDeleteByPrimaryKeyStatementId(replace(old));

    // deleteByExample
    old = introspectedTable.getDeleteByExampleStatementId();
    introspectedTable.setDeleteByExampleStatementId(replace(old));

    // =========================== selects ===========================
    // countByExample
    old = introspectedTable.getCountByExampleStatementId();
    introspectedTable.setCountByExampleStatementId(replace(old));

    // selectAll
    old = introspectedTable.getSelectAllStatementId();
    introspectedTable.setSelectAllStatementId(replace(old));

    // selectByPrimaryKey
    old = introspectedTable.getSelectByPrimaryKeyStatementId();
    introspectedTable.setSelectByPrimaryKeyStatementId(replace(old));

    // selectByExample
    old = introspectedTable.getSelectByExampleStatementId();
    introspectedTable.setSelectByExampleStatementId(replace(old));

    // selectByExampleWithBLOBs
    old = introspectedTable.getSelectByExampleWithBLOBsStatementId();
    introspectedTable.setSelectByExampleWithBLOBsStatementId(replace(old));

    // =========================== inserts ===========================
    // insert
    old = introspectedTable.getInsertStatementId();
    introspectedTable.setInsertStatementId(replace(old));

    // insertSelective
    old = introspectedTable.getInsertSelectiveStatementId();
    introspectedTable.setInsertSelectiveStatementId(replace(old));

    // =========================== xml mapper ids ===========================
    // Base_Column_List
    old = introspectedTable.getBaseColumnListId();
    introspectedTable.setBaseColumnListId(replace(old));

    // Blob_Column_List
    old = introspectedTable.getBlobColumnListId();
    introspectedTable.setBlobColumnListId(replace(old));

    // BaseResultMap
    old = introspectedTable.getBaseResultMapId();
    introspectedTable.setBaseResultMapId(replace(old));

    // ResultMapWithBLOBs
    old = introspectedTable.getResultMapWithBLOBsId();
    introspectedTable.setResultMapWithBLOBsId(replace(old));

    // Example_Where_Clause
    old = introspectedTable.getExampleWhereClauseId();
    introspectedTable.setExampleWhereClauseId(replace(old));

    // Update_By_Example_Where_Clause
    old = introspectedTable.getMyBatis3UpdateByExampleWhereClauseId();
    introspectedTable.setMyBatis3UpdateByExampleWhereClauseId(replace(old));
  }
}
