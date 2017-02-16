package com.wjs.schedule.net.server;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wjs.schedule.bean.ClientTaskInfoBean;
import com.wjs.schedule.bean.MessageInfo;
import com.wjs.schedule.constant.CuckooNetConstant;
import com.wjs.schedule.enums.CuckooMessageType;
import com.wjs.schedule.executor.framerwork.CuckooClient;
import com.wjs.schedule.executor.framerwork.bean.ClientInfoBean;
import com.wjs.schedule.executor.framerwork.bean.CuckooTaskBean;
import com.wjs.schedule.executor.framerwork.cache.CuckooTaskCache;
import com.wjs.schedule.net.client.filter.ConnectFilter;
import com.wjs.schedule.net.client.handle.CuckooClientHandler;
import com.wjs.schedule.net.server.bean.IoServerBean;
import com.wjs.schedule.net.server.cache.IoServerCollection;

public class ServerUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(CuckooClient.class);

	public static boolean connect(IoServerBean bean) {

		if (bean == null || bean.getIp() == null || bean.getPort() == null) {
			return false;
		}
		try {
			NioSocketConnector connector = new NioSocketConnector();
			connector.getFilterChain().addLast("logger", new LoggingFilter());
			connector.getFilterChain().addLast("codec",
					new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName(CuckooNetConstant.ENCODING))));
			connector.getFilterChain().addLast("regist", new ConnectFilter());
			
			// // 设置连接超时检查时间
			connector.setConnectTimeoutCheckInterval(30);
			connector.setHandler(new CuckooClientHandler());

			// 建立连接
			ConnectFuture cf = connector.connect(new InetSocketAddress(bean.getIp(), bean.getPort()));
			// // 等待连接创建完成
			cf.awaitUninterruptibly();

			IoSession session = cf.getSession();
			bean.setSession(session);
			
			
			
			//
			// cf.getSession().write("Hi Server!");
			// cf.getSession().write("quit");
			//
			// // 等待连接断开
			// cf.getSession().getCloseFuture().awaitUninterruptibly();
			// // 释放连接
			// connector.dispose();
		} catch (Exception e) {

			LOGGER.error("could not connect to server,Ip:{},port:{}",bean.getIp(),bean.getPort());
			return false;
		}

		return true;
	}

	/*
	 * retry connect to server,in case of server resart
	 */
	public static void retryConnect() {
		
		Set<IoServerBean> servers = IoServerCollection.getSet();
		if(CollectionUtils.isNotEmpty(servers)){
			
			for (IoServerBean ioServerBean : servers) {
				if(null == ioServerBean.getSession()){
					ServerUtil.connect(ioServerBean);
				}
			}
		}
		
	}
	
	

	private static final Gson gson = new GsonBuilder().create();
	
	// 给所有服务器发消息
	public static void send(CuckooMessageType messageType,  Object message) {
		
		// 给服务端发消息
		MessageInfo msgInfo = new MessageInfo();
		msgInfo.setMessage(message);
		msgInfo.setMessageType(messageType);
		if(CollectionUtils.isNotEmpty(IoServerCollection.getSet())){
			for (Iterator<IoServerBean>  it = IoServerCollection.getSet().iterator(); it.hasNext() ; ) {
				IoServerBean server = it.next();
				if(null != server.getSession()){
					ServerUtil.send(messageType, server.getSession(), message);
					break;
				}
			}
		}
	}
	
	/**
	 * 给一台服务器发消息
	 * @param messageType
	 * @param session
	 * @param message
	 */
	public static void send(CuckooMessageType messageType, IoSession session, Object message) {
		
		try {
			// 给服务端发消息
			MessageInfo msgInfo = new MessageInfo();
			msgInfo.setMessage(message);
			msgInfo.setMessageType(messageType);
			String msg = gson.toJson(msgInfo);
			
			LOGGER.info("客户端发送消息:server:{}, msg:{}",session.getServiceAddress(), msg);
			session.write(msg);
		} catch (Exception e) {
			LOGGER.error("client message send error:{}" ,e.getMessage() ,e);
		}
	}

}