package pl.bst.server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import pl.bst.client.*;
import pl.bst.protocol.TreeType;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class NetworkIntegrationTest {
    private TreeConnection connect(TreeServer server,TreeType type)throws IOException{return TreeConnection.connect("127.0.0.1",server.port(),type);}
    @Test void completeCommandRoundTripIncludesAnEmptyTree()throws Exception{
        try(var server=new TreeServer(0)){server.start();try(var client=connect(server,TreeType.INTEGER)){
            assertEquals("",client.request("draw"));assertEquals("Inserted: 5",client.request("insert 5"));
            client.request("insert 3");assertEquals("5 3",client.request("draw"));assertEquals("Search 3: found",client.request("search 3"));
            assertEquals("Deleted: 3",client.request("delete 3"));assertEquals("Bye",client.request("bye"));
        }}
    }
    @Test void clientsOfTheSameTypeShareStateAndOtherTypesRemainIsolated()throws Exception{
        try(var server=new TreeServer(0)){server.start();try(var a=connect(server,TreeType.INTEGER);var b=connect(server,TreeType.INTEGER);var s=connect(server,TreeType.STRING)){
            a.request("insert 8");assertEquals("8",b.request("draw"));assertEquals("",s.request("draw"));
            s.request("insert żółw");assertEquals("żółw",s.request("draw"));
        }}
    }
    @Test void numericErrorAllowsFurtherRequests()throws Exception{
        try(var server=new TreeServer(0)){server.start();try(var client=connect(server,TreeType.DOUBLE)){
            assertTrue(client.request("insert NaN").startsWith("ERROR"));assertEquals("Inserted: 1.25",client.request("insert 1.25"));assertEquals("1.25",client.request("draw"));
        }}
    }
    @Test void injectedLineIsRejectedLocallyWithoutCorruptingTheSession()throws Exception{
        try(var server=new TreeServer(0)){server.start();try(var client=connect(server,TreeType.INTEGER)){
            assertThrows(IllegalArgumentException.class,()->client.request("insert 1\ninsert 2"));
            assertEquals("Inserted: 3",client.request("insert 3"));
        }}
    }
    @Test void serverClosureReleasesOpenSessions()throws Exception{
        var server=new TreeServer(0);server.start();try(var client=connect(server,TreeType.INTEGER)){
            server.close();assertThrows(IOException.class,()->client.request("draw"));server.await();
        }finally{server.close();}
    }
    @Test void unknownHandshakeIsRejectedAndClosed()throws Exception{
        try(var server=new TreeServer(0)){server.start();try(var socket=new Socket("127.0.0.1",server.port())){
            socket.setSoTimeout(2000);var writer=new PrintWriter(socket.getOutputStream(),true,StandardCharsets.UTF_8);
            var reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),StandardCharsets.UTF_8));writer.println("unknown");
            assertTrue(reader.readLine().startsWith("ERROR"));assertNull(reader.readLine());
        }}
    }
    @Test void manyClientsCanMutateOneTreeConcurrently()throws Exception{
        try(var server=new TreeServer(0)){server.start();try(var pool=Executors.newVirtualThreadPerTaskExecutor()){
            var tasks=new ArrayList<Future<?>>();for(int worker=0;worker<8;worker++){int offset=worker*20;
                tasks.add(pool.submit(()->{try(var client=connect(server,TreeType.INTEGER)){for(int i=0;i<20;i++)client.request("insert "+(offset+i));}return null;}));}
            for(var task:tasks)task.get(8,TimeUnit.SECONDS);
        }try(var client=connect(server,TreeType.INTEGER)){assertEquals(160,new HashSet<>(Arrays.asList(client.request("draw").split(" "))).size());}}
    }
    @Test void decoderHandlesEmptyAndTypedServerSnapshots(){assertNull(TreeDecoder.decode("",TreeType.INTEGER));
        assertEquals(4,TreeDecoder.decode("4 2 6",TreeType.INTEGER).value());assertEquals("ala",TreeDecoder.decode("ala żółw",TreeType.STRING).value());}
}
