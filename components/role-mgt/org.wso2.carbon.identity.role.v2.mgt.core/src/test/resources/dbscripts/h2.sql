CREATE TABLE IF NOT EXISTS UM_DOMAIN(
    UM_DOMAIN_ID   INTEGER      NOT NULL AUTO_INCREMENT,
    UM_DOMAIN_NAME VARCHAR(255) NOT NULL,
    UM_TENANT_ID   INTEGER DEFAULT 0,
    PRIMARY KEY (UM_DOMAIN_ID, UM_TENANT_ID),
    UNIQUE (UM_DOMAIN_NAME, UM_TENANT_ID)
);

CREATE TABLE IF NOT EXISTS UM_HYBRID_ROLE_AUDIENCE(
            UM_ID INTEGER NOT NULL AUTO_INCREMENT,
            UM_AUDIENCE VARCHAR(255) NOT NULL,
            UM_AUDIENCE_ID VARCHAR(255) NOT NULL,
            UNIQUE (UM_AUDIENCE, UM_AUDIENCE_ID),
            PRIMARY KEY (UM_ID)
);

CREATE TABLE IF NOT EXISTS UM_HYBRID_ROLE(
            UM_ID INTEGER NOT NULL AUTO_INCREMENT,
            UM_ROLE_NAME VARCHAR(255) NOT NULL,
            UM_TENANT_ID INTEGER DEFAULT 0,
            UM_AUDIENCE_REF_ID INTEGER DEFAULT -1,
            PRIMARY KEY (UM_ID, UM_TENANT_ID),
            UNIQUE (UM_ROLE_NAME, UM_TENANT_ID, UM_AUDIENCE_REF_ID)
);

CREATE INDEX IF NOT EXISTS UM_ROLE_NAME_IND ON UM_HYBRID_ROLE(UM_ROLE_NAME);

CREATE TABLE IF NOT EXISTS UM_HYBRID_USER_ROLE(
    UM_ID INTEGER NOT NULL AUTO_INCREMENT,
    UM_USER_NAME VARCHAR(255),
    UM_ROLE_ID INTEGER NOT NULL,
    UM_TENANT_ID INTEGER DEFAULT 0,
    UM_DOMAIN_ID INTEGER,
    UNIQUE (UM_USER_NAME, UM_ROLE_ID, UM_TENANT_ID,UM_DOMAIN_ID),
    FOREIGN KEY (UM_ROLE_ID, UM_TENANT_ID) REFERENCES UM_HYBRID_ROLE(UM_ID, UM_TENANT_ID) ON DELETE CASCADE,
    FOREIGN KEY (UM_DOMAIN_ID, UM_TENANT_ID) REFERENCES UM_DOMAIN(UM_DOMAIN_ID, UM_TENANT_ID) ON DELETE CASCADE,
    PRIMARY KEY (UM_ID, UM_TENANT_ID)
);

CREATE TABLE IF NOT EXISTS UM_HYBRID_GROUP_ROLE(
    UM_ID        INTEGER NOT NULL AUTO_INCREMENT,
    UM_GROUP_NAME VARCHAR(255),
    UM_ROLE_ID   INTEGER NOT NULL,
    UM_TENANT_ID INTEGER DEFAULT 0,
    UM_DOMAIN_ID INTEGER,
    UNIQUE (UM_GROUP_NAME, UM_ROLE_ID, UM_TENANT_ID, UM_DOMAIN_ID),
    FOREIGN KEY (UM_ROLE_ID, UM_TENANT_ID) REFERENCES UM_HYBRID_ROLE (UM_ID, UM_TENANT_ID) ON DELETE CASCADE,
    FOREIGN KEY (UM_DOMAIN_ID, UM_TENANT_ID) REFERENCES UM_DOMAIN (UM_DOMAIN_ID, UM_TENANT_ID) ON DELETE CASCADE,
    PRIMARY KEY (UM_ID, UM_TENANT_ID)
);

