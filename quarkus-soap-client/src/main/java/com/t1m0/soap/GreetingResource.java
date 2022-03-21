package com.t1m0.soap;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


@Path("/greeting")
public class GreetingResource {

    @ConfigProperty(name = "com.t1m0.soap.service.endpointUrl")
    private String endpointUrl;

    @GET
    @Path("/{name}")
    public Greeting greeting(@PathParam("name")String name) {
        MDC.put("username",name);
        SimpleSoapServicePortType simpleSoapService = getClient();
        GreetingRequest request = new GreetingRequest();
        request.setName(name);

        GreetingResponse response = simpleSoapService.greeting(request);


        Greeting message = new Greeting();
        message.setMessage(response.getMessage());
        return message;
    }

    private SimpleSoapServicePortType getClient() {
        SimpleSoapServicePortType soapService = new SimpleSoapService().getSimpleSoapService();

        final BindingProvider bindingProvider = (BindingProvider) soapService;
        bindingProvider.getRequestContext().put("schema-validation-enabled", false);
        bindingProvider.getRequestContext().put("set-jaxb-validation-event-handler", false);
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);

        Binding aBinding = bindingProvider.getBinding();
        List<Handler> handlerChain = aBinding.getHandlerChain();
        handlerChain.add(new SOAPDebugHandler());
        aBinding.setHandlerChain(handlerChain);

        return soapService;
    }

    public static class SOAPDebugHandler implements SOAPHandler<SOAPMessageContext> {
        private static final Logger LOGGER = LoggerFactory.getLogger(SOAPDebugHandler.class);
        static final String OUT_LOG = "Sent:\n{}";
        static final String IN_LOG = "Received:\n{}";

        public SOAPDebugHandler() {
        }

        public void close(MessageContext inContext) {
        }

        public Set<QName> getHeaders() {
            return Collections.emptySet();
        }

        public boolean handleFault(SOAPMessageContext inContext) {
            this.logMessage(SOAPDebugHandler.LogLevel.ERROR, inContext);
            return true;
        }

        public boolean handleMessage(SOAPMessageContext inContext) {
            if (LOGGER.isDebugEnabled()) {
                this.logMessage(SOAPDebugHandler.LogLevel.DEBUG, inContext);
            }
            return true;
        }

        private void logMessage(SOAPDebugHandler.LogLevel logLevel, SOAPMessageContext inContext) {
            String logMessage = this.getLogMessage(inContext);

            try {
                String soapMessage = this.getSoapMessage(inContext);
                switch(logLevel) {
                    case DEBUG:
                        LOGGER.debug(logMessage, soapMessage);
                        break;
                    case INFO:
                        LOGGER.info(logMessage, soapMessage);
                        break;
                    case ERROR:
                        LOGGER.error(logMessage, soapMessage);
                }
            } catch (Exception e) {
                LOGGER.error("Tracing the soap fault led to an error.", e);
            }

        }

        private String getLogMessage(SOAPMessageContext inContext) {
            boolean outboundProperty = this.getOutBoundProperty(inContext);
            return outboundProperty ? OUT_LOG : IN_LOG;
        }

        private boolean getOutBoundProperty(SOAPMessageContext inContext) {
            Boolean outboundProperty = (Boolean)inContext.get("javax.xml.ws.handler.message.outbound");
            return outboundProperty != null && outboundProperty;
        }

        private String getSoapMessage(SOAPMessageContext inContext) {
            return inContext.getMessage() == null ? "(N/A)" : convertToString(inContext.getMessage(), false);
        }

        private enum LogLevel { DEBUG, INFO, ERROR}

        public String convertToString(SOAPMessage inMessage, boolean inIsPretty) {
            // handle no message object.
            if (inMessage == null) {
                return null;
            }

            try {
                // prepare the transformation
                Transformer aTransformer = getTransformer(inIsPretty);

                // prepare result
                StringWriter outResult = new StringWriter();

                // read source into stream, format it and return formatted string.
                final Source anXmlHeaderSource;
                if (inMessage.getSOAPHeader() != null) {
                    anXmlHeaderSource = new DOMSource(inMessage.getSOAPHeader().getOwnerDocument());
                } else {
                    anXmlHeaderSource = new DOMSource(inMessage.getSOAPBody().getOwnerDocument());
                }

                ByteArrayOutputStream aHeaderOutputStream = new ByteArrayOutputStream();
                StreamResult aHeaderResult = new StreamResult(aHeaderOutputStream);
                aTransformer.transform(anXmlHeaderSource, aHeaderResult);
                outResult.write(aHeaderOutputStream.toString());

                // return the formatted string
                String message = outResult.toString();
                if (!inIsPretty && message != null) {
                    message = message.replace("\n", "");
                    message = message.replace("\t", "");
                    message = message.replaceAll(">\\s*<", "><");
                }
                return message;

            } catch (TransformerException | SOAPException aTransformationProblem) {
                throw new RuntimeException("A problem occurred transforming XML to text.", aTransformationProblem);
            }
        }

        private static Transformer getTransformer(boolean indentOutput) throws TransformerConfigurationException {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());

            if (indentOutput) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }

            return transformer;
        }
    }

}
