/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package domainhealth.backend.sender;

import domainhealth.core.env.AppLog;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;

import java.net.ConnectException;
import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.TimerTask;
import java.text.SimpleDateFormat;


/**
 * Handles a client-side channel.
 */
public class GraphiteClientHandler extends SimpleChannelUpstreamHandler {

//      private GraphiteBackgroundSender gSender;
        private  int RECONNECT_TIMEOUT = 5;
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	private SimpleDateFormat sdf;

  

	final ClientBootstrap bootstrap;
      private final Timer timer;
      private long startTime = -1;
  
      public GraphiteClientHandler(ClientBootstrap bootstrap, Timer timer) {
          this.bootstrap = bootstrap;
          this.timer = timer;
	  this.sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
      }

	/*
      public void setSender(GraphiteBackgroundSender gSender) {
 		this.gSender=gSender;
      }*/
      public void setReconnectTimeout(int timeout) {
		this.RECONNECT_TIMEOUT=timeout;
      }

    InetSocketAddress getRemoteAddress() {
     return (InetSocketAddress) bootstrap.getOption("remoteAddress");
     }

    @Override
     public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
     //System.out.println("Disconnected from: " + getRemoteAddress());
	 
		println("Disconnected from: " + getRemoteAddress());

     }

  @Override
  public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
      println("Sleeping for: " + this.RECONNECT_TIMEOUT + 's');
	  println("ChannelClosed: " + GraphiteBackgroundSender.isShuttingDown()); 
	  if (!GraphiteBackgroundSender.isShuttingDown())
	  {
		timer.newTimeout(new TimerTask() {
		public void run(Timeout timeout) throws Exception {
			try {
                println("Reconnecting to: " + getRemoteAddress());
				bootstrap.connect();
			} catch (Exception e) {
				AppLog.getLogger().error("Error cn trying to reconnect after Reconnect timeout expired : " + e.getMessage(),e);
			}
          }}, this.RECONNECT_TIMEOUT, TimeUnit.SECONDS);
	   }

      
  }

    @Override
    public void handleUpstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            println(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, MessageEvent e) {
        // Print out the line received from the server.
        println("GRAPHITE SERVER SEND MESSAGE !! :"+e.getMessage());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
	Throwable cause = e.getCause();
  	 if (cause instanceof ConnectException) {
              startTime = -1;
	      AppLog.getLogger().error("Failed to connect: "+ cause.getMessage(),cause);
              //println("Failed to connect: " + cause.getMessage());
          }
          if (cause instanceof ReadTimeoutException) {
              // The connection was OK but there was no traffic for last period.
			AppLog.getLogger().error("Disconnecting due to read timeout " + cause.getMessage(),cause);
              //println("Disconnecting due to no inbound traffic");
          } else {
			AppLog.getLogger().error("Disconnecting due to no inbound traffic " + cause.getMessage(),cause);
             
			//cause.printStackTrace();
          }
          ctx.getChannel().close();
        //e.getChannel().close();
    }

  @Override
  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
    if (startTime < 0) {
           startTime = System.currentTimeMillis();
     }
	println("Connected to: " + getRemoteAddress());
  }

   void println(String msg) {
	  String date=sdf.format(new Date());
          if (startTime < 0) {
	      AppLog.getLogger().warning(String.format("%s :[CONNECTION IS DOWN] %s",date, msg));
              //System.err.format("%s :[SERVER IS DOWN] %s%n",date, msg);
          } else {
	      AppLog.getLogger().warning(String.format("%s :[UPTIME: %5ds] %s",date,(System.currentTimeMillis() - startTime) / 1000,msg));
              //System.err.format("%s :[UPTIME: %5ds] %s%n",date, (System.currentTimeMillis() - startTime) / 1000, msg);
   }
      }
}