CREATE TABLE IF NOT EXISTS IDN_SCIM_GROUP (
            ID INTEGER NOT NULL AUTO_INCREMENT,
            TENANT_ID INTEGER NOT NULL,
            ROLE_NAME VARCHAR(255) NOT NULL,
            ATTR_NAME VARCHAR(1024) NOT NULL,
            ATTR_VALUE VARCHAR(1024),
            AUDIENCE_REF_ID INTEGER DEFAULT -1,
            UNIQUE(TENANT_ID, ROLE_NAME, ATTR_NAME, AUDIENCE_REF_ID),
            PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS SP_APP (
        ID INTEGER NOT NULL AUTO_INCREMENT,
        TENANT_ID INTEGER NOT NULL,
        APP_NAME VARCHAR (255) NOT NULL ,
        USER_STORE VARCHAR (255) NOT NULL,
        USERNAME VARCHAR (255) NOT NULL ,
        DESCRIPTION VARCHAR (1024),
        ROLE_CLAIM VARCHAR (512),
        AUTH_TYPE VARCHAR (255) NOT NULL,
        PROVISIONING_USERSTORE_DOMAIN VARCHAR (512),
        IS_LOCAL_CLAIM_DIALECT CHAR(1) DEFAULT '1',
        IS_SEND_LOCAL_SUBJECT_ID CHAR(1) DEFAULT '0',
        IS_SEND_AUTH_LIST_OF_IDPS CHAR(1) DEFAULT '0',
        IS_USE_TENANT_DOMAIN_SUBJECT CHAR(1) DEFAULT '1',
        IS_USE_USER_DOMAIN_SUBJECT CHAR(1) DEFAULT '1',
        ENABLE_AUTHORIZATION CHAR(1) DEFAULT '0',
        SUBJECT_CLAIM_URI VARCHAR (512),
        IS_SAAS_APP CHAR(1) DEFAULT '0',
        IS_DUMB_MODE CHAR(1) DEFAULT '0',
        UUID CHAR(36),
        IMAGE_URL VARCHAR(1024),
        ACCESS_URL VARCHAR(1024),
        IS_DISCOVERABLE CHAR(1) DEFAULT '0',

        PRIMARY KEY (ID)
);

ALTER TABLE SP_APP ADD CONSTRAINT APPLICATION_NAME_CONSTRAINT UNIQUE(APP_NAME, TENANT_ID);
ALTER TABLE SP_APP ADD CONSTRAINT APPLICATION_UUID_CONSTRAINT UNIQUE(UUID);

CREATE TABLE IF NOT EXISTS API_RESOURCE (
    ID VARCHAR(255) NOT NULL PRIMARY KEY,
    CURSOR_KEY INTEGER NOT NULL AUTO_INCREMENT,
    NAME VARCHAR(255) NOT NULL,
    IDENTIFIER VARCHAR(255) NOT NULL,
    TENANT_ID INT NOT NULL,
    DESCRIPTION VARCHAR(255),
    TYPE VARCHAR(255) NOT NULL,
    REQUIRES_AUTHORIZATION BOOLEAN NOT NULL,
    CONSTRAINT IDENTIFIER_UNIQUE UNIQUE (IDENTIFIER, TENANT_ID)
);

CREATE TABLE IF NOT EXISTS SCOPE (
    ID VARCHAR(255) NOT NULL PRIMARY KEY,
    CURSOR_KEY INTEGER NOT NULL AUTO_INCREMENT,
    API_ID VARCHAR(255) NOT NULL,
    NAME VARCHAR(255) NOT NULL,
    DISPLAY_NAME VARCHAR(255) NOT NULL,
    TENANT_ID INT NOT NULL,
    DESCRIPTION VARCHAR(300),
    FOREIGN KEY (API_ID) REFERENCES API_RESOURCE(ID) ON DELETE CASCADE,
    CONSTRAINT SCOPE_UNIQUE UNIQUE (NAME, TENANT_ID)
);

CREATE TABLE IF NOT EXISTS APP_ROLE_ASSOCIATION (
    ROLE_ID VARCHAR(255) NOT NULL,
    APP_ID CHAR(36) NOT NULL,
    CONSTRAINT APP_ROLE_UNIQUE UNIQUE (ROLE_ID, APP_ID),
    FOREIGN KEY (APP_ID) REFERENCES SP_APP(UUID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ROLE_SCOPE (
    ROLE_ID VARCHAR(255) NOT NULL,
    SCOPE_NAME VARCHAR(255) NOT NULL,
    TENANT_ID INT NOT NULL,
    CONSTRAINT ROLE_SCOPE_UNIQUE UNIQUE (ROLE_ID, SCOPE_NAME, TENANT_ID),
    FOREIGN KEY (SCOPE_NAME, TENANT_ID) REFERENCES SCOPE(NAME, TENANT_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS IDP (
  ID                         INTEGER               AUTO_INCREMENT,
  TENANT_ID                  INTEGER,
  NAME                       VARCHAR(254) NOT NULL,
  IS_ENABLED                 CHAR(1)      NOT NULL DEFAULT '1',
  IS_PRIMARY                 CHAR(1)      NOT NULL DEFAULT '0',
  HOME_REALM_ID              VARCHAR(254),
  IMAGE                      MEDIUMBLOB,
  CERTIFICATE                BLOB,
  ALIAS                      VARCHAR(254),
  INBOUND_PROV_ENABLED       CHAR(1)      NOT NULL DEFAULT '0',
  INBOUND_PROV_USER_STORE_ID VARCHAR(254),
  USER_CLAIM_URI             VARCHAR(254),
  ROLE_CLAIM_URI             VARCHAR(254),
  DESCRIPTION                VARCHAR(1024),
  DEFAULT_AUTHENTICATOR_NAME VARCHAR(254),
  DEFAULT_PRO_CONNECTOR_NAME VARCHAR(254),
  PROVISIONING_ROLE          VARCHAR(128),
  IS_FEDERATION_HUB          CHAR(1)      NOT NULL DEFAULT '0',
  IS_LOCAL_CLAIM_DIALECT     CHAR(1)      NOT NULL DEFAULT '0',
  DISPLAY_NAME               VARCHAR(255),
  IMAGE_URL                  VARCHAR(1024),
  UUID                       CHAR(36) NOT NULL,
  PRIMARY KEY (ID),
  UNIQUE (TENANT_ID, NAME),
  UNIQUE (UUID)
);

CREATE TABLE IF NOT EXISTS IDP_GROUP (
			ID INTEGER AUTO_INCREMENT NOT NULL,
			IDP_ID INTEGER NOT NULL,
			TENANT_ID INTEGER NOT NULL,
			GROUP_NAME VARCHAR(255) NOT NULL,
			UUID CHAR(36) NOT NULL,
			PRIMARY KEY (ID),
			UNIQUE (IDP_ID, GROUP_NAME),
			UNIQUE (UUID),
			FOREIGN KEY (IDP_ID) REFERENCES IDP(ID) ON DELETE CASCADE);

CREATE TABLE IF NOT EXISTS UM_IDP_GROUP_ROLE(
            UM_ROLE_ID INTEGER NOT NULL,
            UM_GROUP_ID VARCHAR(36) NOT NULL,
            UM_TENANT_ID INTEGER NOT NULL,
            UNIQUE (UM_ROLE_ID, UM_GROUP_ID, UM_TENANT_ID),
            FOREIGN KEY (UM_ROLE_ID, UM_TENANT_ID) REFERENCES UM_HYBRID_ROLE(UM_ID, UM_TENANT_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS UM_SHARED_ROLE(
            UM_SHARED_ROLE_ID INTEGER NOT NULL,
            UM_MAIN_ROLE_ID INTEGER NOT NULL,
            UM_SHARED_ROLE_TENANT_ID INTEGER NOT NULL,
            UM_MAIN_ROLE_TENANT_ID INTEGER NOT NULL,
            UNIQUE (UM_SHARED_ROLE_ID, UM_MAIN_ROLE_ID),
            FOREIGN KEY (UM_SHARED_ROLE_ID, UM_SHARED_ROLE_TENANT_ID) REFERENCES UM_HYBRID_ROLE(UM_ID, UM_TENANT_ID) ON DELETE CASCADE,
            FOREIGN KEY (UM_MAIN_ROLE_ID, UM_MAIN_ROLE_TENANT_ID) REFERENCES UM_HYBRID_ROLE(UM_ID, UM_TENANT_ID) ON DELETE CASCADE
);