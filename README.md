MyBatis Generator Plugin Set
=======================

Customized collects of plugins for MyBatis Generator.

Java version: [1.8,), lombok enabled.

Plugins should be in order(check the end);
Example related plugins should enable or disable all

## CreateGenericInterfacePlugin

**Default plugin, must claimed at top**

Fork from [dcendents/CreateGenericInterfacePlugin](https://github.com/dcendents/mybatis-generator-plugins/blob/master/src/main/java/com/github/dcendents/mybatis/generator/plugin/client/CreateGenericInterfacePlugin.java)

> This plugin will create a Mapper interface using java generics as method arguments and return types. It will modify the mappers to extend it with the concrete types. It makes it easier to add utility methods that are agnostic of the exact mapper they are using. e.g.: Maybe a method that will call insert when the id field is null and update when it is not for any model/mapper combination.

Parameters to set:

- **interface**: The fully qualified name of the interface to create.
- **example**: `false`. enable all kinds of examples or not.

```xml
<plugin type="org.mybatis.generator.plugins.CreateGenericInterfacePlugin">
    <property name="interface" value="some.package.InterfaceName" />
    <property name="example" value="true" />
</plugin>
```

## BatchInsertPlugin

Batch insert for mysql.

Parameters to set:

- **methodName**: `insertAll`. batch insert method name.

```xml
<plugin type="org.mybatis.generator.plugins.BatchInsertPlugin" />
```

## CriteriaMethodRenameToRelationalOperatorPlugin

Rename criteria methods name to relational operator format. 
For example: `andIdEqualTo` -> `idEq`, `andIdGreaterThanOrEqualTo` -> `idGe`

```xml
<plugin type="org.mybatis.generator.plugins.CriteriaMethodRenameToRelationalOperatorPlugin" />
```

## ExampleColumnPlugin

Add database column enum to examples.

Parameters to set:

- **columnSuffix**: `true`. enable table name as suffix, `Col` -> `ColUser`

```xml
<plugin type="org.mybatis.generator.plugins.ExampleColumnPlugin">
    <property name="columnSuffix" value="true" />
</plugin>
```

## ExampleEnhancedPlugin

Chain calls for example classes.

e.g.: 
`UserEx.and().ageGe(18).andIf(name != null, e -> e.nameEq(name)).or().gradeLt(3).build();`

support: a or b&c or d&e&f
NOT support: (a or b)&c

```xml
<plugin type="org.mybatis.generator.plugins.ExampleEnhancedPlugin" />
```

## ExampleTargetPlugin

Change example classes package.

Parameters to set:

- **targetPackage**: default is your_entity_package.example

```xml
<plugin type="org.mybatis.generator.plugins.ExampleTargetPlugin" />
```

## ExistByExamplePlugin

Fork from [itfsw/mybatis-generator-plugin](https://github.com/itfsw/mybatis-generator-plugin)

Add existByExample method for mapper

```xml
<plugin type="org.mybatis.generator.plugins.ExistByExamplePlugin" />
```

## IgnoreBlobPlugin

Ignore blob and 'WithBlob' methods

```xml
<plugin type="org.mybatis.generator.plugins.IgnoreBlobPlugin" />
```

## LombokPlugin

Fork from [softwareloop/mybatis-generator-lombok-plugin](https://github.com/softwareloop/mybatis-generator-lombok-plugin)

Lombok supported!

```xml
<plugin type="org.mybatis.generator.plugins.LombokPlugin">
   <property name="data" value="true"/>
   <property name="builder" value="true"/>
   <property name="allArgsConstructor" value="true"/>
   <property name="noArgsConstructor" value="true"/>
</plugin>
```

## MapperChunkPlugin

This plugin would create 'empty' folder under generated xmls, and generate empty mapper xmls.
I usually use two xmls(auto generated and customized) for one mapper class

```xml
<plugin type="org.mybatis.generator.plugins.MapperChunkPlugin" />
```

## PainQueryPlugin

Fork from [dfxyz/mybatis-generator-plugin](https://github.com/dfxyz/mybatis-generator-plugin)

Generate pain query methods: `selectManuallyByExample`,`selectManuallyById`,`updateManuallyByExample`,`updateManuallyById`

```xml
<plugin type="org.mybatis.generator.plugins.PainQueryPlugin" />
```

## RenameMapperIdPlugin

Rename mapper method name, can be applied multi times in order.

Parameters to set:

- **searchString**: search text
- **replaceString**: text to replace
- **regex**: `false`. is regex search

```xml
<plugin type="org.mybatis.generator.plugins.RenameMapperIdPlugin">
    <property name="searchString" value="PrimaryKey" />
    <property name="replaceString" value="Id" />
    <property name="regex" value="true" />
</plugin>
```

## SelectOneByExamplePlugin

Select one record.

```xml
<plugin type="org.mybatis.generator.plugins.SelectOneByExamplePlugin" />
```

## SqlDynamicColumnPlugin

Generate dynamic <sql> in xml files.
For example: `${user}.phone as ${user}_phone`

```xml
<plugin type="org.mybatis.generator.plugins.SqlDynamicColumnPlugin" />
```

---

Full example:

```xml
<generatorConfiguration>
    <classPathEntry location="PATH/TO/mysql-connector-java.jar" />
    <context id="context" defaultModelType="flat">
        <!-- serializable entities -->
        <plugin type="org.mybatis.generator.plugins.SerializablePlugin"/>
        <!-- enable lombok and @Mapper annotation -->
        <plugin type="org.mybatis.generator.plugins.LombokPlugin">
           <!-- enable annotations -->
           <property name="data" value="true"/>
           <property name="builder" value="true"/>
           <property name="allArgsConstructor" value="true"/>
           <property name="noArgsConstructor" value="true"/>
        </plugin>
        <!-- generate basic Mapper -->
        <plugin type="org.mybatis.generator.plugins.CreateGenericInterfacePlugin">
            <property name="interface" value="com.DOMAIN.dao.BaseMapper" />
            <property name="example" value="true" />
        </plugin>
        <!-- ignore blobs -->
        <plugin type="org.mybatis.generator.plugins.IgnoreBlobPlugin" />
        <!-- replace some mapper id(method names) -->
        <plugin type="org.mybatis.generator.plugins.RenameMapperIdPlugin">
            <property name="searchString" value="PrimaryKey" />
            <property name="replaceString" value="Id" />
            <property name="regex" value="true" />
        </plugin>
        <!-- change examples location -->
        <plugin type="org.mybatis.generator.plugins.ExampleTargetPlugin" />
        <!-- batch insert -->
        <plugin type="org.mybatis.generator.plugins.BatchInsertPlugin" />
        <!-- add sql dynamic column -->
        <plugin type="org.mybatis.generator.plugins.SqlDynamicColumnPlugin" />
        <!-- rename examples -->
        <plugin type="org.mybatis.generator.plugins.RenameExampleClassPlugin">
            <property name="searchString" value="Example" />
            <property name="replaceString" value="Ex" />
        </plugin>
        <!-- add pain query support -->
        <plugin type="org.mybatis.generator.plugins.PainQueryPlugin" />
        <!-- SelectOneByExample -->
        <plugin type="org.mybatis.generator.plugins.SelectOneByExamplePlugin" />
        <!-- ExistByExample -->
        <plugin type="org.mybatis.generator.plugins.ExistByExamplePlugin" />
        <!-- Example column enum -->
        <plugin type="org.mybatis.generator.plugins.ExampleColumnPlugin">
            <property name="columnSuffix" value="true" />
        </plugin>
        <!-- Example enhanced -->
        <plugin type="org.mybatis.generator.plugins.ExampleEnhancedPlugin" />
        <!-- generate empty xml files -->
        <plugin type="org.mybatis.generator.plugins.MapperChunkPlugin" />
        <!-- example name to relational operator -->
        <plugin type="org.mybatis.generator.plugins.CriteriaMethodRenameToRelationalOperatorPlugin" />

        <!-- disable all comment -->
        <commentGenerator>
            <property name="suppressAllComments" value="true" />
            <property name="suppressDate" value="true" />
        </commentGenerator>

        <jdbcConnection connectionURL="jdbc:mysql://DOMAIN.com:3306/DATABASE_NAME" 
            driverClass="com.mysql.jdbc.Driver" password="PASSWORD" userId="USERNAME">
            <!-- do not generate code for information schemas -->
            <property name="nullCatalogMeansCurrent" value="true" />
        </jdbcConnection>

        <javaTypeResolver>
            <!-- enable jsr310, @see org.mybatis.generator.config.PropertyRegistry -->
            <property name="useJSR310Types" value="true" />
            <!-- <property name="forceBigDecimals" value="true" /> -->
        </javaTypeResolver>

        <javaModelGenerator targetPackage="com.DOMAIN.entity" targetProject="C:\MBG\generated" />

        <sqlMapGenerator targetPackage="com.DOMAIN.mapper" targetProject="C:\MBG\generated" />

        <javaClientGenerator targetPackage="com.DOMAIN.dao" targetProject="C:\MBG\generated" type="XMLMAPPER" />

        <table tableName="%" alias="" modelType="flat" enableSelectByExample="true" enableDeleteByExample="true" enableCountByExample="true" enableUpdateByExample="true">
            <!-- <generatedKey column="id" sqlStatement="MySql" identity="true"/> -->
            <columnOverride column="UNSIGNED_BIGINT_FIELD" javaType="java.lang.Object" jdbcType="LONG" />
        </table>
    </context>
</generatorConfiguration>
```