-- ----------------------------
-- app用户表
-- ----------------------------
create table app_user (
    user_id             bigint(20)    not null            comment '用户ID',
    tenant_id           varchar(20)   default '000000'    comment '租户编号',
    user_name           varchar(30)   not null            comment '用户账号',
    nick_name           varchar(30)   default ''          comment '用户昵称',
    phonenumber         varchar(11)   default ''          comment '手机号码',
    email               varchar(50)   default ''          comment '用户邮箱',
    avatar              bigint(20)    default null        comment '头像地址',
    password            varchar(100)  default ''          comment '密码',
    status              char(1)       default '0'         comment '账号状态（0正常 1停用）',
    del_flag            char(1)       default '0'         comment '删除标志（0代表存在 1代表删除）',
    login_ip            varchar(128)  default ''          comment '最后登录IP',
    login_date          datetime      default null        comment '最后登录时间',
    remark              varchar(500)  default null        comment '备注',
    create_dept         bigint(20)    default null        comment '创建部门',
    create_by           bigint(20)    default null        comment '创建者',
    create_time         datetime      default null        comment '创建时间',
    update_by           bigint(20)    default null        comment '更新者',
    update_time         datetime      default null        comment '更新时间',
    primary key (user_id),
    unique key uk_app_user_tenant_username (tenant_id, user_name),
    key idx_app_user_tenant_phone (tenant_id, phonenumber)
) engine=innodb comment='app用户表';
