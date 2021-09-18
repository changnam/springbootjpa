package com.honsoft.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@PropertySource(value = { "classpath:jdbc.properties" }, ignoreResourceNotFound = true)
@EnableJpaRepositories(basePackages = "com.honsoft.repository", entityManagerFactoryRef = "mysqlEntityManagerFactory", transactionManagerRef = "mysqlTransactionManager")
public class DataSourceConfigMysql {

	@Autowired
	private Environment env;

	@Bean
	@ConfigurationProperties(prefix = "mysql.datasource")
	public DataSourceProperties mysqlDataSourceProperties() {
		DataSourceProperties properties = new DataSourceProperties();
		
		return properties;
	}
	// --------------------------------------------------------------------------------------------
	// datasource
	@Bean(name = "mysqlDataSource", destroyMethod = "close")
	public DataSource mysqlDataSource() {
		return mysqlDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

	@Bean
	public DataSourceInitializer mysqlDataSourceInitializer(@Qualifier("mysqlDataSource") DataSource datasource) {
		ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
		//resourceDatabasePopulator.addScript(new ClassPathResource("ddl/mysql/schema-mysql.sql"));
		//resourceDatabasePopulator.addScript(new ClassPathResource("ddl/mysql/data-mysql.sql"));
		resourceDatabasePopulator.setIgnoreFailedDrops(true);

		DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
		dataSourceInitializer.setDataSource(datasource);
		dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);
		dataSourceInitializer.setEnabled(env.getProperty("mysql.datasource.initialize", Boolean.class, false));

		return dataSourceInitializer;
	}

	@Bean(name = "mysqlTransactionManager")
	public PlatformTransactionManager mysqlTransactionManager() {
		EntityManagerFactory factory = mysqlEntityManagerFactory().getObject();
		return new JpaTransactionManager(factory);
	}
	
	// jpa
	@PersistenceContext(unitName = "mysqlUnit")
	@Bean(name = "mysqlEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory() {
		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setDataSource(mysqlDataSource());
		factory.setPackagesToScan(new String[] { "com.honsoft.entity","com.honsoft.domain" });
		factory.setJpaDialect(new HibernateJpaDialect());
		factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.hbm2ddl.auto", env.getProperty("mysql.jpa.hibernate.ddl-auto"));
		jpaProperties.put("hibernate.show_sql", env.getProperty("mysql.jpa.hibernate.show_sql"));
		jpaProperties.put("hibernate.format_sql", env.getProperty("mysql.jpa.hibernate.format_sql"));
		jpaProperties.put("hibernate.dialect", env.getProperty("mysql.jpa.hibernate.dialect"));
		factory.setJpaProperties(jpaProperties);

		return factory;
	}

}
