package com.devs.feedback.handler;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.*;
import com.amazonaws.serverless.proxy.internal.servlet.AwsHttpServletResponse;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.devs.feedback.ApiApplication;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;

@Slf4j
public class StreamLambdaHandler extends SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> {

    private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(
                    ApiApplication.class
            );
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("Erro ao iniciar Spring", e);
        }
    }

    public StreamLambdaHandler(Class<AwsProxyRequest> awsProxyRequestClass, Class<AwsProxyResponse> awsProxyResponseClass, RequestReader<AwsProxyRequest, HttpServletRequest> requestReader, ResponseWriter<AwsHttpServletResponse, AwsProxyResponse> responseWriter, SecurityContextWriter<AwsProxyRequest> securityContextWriter, ExceptionHandler<AwsProxyResponse> exceptionHandler, Class<?> springBootInitializer, InitializationWrapper init, WebApplicationType applicationType) {
        super(awsProxyRequestClass, awsProxyResponseClass, requestReader, responseWriter, securityContextWriter, exceptionHandler, springBootInitializer, init, applicationType);
    }

//    @Override
//    public AwsProxyResponse handleRequest(AwsProxyRequest request, Context context) {
//        log.info("handle feedback: {}", request);
//        return handler.proxy(request, context);
//    }

}
