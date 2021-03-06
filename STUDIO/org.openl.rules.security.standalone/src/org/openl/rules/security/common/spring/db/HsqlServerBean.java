package org.openl.rules.security.common.spring.db;

import org.hsqldb.Server;
import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Properties;

/**
 * Bean that will start an instance of an HSQL database.
 * <p/>
 * <p>
 * This class is primarily intended to be used in demo applications. It allows
 * for a self contained distribution including a database instance. HSQLDB will
 * be properly "shutdowned".
 * </p>
 * <p/>
 * <p>
 * This is an example of a bean configuration:
 * </p>
 * <p/>
 * <pre>
 *     &lt;bean id=&quot;dataBase&quot; class=&quot;com.exigen.example.solution.admin.database.util.HsqlServerBean&quot; singleton=&quot;true&quot; lazy-init=&quot;false&quot;&gt;
 *         &lt;property name=&quot;serverProperties&quot;&gt;
 *             &lt;props&gt;
 *                 &lt;prop key=&quot;server.port&quot;&gt;9101&lt;/prop&gt;
 *                 &lt;prop key=&quot;server.database.0&quot;&gt;webapps/myapp/db/test&lt;/prop&gt;
 *                 &lt;prop key=&quot;server.dbname.0&quot;&gt;test&lt;/prop&gt;
 *             &lt;/props&gt;
 *         &lt;/property&gt;
 *     &lt;/bean&gt;
 * </pre>
 *
 * @author Andrey Naumenko
 * @see org.hsqldb.Server
 */
public class HsqlServerBean implements InitializingBean, DisposableBean {
    private final Logger log = LoggerFactory.getLogger(HsqlServerBean.class);
    private Properties serverProperties;
    private boolean enabled = true;

    /**
     * The actual server instance.
     */
    private Server server;

    public void afterPropertiesSet() throws Exception {
        if (!enabled) {
            log.debug("HsqlServerBean is disabled, so HSQLDB will not be started");
            return;
        }

        HsqlProperties configProps = new HsqlProperties(serverProperties);

        ServerConfiguration.translateDefaultDatabaseProperty(configProps);

        // finished setting up properties - set some important behaviors as
        // well;
        server = new Server();
        server.setRestartOnShutdown(false);
        server.setNoSystemExit(true);
        server.setProperties(configProps);

        log.info("HSQL Server Startup sequence initiated");

        server.start();

        log.info("HSQL Server started on {}", server.getAddress());
    }

    public void destroy() {
        if (!enabled) {
            return;
        }
        log.info("HSQL Server Shutdown sequence initiated");
        server.signalCloseAllServerConnections();
        server.shutdown();
        server = null;
        log.info("HSQL Server stopped");
    }

    /**
     * If <code>false</code> this class do nothing. <code>true</code> by
     * default.
     *
     * @param enabled <code>true</code> or <code>false</code>.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Properties used to customize instance.
     *
     * @param serverProperties HSQLDB properties
     */
    public void setServerProperties(Properties serverProperties) {
        this.serverProperties = serverProperties;
    }
}
