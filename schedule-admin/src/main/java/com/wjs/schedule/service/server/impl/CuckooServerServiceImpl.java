package com.wjs.schedule.service.server.impl;

import java.net.InetSocketAddress;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wjs.schedule.bean.ClientTaskInfoBean;
import com.wjs.schedule.bean.JobInfoBean;
import com.wjs.schedule.cache.JobClientCache;
import com.wjs.schedule.dao.exec.CuckooClientJobDetailMapper;
import com.wjs.schedule.dao.exec.CuckooClientJobDetailSubMapper;
import com.wjs.schedule.domain.exec.CuckooClientJobDetail;
import com.wjs.schedule.domain.exec.CuckooClientJobDetailCriteria;
import com.wjs.schedule.enums.CuckooClientJobStatus;
import com.wjs.schedule.exception.BaseException;
import com.wjs.schedule.service.server.CuckooServerService;
import com.wjs.schedule.vo.net.ClientInfo;

@Service("cuckooServerService")
public class CuckooServerServiceImpl implements CuckooServerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CuckooServerServiceImpl.class);
	
	@Autowired
	CuckooClientJobDetailMapper cuckooClientJobDetailMapper;
	
	@Autowired
	CuckooClientJobDetailSubMapper cuckooClientJobDetailSubMapper;

	@Override
	public CuckooClientJobDetail execRemoteJob(List<CuckooClientJobDetail> remoteJobExecs, JobInfoBean jobBean) {
		
		
		if(CollectionUtils.isEmpty(remoteJobExecs)){
			return null;
		}
		// 根据remoteJobExec 获取socket,
		Object socket = null;
		CuckooClientJobDetail socketClient = null;
		for (CuckooClientJobDetail cuckooClientJobDetail : remoteJobExecs) {
			socket = JobClientCache.get(cuckooClientJobDetail.getId());
			if(null != socket){
				socketClient = cuckooClientJobDetail;
				break;
			}
		}
		// 意外情况获取不到socket
		if(socket == null){
			return null;
		}

		// 更改client状态为Running
		socketClient.setCuckooClientStatus(CuckooClientJobStatus.RUNNING.getValue());
		cuckooClientJobDetailMapper.updateByPrimaryKeySelective(socketClient);
		
		// socket写数据 TODO
//		socket.write(jobBean);
		
		LOGGER.info("调用远程任务开始,jobNname:{},bean:{}" ,jobBean.getJobName(), jobBean);
		
		return socketClient;
	}

	@Override
	public List<CuckooClientJobDetail> getExecRemotesId(Long jobId) {
		

		//   服务器负载均衡控制
		ClientInfo remoteInfo = cuckooClientJobDetailSubMapper.getLoadBalanceClient(jobId);
		if(null == remoteInfo){
			return null;
		}
		
		// 查询可执行服务器详细信息，并返回
		CuckooClientJobDetailCriteria clientCrt = new CuckooClientJobDetailCriteria();
		clientCrt.createCriteria().andCuckooClientIpEqualTo(remoteInfo.getRemoteIp())
		.andCuckooClientTagEqualTo(remoteInfo.getRemoteTag());
		List<CuckooClientJobDetail> result = cuckooClientJobDetailMapper.selectByExample(clientCrt);

		return result;
	}

	@Override
	public Long addRemote(IoSession session, ClientTaskInfoBean clientTaskInfoBean) {

		// 数据库增加记录
		String ip = ((InetSocketAddress)session.getRemoteAddress()).getAddress().getHostAddress();;
		Long clientId = null;
		try {
			if(StringUtils.isEmpty(clientTaskInfoBean.getAppName()) || 
					StringUtils.isEmpty(clientTaskInfoBean.getClientTag()) || 
					StringUtils.isEmpty(clientTaskInfoBean.getTaskName())){
				throw new BaseException("clientTaskInfo error,AppName:{},ClientTag:{},TaskName:{}"
						,clientTaskInfoBean.getAppName(),clientTaskInfoBean.getClientTag(),clientTaskInfoBean.getTaskName());
			}
			CuckooClientJobDetailCriteria crt = new CuckooClientJobDetailCriteria();
			crt.createCriteria().andJobClassApplicationEqualTo(clientTaskInfoBean.getAppName())
			.andCuckooClientTagEqualTo(clientTaskInfoBean.getClientTag())
			.andJobNameEqualTo(clientTaskInfoBean.getTaskName());
			List<CuckooClientJobDetail> result = cuckooClientJobDetailMapper.selectByExample(crt);
			if(CollectionUtils.isEmpty(result)){
				
				CuckooClientJobDetail cuckooClientJobDetail = new CuckooClientJobDetail();
				cuckooClientJobDetail.setCuckooClientStatus(CuckooClientJobStatus.RUNNING.getValue());
				cuckooClientJobDetail.setCuckooClientTag(clientTaskInfoBean.getClientTag());
				cuckooClientJobDetail.setJobName(clientTaskInfoBean.getTaskName());
				cuckooClientJobDetail.setJobClassApplication(clientTaskInfoBean.getAppName());
				cuckooClientJobDetail.setBeanName(clientTaskInfoBean.getBeanName());
				cuckooClientJobDetail.setMethodName(clientTaskInfoBean.getMethodName());
				cuckooClientJobDetail.setCuckooClientIp(ip);
				cuckooClientJobDetailMapper.insertSelective(cuckooClientJobDetail);
				clientId =  cuckooClientJobDetailMapper.lastInsertId();
			}else{
				clientId = result.get(0).getId();
			}
		} catch (Exception e) {
			
			LOGGER.info("insert client job error:{},clientTaskInfoBean:{}", e.getMessage(),clientTaskInfoBean, e);
		}
		
		// 链接缓存中增加缓存
		ClientInfo socket = new ClientInfo();
		socket.setRemoteIp(ip);
		socket.setRemoteTag(clientTaskInfoBean.getClientTag());
		socket.setSession(session);
		JobClientCache.put(clientId, socket);
		return clientId;
	}

}
