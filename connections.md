Database connections are configured:

* cloud -  no DB used
* metaacct - reference to Tomcat resource /jdbc/acctDb (defined in $CATALINA_BASE/conf/server.xml, tomcat@segin )
* metaacct_cmd - src/main/resources/config.properties
* pbsmon - reference to Tomcat resource /jdbc/acctDb (defined in $CATALINA_BASE/conf/server.xml, makub@segin )
* perun_machines - no DB used

Further connections:
* resource statistics: segin:/var/www/metavo/resourcestats/pripojeni.php