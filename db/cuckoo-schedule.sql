CREATE TABLE cuckoo_client_job_detail
(
	id                             bigint          NOT NULL AUTO_INCREMENT	COMMENT '标准ID',
	job_class_application          varchar(50)     DEFAULT ''         NOT NULL	COMMENT '作业执行应用名',
	cuckoo_client_ip               varchar(30)     DEFAULT ''         NOT NULL	COMMENT '执行器IP',
	cuckoo_client_tag              varchar(128)    DEFAULT ''         NOT NULL	COMMENT '客户端标识',
	cuckoo_client_status           varchar(10)     DEFAULT ''         NOT NULL	COMMENT '客户端状态',
	job_name                       varchar(100)    DEFAULT ''         NOT NULL	COMMENT '任务名称',
	bean_name                      varchar(256)    DEFAULT ''         NOT NULL	COMMENT '实现类名称',
	method_name                    varchar(100)    DEFAULT ''         NOT NULL	COMMENT '方法名称',
	create_date                    decimal(13,0)   DEFAULT 0          NOT NULL	COMMENT '创建时间',
	modify_date                    decimal(13,0)   DEFAULT 0          NOT NULL	COMMENT '修改时间',
PRIMARY KEY(id)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_bin
COMMENT='客户端任务注册表'
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT;
CREATE UNIQUE INDEX uk_clientjob ON cuckoo_client_job_detail(job_class_application ASC ,cuckoo_client_tag ASC ,job_name ASC );
CREATE INDEX idx_clientjob_jobname ON cuckoo_client_job_detail(job_name ASC );





CREATE TABLE cuckoo_job_dependency
(
	id                             bigint          NOT NULL AUTO_INCREMENT	COMMENT '标准ID',
	job_id                         bigint          DEFAULT 0          NOT NULL	COMMENT '任务ID',
	dependency_job_id              bigint          DEFAULT 0          NOT NULL	COMMENT '依赖任务ID',
PRIMARY KEY(id)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_bin
COMMENT='上级任务依赖表'
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT;
CREATE INDEX idx_jobdependency_jobid ON cuckoo_job_dependency(job_id ASC );
CREATE INDEX idx_jobdependency_depid ON cuckoo_job_dependency(dependency_job_id ASC );



CREATE TABLE cuckoo_job_detail
(
	id                             bigint          NOT NULL AUTO_INCREMENT	COMMENT '标准ID',
	group_id                       bigint          DEFAULT 0          NOT NULL	COMMENT '分组ID',
	job_class_application          varchar(50)     DEFAULT ''         NOT NULL	COMMENT '作业执行应用名',
	job_name                       varchar(100)    DEFAULT ''         NOT NULL	COMMENT '任务名称',
	job_desc                       varchar(500)    DEFAULT ''         NOT NULL	COMMENT '任务描述',
	trigger_type                   varchar(10)     DEFAULT ''         NOT NULL	COMMENT '触发类型',
	type_daily                     varchar(6)      DEFAULT ''         NOT NULL	COMMENT '是否为日切任务',
	cron_expression                varchar(20)     DEFAULT ''         NOT NULL	COMMENT 'cron任务表达式',
	offset                         int             DEFAULT 0          NOT NULL	COMMENT '偏移量',
	job_status                     varchar(10)     DEFAULT ''         NOT NULL	COMMENT '任务状态',
	cuckoo_parallel_job_args       varchar(256)    DEFAULT ''         NOT NULL	COMMENT '并发/集群任务参数',
PRIMARY KEY(id)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_bin
COMMENT='任务表'
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT;
CREATE INDEX idx_jobdetail_groupid ON cuckoo_job_detail(group_id ASC );
CREATE INDEX idx_jobdetail_app ON cuckoo_job_detail(job_class_application ASC );
CREATE INDEX idx_jobdetail_name ON cuckoo_job_detail(job_name ASC );



CREATE TABLE cuckoo_job_exec_log
(
	id                             bigint          NOT NULL AUTO_INCREMENT	COMMENT '标准ID',
	job_id                         bigint          DEFAULT 0          NOT NULL	COMMENT '任务ID',
	group_id                       bigint          DEFAULT 0          NOT NULL	COMMENT '分组ID',
	job_class_application          varchar(50)     DEFAULT ''         NOT NULL	COMMENT '作业执行应用名',
	job_name                       varchar(100)    DEFAULT ''         NOT NULL	COMMENT '任务名称',
	trigger_type                   varchar(10)     DEFAULT ''         NOT NULL	COMMENT '触发类型',
	type_daily                     varchar(6)      DEFAULT ''         NOT NULL	COMMENT '是否为日切任务',
	cron_expression                varchar(20)     DEFAULT ''         NOT NULL	COMMENT 'cron任务表达式',
	tx_date                        int             DEFAULT 0          NOT NULL	COMMENT '任务执行业务日期',
	flow_last_time                 decimal(13,0)   DEFAULT 0          NOT NULL	COMMENT '流式任务上一次时间参数',
	flow_cur_time                  decimal(13,0)   DEFAULT 0          NOT NULL	COMMENT '流式任务当前时间参数',
	cuckoo_parallel_job_args       varchar(256)    DEFAULT ''         NOT NULL	COMMENT '并发/集群任务参数',
	job_start_time                 decimal(13,0)   DEFAULT 0          NOT NULL	COMMENT '任务开始时间',
	job_end_time                   decimal(13,0)   DEFAULT 0          NOT NULL	COMMENT '任务结束时间',
	exec_job_status                varchar(10)     DEFAULT ''         NOT NULL	COMMENT '执行状态',
	cuckoo_client_ip               varchar(30)     DEFAULT ''         NOT NULL	COMMENT '执行器IP',
	cuckoo_client_tag              varchar(128)    DEFAULT ''         NOT NULL	COMMENT '客户端标识',
	latest_check_time              decimal(13,0)   DEFAULT 0          NOT NULL	COMMENT '最近检查时间',
	need_triggle_next              boolean         DEFAULT 1          NOT NULL	COMMENT '是否触发下级任务',
	force_triggle                  boolean         DEFAULT 1          NOT NULL	COMMENT '是否强制触发',
	remark                         varchar(500)    DEFAULT ''         NOT NULL	COMMENT '备注',
PRIMARY KEY(id)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_bin
COMMENT='任务执行流水表'
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT;
CREATE INDEX idx_joblog_jobid ON cuckoo_job_exec_log(job_id ASC );
CREATE INDEX idx_joblog_groupid ON cuckoo_job_exec_log(group_id ASC );
CREATE INDEX idx_joblog_starttime ON cuckoo_job_exec_log(job_start_time ASC );
CREATE INDEX idx_joblog_endtime ON cuckoo_job_exec_log(job_end_time ASC );


CREATE TABLE cuckoo_job_extend
(
	job_id                         bigint          DEFAULT 0          NOT NULL	COMMENT '任务ID',
	email_list                     varchar(2000)   DEFAULT ''         NOT NULL	COMMENT '邮件列表逗号分隔',
	over_time_long                 bigint          DEFAULT 0          NOT NULL	COMMENT '邮件超时时间设置(毫秒)',
PRIMARY KEY(job_id)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_bin
COMMENT='任务其他信息'
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT;


CREATE TABLE cuckoo_job_group
(
	id                             bigint          NOT NULL AUTO_INCREMENT	COMMENT '标准ID',
	group_name                     varchar(100)    DEFAULT ''         NOT NULL	COMMENT '分组名称',
	group_desc                     varchar(500)    DEFAULT ''         NOT NULL	COMMENT '分组描述',
PRIMARY KEY(id)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_bin
COMMENT='任务分组表'
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT;



CREATE TABLE cuckoo_job_next_job
(
	id                             bigint          NOT NULL AUTO_INCREMENT	COMMENT '标准ID',
	job_id                         bigint          DEFAULT 0          NOT NULL	COMMENT '任务ID',
	next_job_id                    bigint          DEFAULT 0          NOT NULL	COMMENT '下级任务ID',
PRIMARY KEY(id)
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_bin
COMMENT='下级任务触发表'
AUTO_INCREMENT=1
ROW_FORMAT=COMPACT;
CREATE UNIQUE INDEX uk_cuckoo_next_job ON cuckoo_job_next_job(next_job_id ASC );
CREATE INDEX idx_jobnext_jobid ON cuckoo_job_next_job(job_id ASC );



