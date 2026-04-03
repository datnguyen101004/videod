create table users (
    id bigint auto_increment primary key ,
    email varchar(255),
    password varchar(50),
    role varchar(50),
    plan varchar(50),
    login_method varchar(50)
);

create table videos (
    id bigint auto_increment primary key ,
    title varchar(255) ,
    description varchar(255),
    category varchar(255),
    key_storage varchar(255),
    url varchar(255),
    author_email varchar(255)
);