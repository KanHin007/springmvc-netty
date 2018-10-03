package demo.server;

import demo.handler.HttpServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.springframework.context.ApplicationContext;

import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.Servlet;

import javax.servlet.ServletException;

/**
 * @author larry
 */
public class ChatServer {

    int port = 8888;

    public void start(int port, final Servlet servlet) {

        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup work = new NioEventLoopGroup();

        ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap
                .group(boss, work)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new HttpServerCodec());
                        socketChannel.pipeline().addLast(new HttpObjectAggregator(64 * 1024));
                        socketChannel.pipeline().addLast(new HttpServer(servlet));
                    }
                }).option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_NODELAY,true);

        try {
            ChannelFuture future = serverBootstrap.bind(this.port).sync();
            System.out.println("-----------聊天服务器已启动----------");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }

    }


    public static void main(String[] args) {


 //       ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");

        new ChatServer().start(8888, getDispatcherServlet(null));

    }

    private static DispatcherServlet getDispatcherServlet(ApplicationContext context) {

        MockServletContext servletContext = new MockServletContext();
        MockServletConfig servletConfig = new MockServletConfig(servletContext);

        XmlWebApplicationContext wac = new XmlWebApplicationContext();

        wac.setServletContext(servletContext);
        wac.setServletConfig(servletConfig);

        wac.setConfigLocation("classpath:applicationContext.xml");
        wac.refresh();
        DispatcherServlet dispatcherServlet = new DispatcherServlet(wac);

        try {
            dispatcherServlet.init(servletConfig);
        } catch (ServletException e) {
            e.printStackTrace();
        }

        return dispatcherServlet;
    }

}


