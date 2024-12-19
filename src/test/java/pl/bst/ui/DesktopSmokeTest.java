package pl.bst.ui;

import java.util.concurrent.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import pl.bst.server.TreeServer;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="DISPLAY",matches=".+")
class DesktopSmokeTest {
    @BeforeAll static void toolkit()throws Exception{var ready=new CompletableFuture<Void>();Platform.startup(()->{Platform.setImplicitExit(false);ready.complete(null);});ready.get(10,TimeUnit.SECONDS);}
    @AfterAll static void exit(){Platform.exit();}
    static <T>T fx(Callable<T> work)throws Exception{var task=new FutureTask<>(work);Platform.runLater(task);return task.get(10,TimeUnit.SECONDS);}
    @Test void desktopClientConnectsAndDrawsTheServerTreeWithoutBlockingJavaFX()throws Exception{
        try(var server=new TreeServer(0)){server.start();var view=new TreeClientView[1];var stage=new Stage[1];
            fx(()->{stage[0]=new Stage();view[0]=new TreeClientView();stage[0].setScene(new Scene(view[0],850,650));stage[0].show();
                ((TextField)view[0].lookup("#port")).setText(Integer.toString(server.port()));((Button)view[0].lookup("#connect")).fire();return null;});
            try{
                await(()->fx(()->!((Button)view[0].lookup("#insert")).isDisabled()));
                fx(()->{((TextField)view[0].lookup("#value")).setText("42");((Button)view[0].lookup("#insert")).fire();return null;});
                await(()->fx(()->((TextArea)view[0].lookup("#messages")).getText().contains("Inserted: 42")));
                fx(()->{assertFalse(((Pane)view[0].lookup("#tree-canvas")).getChildren().isEmpty());assertTrue(view[0].lookup("#type").isDisabled());
                    assertNotNull(stage[0].getScene().snapshot(null));return null;});
            }finally{fx(()->{view[0].close();stage[0].close();return null;});}
        }
    }
    static void await(Callable<Boolean> ready)throws Exception{long end=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);while(System.nanoTime()<end){if(ready.call())return;Thread.sleep(10);}fail("Desktop operation timed out");}
}
