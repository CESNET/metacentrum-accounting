Database connections are configured:

* cerit-stats - src/main/resources/config.properties
* cloud -  no DB used
* metaacct - reference to Tomcat resource /jdbc/acctDb (defined in $CATALINA_BASE/conf/server.xml, tomcat@segin )
* metaacct_cmd - src/main/resources/config.properties
* nebula_pbs - src/main/resources/config.properties
* pbsmon - reference to Tomcat resource /jdbc/acctDb (defined in $CATALINA_BASE/conf/server.xml, makub@segin )
* perun_machines - no DB used