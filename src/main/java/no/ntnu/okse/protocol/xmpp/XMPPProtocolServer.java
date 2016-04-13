package no.ntnu.okse.protocol.xmpp;/*


 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import no.ntnu.okse.core.messaging.Message;
import no.ntnu.okse.protocol.AbstractProtocolServer;
import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.storage.inmemory.MemoryStorageProviderRegistry;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authorization.AccountCreationException;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.modules.Module;
import org.apache.vysper.xmpp.modules.extension.xep0049_privatedata.PrivateDataModule;
import org.apache.vysper.xmpp.modules.extension.xep0050_adhoc_commands.AdhocCommandsModule;
import org.apache.vysper.xmpp.modules.extension.xep0054_vcardtemp.VcardTempModule;
import org.apache.vysper.xmpp.modules.extension.xep0077_inbandreg.InBandRegistrationModule;
import org.apache.vysper.xmpp.modules.extension.xep0092_software_version.SoftwareVersionModule;
import org.apache.vysper.xmpp.modules.extension.xep0119_xmppping.XmppPingModule;
import org.apache.vysper.xmpp.modules.extension.xep0133_service_administration.ServiceAdministrationModule;
import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
import org.apache.vysper.xmpp.server.XMPPServer;
/**
 * starts the server as a standalone application
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 */
public class XMPPProtocolServer extends AbstractProtocolServer {
    protected static final String SERVERTYPE = "xmpp";

    private static Logger log = Logger.getLogger(XMPPProtocolServer.class.getName());

    public XMPPProtocolServer(String host, Integer port) {
        this.host = host;
        this.port = port;
    }



    public static void init() throws AccountCreationException, FileNotFoundException {

        String domain = "localhost";
        String addedModuleProperty = System.getProperty("vysper.add.module");
        List<Module> listOfModules = null;
        if (addedModuleProperty != null) {
            String[] moduleClassNames = addedModuleProperty.split(",");
            listOfModules = createModuleInstances(moduleClassNames);
        }
        else {
            System.out.println("vysper.add.module is null");
        }
        // choose the storage you want to use
        //StorageProviderRegistry providerRegistry = new JcrStorageProviderRegistry();
        StorageProviderRegistry providerRegistry = new MemoryStorageProviderRegistry();
        //final Entity adminJID = EntityImpl.parseUnchecked("admin@" + domain);
        final Entity adminJID = EntityImpl.parseUnchecked("admin");
        final AccountManagement accountManagement = (AccountManagement) providerRegistry
                .retrieve(AccountManagement.class);
        String initialPassword = System.getProperty("vysper.admin.initial.password", "password");
        if (!accountManagement.verifyAccountExists(adminJID)) {
            System.out.println("Adding user");
            accountManagement.addUser(adminJID, initialPassword);
        }
        XMPPServer server = new XMPPServer(domain);
        server.addEndpoint(new TCPEndpoint());
        //server.addEndpoint(new StanzaSessionFactory());
        server.setStorageProviderRegistry(providerRegistry);
//        server.setTLSCertificateInfo(new File("src/bogus_mina_tls.cert"), "boguspw");
        server.setTLSCertificateInfo(new File("src/main/bogus_mina_tls.cert"), "boguspw");
        try {
            server.start();
            System.out.println("vysper server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.addModule(new SoftwareVersionModule());
        server.addModule(new EntityTimeModule());
        server.addModule(new VcardTempModule());
        server.addModule(new XmppPingModule());
        server.addModule(new PrivateDataModule());
        server.addModule(new InBandRegistrationModule());
        server.addModule(new AdhocCommandsModule());
        final ServiceAdministrationModule serviceAdministrationModule = new ServiceAdministrationModule();
        // unless admin user account with a secure password is added, this will be not become effective
        serviceAdministrationModule.setAddAdminJIDs(Arrays.asList(adminJID));
        server.addModule(serviceAdministrationModule);
        if (listOfModules != null) {
            for (Module module : listOfModules) {
                server.addModule(module);
            }
        }
    }
    private static List<Module> createModuleInstances(String[] moduleClassNames) {
        List<Module> modules = new ArrayList<Module>();
        for (String moduleClassName : moduleClassNames) {
            Class<Module> moduleClass;
            try {
                moduleClass = (Class<Module>) Class.forName(moduleClassName);
            } catch (ClassCastException e) {
                System.err.println("not a Vysper module class: " + moduleClassName);
                continue;
            } catch (ClassNotFoundException e) {
                System.err.println("could not load module class " + moduleClassName);
                continue;
            }
            try {
                Module module = moduleClass.newInstance();
                modules.add(module);
            } catch (Exception e) {
                System.err.println("failed to instantiate module class " + moduleClassName);
                continue;
            }
        }
        return modules;
    }
    @Override
    public void boot() {
        if (!_running) {
            _serverThread = new Thread(this::run);
            _serverThread.setName("XMPPProtocolServer");
            _serverThread.start();
            _running = true;
            log.info("XMPPProtocolServer booted successfully");
        }
    }

    @Override
    public void run() {
        try {
            init();
        } catch (AccountCreationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopServer() {
        _running = false;
    }

    @Override
    public String getProtocolServerType() {
            return SERVERTYPE;
    }

    @Override
    public void sendMessage(Message message) {
    }

    public boolean isRunning() {
        return _running;
    }
}