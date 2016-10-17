package jserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server
{
    private static boolean running = true;

    public static void main(final String[] args) throws InterruptedException
    {

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                System.out.println("Shutting down.  Will not allow any more connections");
                running = false;

                while (true)
                    ;
            }
        });

        new Server().startServer();
    }

    public void startServer()
    {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        final Runnable serverTask = () ->
        {
            try
            {
                final ServerSocket serverSocket = new ServerSocket(8080);
                System.out.println("Server Started...");

                while (running)
                {
                    final Socket clientSocket = serverSocket.accept();
                    clientProcessingPool.submit(new ClientTask(clientSocket));
                }

                System.out.println("Server Stopping...");
                serverSocket.close();

            } catch (final IOException e)
            {
                System.err.println("Unable to process client request");
                e.printStackTrace();
            }
        };
        final Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

    private class ClientTask implements Runnable
    {
        private final Socket clientSocket;

        private ClientTask(final Socket clientSocket)
        {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run()
        {
            System.out.println("Got a client !");

            try
            {
                // Do whatever required to process the client's request
                BufferedReader inFromClient;
                inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                final DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
                final String clientSentence = inFromClient.readLine();
                System.out.println("Received: " + clientSentence);

                final String body = "Hello\n";

                outToClient.writeBytes("HTTP/1.1 200 OK\n");
                outToClient.writeBytes("Content-Type: text/html; charset=UTF-8\n");
                outToClient.writeBytes("Content-Encoding: UTF-8\n");
                outToClient.writeBytes("Content-Length: " + body.length() + "\n");
                outToClient.writeBytes("Server: jserver\n");
                outToClient.writeBytes("\n");
                outToClient.writeBytes("Hello\n");

                clientSocket.close();
            } catch (final IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
