package com.wjs.schedule.executor.aspectj;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.wjs.schedule.bean.JobInfoBean;
import com.wjs.schedule.enums.MessageType;
import com.wjs.schedule.exception.BaseException;
import com.wjs.schedule.executor.annotation.CuckooTask;
import com.wjs.schedule.net.server.ServerUtil;

/**
 * mysql的for update no wait 实现
 * 参考资料：http://blog.itpub.net/7591490/viewspace-1033495/
 * @author Silver
 * @date 2017年1月15日 下午6:22:03 
 * 
 *
 */
@Aspect
@Component
public class CuckooTaskAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(CuckooTaskAspect.class);

	@Around("@annotation(task)")
	public Object lockWait(ProceedingJoinPoint pjp, CuckooTask task) throws Throwable {

		
		JobInfoBean jobinfo = null;
		try {
			Signature sign = pjp.getSignature();
			Object[] args = pjp.getArgs();
			if(null == args || args.length < 1){
				LOGGER.error("unknow exception :can not get task param! pjp:{},task:{}", pjp, task.value());
				throw new BaseException("unknow exception :can not get task param! pjp:{},task:{}", pjp, task.value());
			}
			jobinfo = (JobInfoBean)args[0];
			LOGGER.info("task exec start taskName:{} , exector:{} , params :{}", task.value(), sign, jobinfo);
			
			Object obj = pjp.proceed();
			
			// 发送服务端，任务执行完成
			
			ServerUtil.send(MessageType.JOBSUCCED, jobinfo);

			LOGGER.info("task exec succed taskName:{}, jobInfo:{}", task.value(), jobinfo);
			return obj;
		} catch (Exception e) {
			LOGGER.error("task exec error taskName:{}", task.value(), e);
			// 发送服务端，任务执行失败
			ServerUtil.send(MessageType.JOBFAILED, jobinfo);
			throw e;
		}

	}
}
