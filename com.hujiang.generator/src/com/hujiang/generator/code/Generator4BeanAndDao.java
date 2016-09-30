package com.hujiang.generator.code;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import lombok.Data;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.SQL;

import com.google.common.base.CaseFormat;
import com.hujiang.generator.config.PropertiesOperate;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class Generator4BeanAndDao {

	private boolean idExist;

	private static final String ID = "ID";

	private static final String COLUMNS = "COLUMNS";

	private static final String COLUMNS_ALL = "COLUMNS_ALL";

	/**
	 * 代码生成
	 * 
	 * @param table
	 * @param pk
	 * @param filePath
	 * @throws IOException
	 */
	public void generateCode(String table, String pk, String filePath) throws Exception {
		Map<String, Integer> columnMap = this.getColumnFromDBTable(table);

		// 生成Bean文件
		AnnotationSpec beanAnnotationSpec = AnnotationSpec.builder(Data.class).build();

		TypeSpec bean = TypeSpec.classBuilder(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, table)).addModifiers(Modifier.PUBLIC).addFields(this.generateBeanFields(columnMap))
				.addAnnotation(beanAnnotationSpec).addSuperinterface(Serializable.class).build();

		JavaFile javaFile = JavaFile.builder(pk, bean).build();
		javaFile.writeTo(new File(filePath));

		// 生成Dao文件
		AnnotationSpec annotationSpec = AnnotationSpec.builder(DB.class).addMember("dataSource", "Consts.DB_HUJIANG").addMember("table", "$S", table).build();
		TypeSpec dao = TypeSpec.interfaceBuilder(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, table) + "Dao").addModifiers(Modifier.PUBLIC).addAnnotation(annotationSpec)
				.addFields(generateDaoFields(columnMap)).addMethods(generateDaoMethods(columnMap, table, pk)).build();
		javaFile = JavaFile.builder(pk, dao).build();
		javaFile.writeTo(new File(filePath));
	}

	/**
	 * 生成Dao methods
	 * 
	 * @param table
	 * @return
	 */
	private List<MethodSpec> generateDaoMethods(Map<String, Integer> columnMap, String table, String pk) {
		List<MethodSpec> methodList = new ArrayList<MethodSpec>();

		// 构建select语句
		methodList.add(this.buildDaoSelectObjectMethod(table, pk));
		methodList.add(this.buildDaoSelectMethod(table, pk));

		// 构建delete语句
		methodList.add(this.buildDaoDeleteMethod(table));

		// 构建insert语句
		methodList.add(this.buildDaoInsertMethod(columnMap, table, pk));

		// 构建update语句
		methodList.add(this.buildDaoUpdateMethod(columnMap, table, pk));

		return methodList;
	}

	/**
	 * 构建Dao update接口
	 * 
	 * @param columnMap
	 * @param table
	 * @return
	 */
	private MethodSpec buildDaoUpdateMethod(Map<String, Integer> columnMap, String table, String pk) {
		AnnotationSpec annotationSpec = AnnotationSpec.builder(SQL.class).addMember("value", this.buildDaoUpdateSql(columnMap)).build();
		TypeName tableType = ClassName.get(pk, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, table));
		return MethodSpec.methodBuilder("update" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, table)).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).addAnnotation(annotationSpec)
				.addParameter(ParameterSpec.builder(tableType, table).build()).returns(int.class).build();
	}

	private String buildDaoUpdateSql(Map<String, Integer> columnMap) {
		StringBuilder update = new StringBuilder("\"update #table set ");
		StringBuilder sb = new StringBuilder("");
		int i = 1;
		int max = columnMap.size();
		for (Map.Entry<String, Integer> entry : columnMap.entrySet()) {
			if (ID.toLowerCase().equalsIgnoreCase(entry.getKey())) {
				idExist = true;
				max--;
				continue;
			}
			sb.append(entry.getKey()).append("=").append(":1.").append(entry.getKey());
			if (i != max) {
				sb.append(", ");
			}

			i++;
		}

		update.append(sb.toString());

		if (idExist) {
			update.append(" where id=:1.id\"");
		} else {
			update.append(" where 1=1 limit 1\"");
		}

		return update.toString();
	}

	/**
	 * 构建Dao insert接口
	 * 
	 * @param columnMap
	 * @param table
	 * @return
	 */
	private MethodSpec buildDaoInsertMethod(Map<String, Integer> columnMap, String table, String pk) {
		AnnotationSpec annotationSpec = AnnotationSpec.builder(SQL.class).addMember("value", this.buildDaoInsertSql(columnMap)).build();
		TypeName tableType = ClassName.get(pk, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, table));
		return MethodSpec.methodBuilder("insert" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, table)).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).addAnnotation(annotationSpec)
				.addParameter(ParameterSpec.builder(tableType, table).build()).returns(int.class).build();
	}

	private String buildDaoInsertSql(Map<String, Integer> columnMap) {
		StringBuilder insert = new StringBuilder("\"insert into #table( \" + COLUMNS + \") values(");
		StringBuilder sb = new StringBuilder("");
		int i = 1;
		int max = columnMap.size();
		for (Map.Entry<String, Integer> entry : columnMap.entrySet()) {
			if (ID.toLowerCase().equalsIgnoreCase(entry.getKey())) {
				idExist = true;
				max--;
				continue;
			}

			if (i == max) {
				sb.append(":1.").append(entry.getKey());
			} else {
				sb.append(":1.").append(entry.getKey()).append(", ");
			}

			i++;
		}

		insert.append(sb.toString()).append(")\"");

		return insert.toString();
	}

	/**
	 * 构建Dao delete接口
	 * 
	 * @param table
	 * @return
	 */
	private MethodSpec buildDaoDeleteMethod(String table) {
		AnnotationSpec annotationSpec = AnnotationSpec.builder(SQL.class).addMember("value", this.buildDaoDelSql()).build();
		MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("delete" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, table)).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addAnnotation(annotationSpec).returns(int.class);
		if (idExist) {
			methodSpecBuilder.addParameter(ParameterSpec.builder(long.class, ID.toLowerCase()).build());
		}

		return methodSpecBuilder.build();
	}

	/**
	 * 构建Dao select Object 接口
	 * 
	 * @param table
	 * @return
	 */
	private MethodSpec buildDaoSelectObjectMethod(String table, String pk) {
		AnnotationSpec annotationSpec = AnnotationSpec.builder(SQL.class).addMember("value", this.buildDaoSelectSql()).build();
		TypeName tableType = ClassName.get(pk, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, table));
		MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("select" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, table)).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addAnnotation(annotationSpec).returns(tableType);
		if (idExist) {
			methodSpecBuilder.addParameter(ParameterSpec.builder(long.class, ID.toLowerCase()).build());
		}

		return methodSpecBuilder.build();
	}

	/**
	 * 构建Dao select接口
	 * 
	 * @param table
	 * @return
	 */
	private MethodSpec buildDaoSelectMethod(String table, String pk) {
		AnnotationSpec annotationSpec = AnnotationSpec.builder(SQL.class).addMember("value", this.buildDaoSelectSql()).build();
		ClassName list = ClassName.get("java.util", "List");
		TypeName tableType = ClassName.get(pk, CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, table));
		TypeName typeName = ParameterizedTypeName.get(list, tableType);
		MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("select" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, table) + "List").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addAnnotation(annotationSpec).returns(typeName);
		if (idExist) {
			methodSpecBuilder.addParameter(ParameterSpec.builder(long.class, ID.toLowerCase()).build());
		}

		return methodSpecBuilder.build();
	}

	private String buildDaoDelSql() {
		StringBuilder del = new StringBuilder("\"delete from #table");
		if (idExist) {
			del.append(" where id=:1\"");
		} else {
			del.append(" where 1=1 limit 1\"");
		}

		return del.toString();
	}

	private String buildDaoSelectSql() {
		StringBuilder select = new StringBuilder("\"select \" + ");
		if (idExist) {
			select.append("COLUMNS_ALL + ");
		} else {
			select.append("COLUMNS + ");
		}
		select.append("\" from #table");
		if (idExist) {
			select.append(" where id=:1\"");
		} else {
			select.append(" where 1=1 limit 1\"");
		}

		return select.toString();
	}

	/**
	 * 生成Dao属性
	 * 
	 * @param columnMap
	 * @return
	 */
	private List<FieldSpec> generateDaoFields(Map<String, Integer> columnMap) {
		List<FieldSpec> member = new ArrayList<FieldSpec>();
		StringBuilder sb = new StringBuilder("");
		int i = 1;
		int max = columnMap.size();
		for (Map.Entry<String, Integer> entry : columnMap.entrySet()) {
			if (ID.toLowerCase().equalsIgnoreCase(entry.getKey())) {
				idExist = true;
				max--;
				continue;
			}

			if (i == max) {
				sb.append(entry.getKey());
			} else {
				sb.append(entry.getKey()).append(", ");
			}

			i++;
		}

		FieldSpec cloumns = FieldSpec.builder(String.class, COLUMNS, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("$S", sb.toString()).build();

		if (idExist) {
			FieldSpec id = FieldSpec.builder(String.class, ID, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("$S", ID.toLowerCase()).build();

			FieldSpec cloumnsAll = FieldSpec.builder(String.class, COLUMNS_ALL, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer(ID + " + \",\" + " + COLUMNS).build();

			member.add(id);
			member.add(cloumns);
			member.add(cloumnsAll);
		} else {
			member.add(cloumns);
		}

		return member;
	}

	/**
	 * 生成Bean属性内容
	 * 
	 * @param columnMap
	 * @return
	 */
	private List<FieldSpec> generateBeanFields(Map<String, Integer> columnMap) {
		List<FieldSpec> fields = new ArrayList<FieldSpec>();
		for (Map.Entry<String, Integer> entry : columnMap.entrySet()) {
			if (entry.getValue() == Types.VARCHAR || entry.getValue() == Types.CHAR) {
				fields.add(this.createFieldSpec(String.class, entry.getKey()));
			} else if (entry.getValue() == Types.INTEGER || entry.getValue() == Types.TINYINT) {
				fields.add(this.createFieldSpec(Integer.class, entry.getKey()));
			} else if (entry.getValue() == Types.BIGINT) {
				fields.add(this.createFieldSpec(Long.class, entry.getKey()));
			} else if (entry.getValue() == Types.DATE || entry.getValue() == Types.TIME || entry.getValue() == Types.TIMESTAMP) {
				fields.add(this.createFieldSpec(java.util.Date.class, entry.getKey()));
			} else if (entry.getValue() == Types.FLOAT || entry.getValue() == Types.DOUBLE) {
				fields.add(this.createFieldSpec(Double.class, entry.getKey()));
			} else if (entry.getValue() == Types.DECIMAL) {
				fields.add(this.createFieldSpec(BigDecimal.class, entry.getKey()));
			}
		}

		return fields;
	}

	/**
	 * 创建FieldSpec
	 * 
	 * @param clz
	 * @param columnName
	 * @return
	 */
	private FieldSpec createFieldSpec(Class<?> clz, String columnName) {
		return FieldSpec.builder(clz, columnName, Modifier.PRIVATE).build();
	}

	/**
	 * 从db获取指定table的属性字段及其类型
	 * 
	 * @param ip
	 * @param port
	 * @param database
	 * @param table
	 * @param uname
	 * @param pwd
	 * @return
	 */
	private Map<String, Integer> getColumnFromDBTable(String table) throws Exception {
		Map<String, Integer> columnMap = new HashMap<String, Integer>();
		Connection conn = null;
		try {
			String driver = PropertiesOperate.getString("jdbc.driver");
			Class.forName(driver);
			conn = DriverManager.getConnection(PropertiesOperate.getString("jdbc.url"), PropertiesOperate.getString("jdbc.username"), PropertiesOperate.getString("jdbc.password"));
			PreparedStatement statement = conn.prepareStatement("select * from " + table + " limit 1");
			ResultSetMetaData rsm = statement.getMetaData();
			for (int i = 1; i <= rsm.getColumnCount(); i++) {
				columnMap.put(rsm.getColumnName(i), rsm.getColumnType(i));
			}

			statement.close();
		} finally {
			if (conn != null) {
				try {
					conn.close();
					conn = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return columnMap;
	}
}
