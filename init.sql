drop table if exists ua.users;
create table ua.`users`(
    `id` int(11) not null primary key auto_increment comment '用户ID',
    `username` varchar(50) unique not null comment '用户账号名称',
    `password` char(68) comment '用户密码',
    `status` int not null default 0 comment '用户状态 0-正常',
    `role_ids`   varchar(200) comment '用户角色ID，多角色以英文逗号分隔',
    `last_used` timestamp comment '最后登录时间'
) COMMENT '用户表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
insert into ua.users (username, password, role_ids) values ('admin', '{bcrypt}$2a$10$2QciMhK4xHNk33LHZgDqjuixzIDoTVgjPgC9o/Xmr/yEcJxtQ8SRm', '1'),
                                                        ('user', '{bcrypt}$2a$10$2QciMhK4xHNk33LHZgDqjuixzIDoTVgjPgC9o/Xmr/yEcJxtQ8SRm', '2');

drop table if exists ua.roles;
create table ua.`roles`(
    `id` int(11) not null primary key auto_increment comment '角色ID',
    `pid` int(11) not null comment '父角色ID',
    `sort` int(11) not null comment '角色顺序',
    `seq_no` varchar(32) not null comment '角色序号',
    `name` varchar(32) unique not null comment '角色英文名称',
    `desc` varchar(32) comment '角色中文名称',
    `status` int not null default 0 comment '角色状态 0-正常',
    `remark1` varchar(64) comment '备注1'
) COMMENT '平台角色表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
insert into ua.roles (pid, sort, seq_no, name, `desc`, remark1) values (-1, 0, '0', 'ROLE_ADMIN', '系统管理员', '系统内置，禁止删除'), (-1, 1, '1', 'ROLE_USER', '普通用户', '系统内置，禁止删除');

drop table if exists ua.permissions;
create table ua.`permissions`(
    `id` int(11) not null primary key auto_increment comment '权限ID',
    `pid` int(11) not null comment '权限父ID',
    `sort` int(11) not null comment '权限顺序',
    `name` varchar(32) unique not null comment '权限英文名称',
    `type` int not null comment '权限类型 0-菜单权限，1-功能权限',
    `desc` varchar(32) comment '权限中文描述',
    `route` varchar(32) comment '菜单路由',
    `remark1` varchar(64) comment '备注1'
) COMMENT '权限表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

drop table if exists ua.role_to_permission;
create table ua.`role_to_permission`(
    `rid` int(11) not null comment '角色ID',
    `pid` int(11) not null comment '权限ID'
) COMMENT '角色权限关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

drop table if exists ua.oauth_client_details;
create table ua.`oauth_client_details`(
    `id` int(11) not null primary key auto_increment comment '客户端记录ID',
    `client_id` varchar(64) not null unique comment '客户端ID',
    `client_secret` varchar(255) comment '客户端访问密匙',
    `resource_ids` varchar(255) comment '客户端所能访问的资源id集合',
    `scope`   varchar(64) comment '客户端申请的权限范围(read,write,trust)',
    `web_server_redirect_uri` varchar(128) comment '指定客户端支持的grant_type(authorization_code,password,refresh_token,implicit,client_credentials)',
    `authorized_grant_types` varchar(128) comment '客户端的重定向URI',
    `authorities` varchar(255) comment '客户端所拥有的Spring Security的权限值',
    `access_token_validity` int(11) comment '客户端的access_token的有效时间值(单位:秒)',
    `refresh_token_validity` int(11) comment '客户端的refresh_token的有效时间值(单位:秒)',
    `additional_information` varchar(255) comment '预留的字段，JSON数据',
    `autoapprove` boolean default false comment '用户是否自动Approval操作(true,false,read,write)'
)COMMENT '授权终端控制表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO ua.oauth_client_details (client_id, client_secret, resource_ids, scope, web_server_redirect_uri, authorized_grant_types, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove) VALUES ('app', '{bcrypt}$2a$10$EP3Jq0oGchfzU.UKjaTfOunDBlBRp1LNuXR0n25EgkKUc9I1guumO', 'ua-resource,file-resource', 'all', 'http://localhost:9322/ua/oauth/code', 'authorization_code,password,refresh_token', 'ROLE_USER,ROLE_ADMIN', 7200, 172800, null, 0);

drop table if exists fs.file;
create table fs.`file`(
    `id` int(11) not null primary key auto_increment comment '文件ID',
    `name` varchar(64) not null unique comment '文件名称',
    `type` varchar(8) comment '文件类型',
    `uri` varchar(64) comment '文件位置',
    `group_id` int comment '分组ID',
    `group_sort` int comment '组内序号',
    `create_at`   datetime comment '创建时间',
    `form_type` varchar(8) comment '来源类型',
    `favorite_type` varchar(8) comment '收藏类型',
    `remark1` varchar(64) comment '备注1',
    `remark2` varchar(128) comment '备注2'
)comment '系统文件表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

drop table if exists fs.video;
create table fs.`video`(
    `id` int not null primary key auto_increment comment '文件ID',
    `name` varchar(128) not null comment '文件名称',
    `type` varchar(8) comment '文件类型',
    `uri` varchar(128) comment '文件位置',
    `group_id` int comment '分组ID',
    `group_sort` int comment '组内序号',
    `create_at`   datetime comment '创建时间',
    `form_type` varchar(8) comment '来源类型',
    `favorite_type` varchar(8) comment '收藏类型',
    `remark1` varchar(64) comment '备注1',
    `remark2` varchar(128) comment '备注2',
    `cover_uri` varchar(128) comment '封面URI',
    `total_time` time comment '时长',
    `batch_seq` bigint comment '批次操作流水号'
)comment '视频记录表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
create INDEX create_at on fs.video(create_at desc);