package ticker;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;

import java.util.Optional;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.*;

import com.microsoft.azure.functions.signalr.*;
import com.microsoft.azure.functions.signalr.annotation.*;

public class Function {

    ServiceBusSenderClient senderClient = new ServiceBusClientBuilder()
    .credential(System.getenv("APP_NAME")+".servicebus.windows.net", 
        new DefaultAzureCredentialBuilder().build())
            .sender()
            .queueName("ticks")
            .buildClient();



    // Use this function to create ticks into service-bus (for testing purposes)

    @FunctionName("createTicks")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.FUNCTION)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Generating ticks");

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            senderClient.sendMessage(new ServiceBusMessage("{\"tick\": " + System.currentTimeMillis() + "}"));
        }

        return request.createResponseBuilder(HttpStatus.OK).body("ok").build();

    }

    //This reads the ticks from Queue and pushes them to Signal-R

    @FunctionName("tickProcessor")
    @SignalROutput(name = "$return", hubName = "%APP_NAME%")
    public SignalRMessage  serviceBusProcess(
       @ServiceBusQueueTrigger(name = "msg",
                                queueName = "ticks",
                                connection = "SBCON") String message,
      final ExecutionContext context
    ) {
        context.getLogger().info(message);
        return new SignalRMessage("tick", "[{\"tock\": " + System.currentTimeMillis() + "},"+message+ "]");
    }


    @FunctionName("negotiate")
    public SignalRConnectionInfo negotiate(
            @HttpTrigger(
                name = "req",
                methods = { HttpMethod.POST },
                authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> req,
            @SignalRConnectionInfoInput(
                name = "connectionInfo",
                hubName = "%APP_NAME%") SignalRConnectionInfo connectionInfo) {

        return connectionInfo;
    }

}
