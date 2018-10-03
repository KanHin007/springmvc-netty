package demo.handler;

import demo.exception.PageNotFoundException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

public class HttpServer extends SimpleChannelInboundHandler<FullHttpRequest> {

    private Servlet dispatcherServlet;

    public HttpServer(Servlet servlet){
        this.dispatcherServlet = servlet;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {

        HttpResponseStatus httpResponseStatus = HttpResponseStatus.OK;


        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRequestURI(fullHttpRequest.getUri());

        String result = "";

        try {

            dispatcherServlet.service(request, response);
            result = response.getContentAsString();
            if(response.getStatus() == HttpServletResponse.SC_NOT_FOUND){
                throw new PageNotFoundException();
            }
        }catch(PageNotFoundException e){

            httpResponseStatus = HttpResponseStatus.NOT_FOUND;
            result = "404 not found ~";
        }catch (Exception e){
            result = e.getMessage();
            httpResponseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        }



        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                                                            HttpVersion.HTTP_1_1,
                                                            httpResponseStatus,
                                                            Unpooled.wrappedBuffer(result.getBytes()));


        channelHandlerContext.writeAndFlush(fullHttpResponse);

        channelHandlerContext.close();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }

    public Servlet getDispatcherServlet() {
        return dispatcherServlet;
    }

    public void setDispatcherServlet(Servlet dispatcherServlet) {
        this.dispatcherServlet = dispatcherServlet;
    }
}
