DROP TABLE IF EXISTS iasauser_systemrole;
DROP TABLE IF EXISTS projectdatabase;
DROP TABLE IF EXISTS projectuserright;
DROP TABLE IF EXISTS projectrolepermission;
DROP TABLE IF EXISTS projectpermission;
DROP TABLE IF EXISTS systemrole;
DROP TABLE IF EXISTS projectrole;
DROP TABLE IF EXISTS project;
DROP TABLE IF EXISTS iasauser;


CREATE TABLE project
(
    project_id           serial NOT NULL
        CONSTRAINT project_pkey
            PRIMARY KEY,
    project_name         varchar(255),
    project_creationdate timestamp,
    project_giturl       varchar(600),
    project_description  varchar(1000)
);

CREATE TABLE iasauser
(
    iasauser_id       serial       NOT NULL
        CONSTRAINT iasauser_pkey
            PRIMARY KEY,
    iasauser_name     varchar(255) NOT NULL
        CONSTRAINT iasauser_iasauser_name_key
            UNIQUE,
    iasauser_password varchar(255) NOT NULL,
    iasauser_active   boolean,
    iasauser_locked   boolean,
    iasauser_expired  boolean
);

CREATE TABLE systemrole
(
    systemrole_id         serial NOT NULL
        CONSTRAINT systemrole_pkey
            PRIMARY KEY,
    systemrole_name       varchar(255),
    systemrole_permission varchar(255)
);

CREATE TABLE projectrole
(
    projectrole_id   serial NOT NULL
        CONSTRAINT projectrole_pkey
            PRIMARY KEY,
    projectrole_name varchar(255)
);

CREATE TABLE projectdatabase
(
    project_id               integer      NOT NULL
        CONSTRAINT projectdatabase_pkey
            PRIMARY KEY
        CONSTRAINT projectdatabase_project_id_fkey
            REFERENCES project,
    projectdatabase_boltport integer      NOT NULL
        CONSTRAINT projectdatabase_projectdatabase_boltport_key
            UNIQUE,
    projectdatabase_url      varchar(600) NOT NULL,
    projectdatabase_user     varchar(50)  NOT NULL,
    projectdatabase_password varchar(100) NOT NULL
);

CREATE TABLE projectpermission
(
    projectpermission_id   serial NOT NULL
        CONSTRAINT projectpermission_pkey
            PRIMARY KEY,
    projectpermission_name varchar(255)
);

CREATE TABLE iasauser_systemrole
(
    systemrole_id integer NOT NULL
        CONSTRAINT iasauser_systemrole_systemrole_fkey
            REFERENCES systemrole,
    iasauser_id   integer NOT NULL
        CONSTRAINT iasauser_systemrole_iasauser_fkey
            REFERENCES iasauser,
    CONSTRAINT iasauser_systemrole_pkey
        PRIMARY KEY (systemrole_id, iasauser_id)
);

CREATE TABLE projectuserright
(
    projectrole_id integer NOT NULL
        CONSTRAINT projectuserright_projectrole_id_fkey
            REFERENCES projectrole,
    project_id     integer NOT NULL
        CONSTRAINT projectuserright_project_id_fkey
            REFERENCES project,
    iasauser_id    integer NOT NULL
        CONSTRAINT projectuserright_iasauser_id_fkey
            REFERENCES iasauser,
    CONSTRAINT projectuserright_pkey
        PRIMARY KEY (projectrole_id, project_id, iasauser_id)
);

CREATE TABLE projectrolepermission
(
    projectpermission_id integer NOT NULL
        CONSTRAINT projectrolepermission_projectpermission_id_fkey
            REFERENCES projectpermission,
    projectrole_id       integer NOT NULL
        CONSTRAINT projectrolepermission_projectrole_id_fkey
            REFERENCES projectrole,
    CONSTRAINT projectrolepermission_pkey
        PRIMARY KEY (projectpermission_id, projectrole_id)
);
