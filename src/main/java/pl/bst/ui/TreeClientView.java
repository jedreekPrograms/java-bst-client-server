package pl.bst.ui;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import pl.bst.client.*;
import pl.bst.protocol.TreeType;

/** Network work is serialized off the JavaFX thread; views update from server replies. */
public final class TreeClientView extends BorderPane implements AutoCloseable {
    private final TextField host = new TextField("127.0.0.1"), port = new TextField("4444"), value = new TextField();
    private final ComboBox<TreeType> type = new ComboBox<>();
    private final Button connect = new Button("Connect"), insert = new Button("Insert"), delete = new Button("Delete"), search = new Button("Search"), draw = new Button("Draw");
    private final TextArea messages = new TextArea();
    private final Pane canvas = new Pane();
    private final TreeDrawer drawer = new TreeDrawer(canvas);
    private final ExecutorService io = Executors.newSingleThreadExecutor(Thread.ofPlatform().daemon(true).name("bst-client-io").factory());
    private final AtomicBoolean closed = new AtomicBoolean();
    private volatile TreeConnection connection;
    private TreeType selectedType;
    private boolean busy;

    public TreeClientView() {
        setPadding(new Insets(12));
        type.getItems().setAll(TreeType.values()); type.setValue(TreeType.INTEGER);
        host.setPrefColumnCount(12); port.setPrefColumnCount(5); value.setPromptText("Enter one value");
        host.setId("host"); port.setId("port"); type.setId("type"); connect.setId("connect");
        insert.setId("insert"); delete.setId("delete"); search.setId("search"); draw.setId("draw"); value.setId("value"); messages.setId("messages"); canvas.setId("tree-canvas");
        HBox address = new HBox(8,new Label("Host"),host,new Label("Port"),port,type,connect);
        HBox commands = new HBox(8,value,insert,search,delete,draw);
        HBox.setHgrow(value,Priority.ALWAYS); setTop(new VBox(10,address,commands));
        canvas.setPrefSize(600,300); canvas.setStyle("-fx-background-color: white;");
        ScrollPane viewport = new ScrollPane(new Group(canvas)); viewport.setPannable(true); setCenter(viewport);
        BorderPane.setMargin(viewport,new Insets(10,0,10,0));
        canvas.setOnScroll(event -> {
            double scale = Math.clamp(canvas.getScaleX()*(event.getDeltaY()>0?1.05:1/1.05),0.2,3);
            canvas.setScaleX(scale); canvas.setScaleY(scale); event.consume();
        });
        messages.setEditable(false); messages.setPrefRowCount(5); setBottom(messages);
        connect.setOnAction(event -> { if(connection==null)connect();else disconnect(); });
        insert.setOnAction(event->command("insert"));delete.setOnAction(event->command("delete"));
        search.setOnAction(event->command("search"));draw.setOnAction(event->command("draw"));
        controls();
    }

    private void connect() {
        final int targetPort;
        try { targetPort=Integer.parseInt(port.getText().trim()); if(targetPort<1||targetPort>65535)throw new IllegalArgumentException(); }
        catch(IllegalArgumentException invalid){log("Invalid port");return;}
        String targetHost=host.getText().trim();TreeType targetType=type.getValue();
        busy=true;controls();
        io.submit(()->{
            try{
                var opened=TreeConnection.connect(targetHost,targetPort,targetType);
                if(closed.get()){opened.close();return;}
                connection=opened;
                ui(()->{selectedType=targetType;busy=false;drawer.draw(null);log("Connected to "+targetType);controls();});
            }catch(Exception error){ui(()->{busy=false;log("Connection error: "+error.getMessage());controls();});}
        });
    }

    private void command(String command) {
        TreeConnection active=connection;if(active==null||busy)return;
        String argument=value.getText().trim();
        if(!command.equals("draw")&&(argument.isEmpty()||argument.chars().anyMatch(Character::isWhitespace))){log("Enter one value");return;}
        TreeType activeType=selectedType;busy=true;controls();
        io.submit(()->{
            try{
                String response=active.request(command.equals("draw")?command:command+" "+argument);
                String snapshot=command.equals("draw")?response:response.startsWith("ERROR")?null:active.request("draw");
                var tree=snapshot==null?null:TreeDecoder.decode(snapshot,activeType);
                ui(()->{if(connection!=active)return;log(command.equals("draw")?"Tree: "+response:response);
                    if(snapshot!=null)drawer.draw(tree);value.clear();busy=false;controls();});
            }catch(Exception error){active.close();ui(()->{if(connection!=active)return;connection=null;busy=false;log("Communication error: "+error.getMessage());controls();});}
        });
    }

    private void disconnect(){TreeConnection active=connection;connection=null;if(active!=null)active.close();selectedType=null;drawer.draw(null);log("Disconnected");controls();}
    private void controls(){boolean connected=connection!=null;connect.setText(connected?"Disconnect":"Connect");connect.setDisable(busy);
        host.setDisable(busy||connected);port.setDisable(busy||connected);type.setDisable(busy||connected);
        for(Button b:new Button[]{insert,delete,search,draw})b.setDisable(busy||!connected);value.setDisable(busy||!connected);}
    private void ui(Runnable action){if(!closed.get())Platform.runLater(()->{if(!closed.get())action.run();});}
    private void log(String text){messages.appendText(text+"\n");}
    @Override public void close(){closed.set(true);TreeConnection active=connection;if(active!=null)active.close();io.shutdownNow();}
}
