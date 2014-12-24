
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

import static org.jboss.netty.channel.Channels.*;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.util.Timer;

/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 */
public class GraphiteClientPipelineFactory implements ChannelPipelineFactory {

	final ClientBootstrap bootstrap;
	private ChannelPipeline current_pipeline;
    private final Timer timer;
	private GraphiteClientHandler gch;
	private int RECONNECT_TIMEOUT = 5;
    private int FORCE_RECONNECT_TIMEOUT = 180;

   	public  GraphiteClientPipelineFactory(ClientBootstrap bootstrap, Timer timer){
		this.bootstrap=bootstrap;
		this.timer=timer;
	}

    public void setReconnectTimeout(int timeout) {
        this.RECONNECT_TIMEOUT=timeout;
    }
   
    public void setForceReconnectTimeout(int timeout){
        this.FORCE_RECONNECT_TIMEOUT=timeout;
    }
    public ChannelPipeline getPipeline() throws Exception {
        current_pipeline = pipeline();
        gch= new GraphiteClientHandler(this.bootstrap,this.timer);
        gch.setReconnectTimeout(RECONNECT_TIMEOUT);
        gch.setForceReconnectTimeout(FORCE_RECONNECT_TIMEOUT);

        // Add the text line codec combination first,
        current_pipeline.addLast("framer", new DelimiterBasedFrameDecoder(
                8192, Delimiters.lineDelimiter()));
        current_pipeline.addLast("decoder", new StringDecoder());
        current_pipeline.addLast("encoder", new StringEncoder());
        // and then business logic.
        current_pipeline.addLast("handler", gch);

        return current_pipeline;
    }

   public ChannelPipeline getCurrentPipeline() throws Exception {
	if(current_pipeline != null) return current_pipeline;
	else return getPipeline();
   }
}
