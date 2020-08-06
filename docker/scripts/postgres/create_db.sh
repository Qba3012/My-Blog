#!/bin/bash
set -e

POSTGRES="psql --username ${POSTGRES_USER}"
SERVER="${SERVER}"

echo "Creating database:  my_blog"

$POSTGRES <<EOSQL
    create database my_blog owner ${POSTGRES_USER} encoding = 'UTF8';

    create sequence hibernate_sequence start 1 increment 1;

    create table Captcha (
       id int8 not null,
        captcha varchar(255),
        captcha_key varchar(255),
        primary key (id)
    );


    create table Comment (
       id int8 not null,
        content varchar(255) not null,
        create_date TIMESTAMP WITH TIME ZONE not null,
        email varchar(50) not null,
        is_confirmed boolean not null,
        post_id int8 not null,
        primary key (id)
    );


    create table comment_token (
       token varchar(255),
        comment_id int8 not null,
        primary key (comment_id)
    );


    create table Post (
       id int8 not null,
        content varchar(1000) not null,
        create_date TIMESTAMP WITH TIME ZONE not null,
        email varchar(50) not null,
        is_confirmed boolean,
        title varchar(20) not null,
        primary key (id)
    );


    create table post_image (
       file_name varchar(255) not null,
        image_fit varchar(255),
        image_offset float8,
        image_url varchar(255),
        local_uri varchar(255),
        post_id int8 not null,
        primary key (post_id)
    );


    create table post_like (
       id int8 not null,
        email varchar(255) not null,
        post_id int8 not null,
        primary key (id)
    );


    create table post_token (
       token varchar(255),
        post_id int8 not null,
        primary key (post_id)
    );


    alter table if exists comment_token
       add constraint FKqceignna4wvkfxniw4omlr0me
       foreign key (comment_id)
       references Comment;


    alter table if exists post_image
       add constraint FKiauah6pey7ixhrhvhvqua9uej
       foreign key (post_id)
       references Post;


    alter table if exists post_token
       add constraint FK1wtw8gycpmev0oes8319feiw5
       foreign key (post_id)
       references Post;

    insert into post (
        id,content,create_date,email,is_confirmed,title) 
        values (
            1,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus at eleifend ex. Nunc egestas posuere purus in interdum. Sed laoreet lobortis sodales. Pellentesque ac magna mattis, ornare augue id, venenatis lorem. Quisque eget lacus malesuada, scelerisque augue vel, aliquet felis. Fusce tincidunt id lorem eget tempor. Suspendisse auctor pharetra augue, nec sollicitudin arcu varius id. Sed vel erat sem. Proin suscipit mauris nisi, consectetur gravida massa varius eu. Vivamus quis tempor nunc. Praesent nec molestie risus, mollis interdum libero. Phasellus viverra odio ac ornare vehicula. In et condimentum dolor, vel volutpat neque. Maecenas cursus, lectus nec malesuada ullamcorper, dolor ante consequat erat, et tristique velit justo dictum ante. Curabitur eget ante purus. Donec vel gravida arcu, eu aliquam nunc. Fusce nec purus porttitor, ullamcorper libero eget, rutrum risus. Vivamus id tortor sem. Donec vel nibh vel dui hendrerit iaculis at a lorem. ',CURRENT_TIMESTAMP,'jakubogorkiewicz89@gmail.com',true,'WOODEN PILE');

    insert into post_image (
        file_name,image_fit,image_offset,image_url,local_uri,post_id) 
        values (
            'wood.jpg','WIDTH',0.5,'${SERVER}/posts/1/image','/root/my-blog/post1/wood.jpg',1);

    insert into post (
        id,content,create_date,email,is_confirmed,title) 
        values (
            2,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus at eleifend ex. Nunc egestas posuere purus in interdum. Sed laoreet lobortis sodales. Pellentesque ac magna mattis, ornare augue id, venenatis lorem. Quisque eget lacus malesuada, scelerisque augue vel, aliquet felis. Fusce tincidunt id lorem eget tempor. Suspendisse auctor pharetra augue, nec sollicitudin arcu varius id. Sed vel erat sem. Proin suscipit mauris nisi, consectetur gravida massa varius eu. Vivamus quis tempor nunc. Praesent nec molestie risus, mollis interdum libero. Phasellus viverra odio ac ornare vehicula. In et condimentum dolor, vel volutpat neque. Maecenas cursus, lectus nec malesuada ullamcorper, dolor ante consequat erat, et tristique velit justo dictum ante. Curabitur eget ante purus. Donec vel gravida arcu, eu aliquam nunc. Fusce nec purus porttitor, ullamcorper libero eget, rutrum risus. Vivamus id tortor sem. Donec vel nibh vel dui hendrerit iaculis at a lorem. ',CURRENT_TIMESTAMP,'jakubogorkiewicz89@gmail.com',true,'SUNNY FOREST');

    insert into post_image (
        file_name,image_fit,image_offset,image_url,local_uri,post_id) 
        values (
            'sunny-forest.jpg','WIDTH',0.5,'${SERVER}/posts/2/image','/root/my-blog/post2/sunny-forest.jpg',2);

    insert into post (
        id,content,create_date,email,is_confirmed,title) 
        values (
            3,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus at eleifend ex. Nunc egestas posuere purus in interdum. Sed laoreet lobortis sodales. Pellentesque ac magna mattis, ornare augue id, venenatis lorem. Quisque eget lacus malesuada, scelerisque augue vel, aliquet felis. Fusce tincidunt id lorem eget tempor. Suspendisse auctor pharetra augue, nec sollicitudin arcu varius id. Sed vel erat sem. Proin suscipit mauris nisi, consectetur gravida massa varius eu. Vivamus quis tempor nunc. Praesent nec molestie risus, mollis interdum libero. Phasellus viverra odio ac ornare vehicula. In et condimentum dolor, vel volutpat neque. Maecenas cursus, lectus nec malesuada ullamcorper, dolor ante consequat erat, et tristique velit justo dictum ante. Curabitur eget ante purus. Donec vel gravida arcu, eu aliquam nunc. Fusce nec purus porttitor, ullamcorper libero eget, rutrum risus. Vivamus id tortor sem. Donec vel nibh vel dui hendrerit iaculis at a lorem. ',CURRENT_TIMESTAMP,'jakubogorkiewicz89@gmail.com',true,'MISTY SUNRISE');

    insert into post_image (
        file_name,image_fit,image_offset,image_url,local_uri,post_id) 
        values (
            'bridge-sunrise1.jpg','HEIGHT',0.5,'${SERVER}/posts/3/image','/root/my-blog/post3/bridge-sunrise1.jpg',3);

    insert into post (
        id,content,create_date,email,is_confirmed,title) 
        values (
            4,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus at eleifend ex. Nunc egestas posuere purus in interdum. Sed laoreet lobortis sodales. Pellentesque ac magna mattis, ornare augue id, venenatis lorem. Quisque eget lacus malesuada, scelerisque augue vel, aliquet felis. Fusce tincidunt id lorem eget tempor. Suspendisse auctor pharetra augue, nec sollicitudin arcu varius id. Sed vel erat sem. Proin suscipit mauris nisi, consectetur gravida massa varius eu. Vivamus quis tempor nunc. Praesent nec molestie risus, mollis interdum libero. Phasellus viverra odio ac ornare vehicula. In et condimentum dolor, vel volutpat neque. Maecenas cursus, lectus nec malesuada ullamcorper, dolor ante consequat erat, et tristique velit justo dictum ante. Curabitur eget ante purus. Donec vel gravida arcu, eu aliquam nunc. Fusce nec purus porttitor, ullamcorper libero eget, rutrum risus. Vivamus id tortor sem. Donec vel nibh vel dui hendrerit iaculis at a lorem. ',CURRENT_TIMESTAMP,'jakubogorkiewicz89@gmail.com',true,'FOREST SUNSET');

    insert into post_image (
        file_name,image_fit,image_offset,image_url,local_uri,post_id) 
        values (
            'forest-sunset.jpg','WIDTH',0.5,'${SERVER}/posts/4/image','/root/my-blog/post4/forest-sunset.jpg',4);

    insert into post (
        id,content,create_date,email,is_confirmed,title) 
        values (
            5,'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus at eleifend ex. Nunc egestas posuere purus in interdum. Sed laoreet lobortis sodales. Pellentesque ac magna mattis, ornare augue id, venenatis lorem. Quisque eget lacus malesuada, scelerisque augue vel, aliquet felis. Fusce tincidunt id lorem eget tempor. Suspendisse auctor pharetra augue, nec sollicitudin arcu varius id. Sed vel erat sem. Proin suscipit mauris nisi, consectetur gravida massa varius eu. Vivamus quis tempor nunc. Praesent nec molestie risus, mollis interdum libero. Phasellus viverra odio ac ornare vehicula. In et condimentum dolor, vel volutpat neque. Maecenas cursus, lectus nec malesuada ullamcorper, dolor ante consequat erat, et tristique velit justo dictum ante. Curabitur eget ante purus. Donec vel gravida arcu, eu aliquam nunc. Fusce nec purus porttitor, ullamcorper libero eget, rutrum risus. Vivamus id tortor sem. Donec vel nibh vel dui hendrerit iaculis at a lorem. ',CURRENT_TIMESTAMP,'jakubogorkiewicz89@gmail.com',true,'EARLY BRIDGE SUNRISE');

    insert into post_image (
        file_name,image_fit,image_offset,image_url,local_uri,post_id) 
        values (
            'bridge-sunrise2.jpg','HEIGHT',0.5,'${SERVER}/posts/5/image','/root/my-blog/post5/bridge-sunrise2.jpg',5);

EOSQL