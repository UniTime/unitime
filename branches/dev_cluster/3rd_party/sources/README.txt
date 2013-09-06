Content of 3rd_party/sources folder was generated using Maven Dependency Plugin
	mvn dependency:copy-dependencies -Dclassifier=sources -DexcludeGroupIds=asm,mysql,org.osgi,org.springframework.ldap,jstl,org.json -DexcludeArtifactIds=staxmapper -DoutputDirectory=3rd_party/sources

Content of WebContent/WEB-INF/lib folder was generated using Maven Dependency Plugin
	mvn dependency:copy-dependencies -DexcludeScope=provided -DoutputDirectory=WebContent/WEB-INF/lib